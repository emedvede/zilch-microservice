package com.zilch.service;

import com.zilch.entities.Currency;
import com.zilch.entities.Transaction;
import com.zilch.entities.TransactionType;
import com.zilch.exceptions.ErrorMessage;
import com.zilch.exceptions.CardException;
import com.zilch.helper.Helper;
import com.zilch.repository.CurrencyRepository;
import com.zilch.repository.TransactionRepository;
import com.zilch.repository.TransactionTypeRepository;
import com.zilch.repository.CardRepository;
import com.zilch.helper.HelperImpl;
import com.zilch.entities.Card;
import org.hibernate.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

/**
 * TransactionService tests
 *
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
public class TransactionServiceTest {
    @TestConfiguration
    static class TransactionServiceImplTestContextConfiguration {
        @Bean
        public TransactionService transactionService() {
            return new TransactionServiceImpl();
        }

        @Bean
        public Helper validator() {
            return new HelperImpl();
        }
        //for annotation validation on method signature
        @Bean
        public MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }
    public static final String TEST_CURRENCY = "EUR";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";
    static int globalIdCounter = 1;

    public static final Integer CURRENCY_ID = 1;

    @Value("${db.updated_by}")
    String lastUpdatedBy;

    @Value("${application.transactionCredit.type.credit}")
    String credit;

    @Value("${application.transactionCredit.type.debit}")
    String debit;

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    @MockBean
    private TransactionTypeRepository transactionTypeRepository;

    @MockBean
    private CardService cardService;

    private Currency currency;
    private Card card1;
    private Card card2;
    private TransactionType typeCredit;
    private TransactionType typeDebit;
    private Transaction transactionCredit;
    private Transaction transactionDebit;

    @Before
    public void setUp() throws CardException {
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY, LAST_UPDATED_BY);
        card1 = new Card(USER,currency, new BigDecimal(0), LAST_UPDATED_BY);
        card1.setId(1);
        card2 = new Card(USER,currency, new BigDecimal(40), LAST_UPDATED_BY);
        card2.setId(2);
        typeCredit = new TransactionType(credit,"credit trn", LAST_UPDATED_BY);
        typeDebit = new TransactionType(debit,"debit trn", LAST_UPDATED_BY);
        transactionCredit = new Transaction(String.valueOf(globalIdCounter++) ,typeCredit,new BigDecimal(20),card1,currency,"Credit transaction");
        transactionCredit.setId(5);
        transactionDebit = new Transaction(String.valueOf(globalIdCounter++) ,typeDebit,new BigDecimal(20),card2,currency,"Debit transaction");
        transactionDebit.setId(6);


        //getTransactionsBycardId
        Mockito.when(cardService.findById(card1.getId())).thenReturn(card1);
        Mockito.when(transactionRepository.findBycard(card1))
                .thenReturn(Arrays.asList(transactionCredit));

        Mockito.when(transactionRepository.findBycard(card2))
                .thenReturn(Arrays.asList(transactionCredit));


        //createTransaction
        Currency wrong = new Currency(2, "Wrong",LAST_UPDATED_BY);
        Mockito.when(currencyRepository.findByName("Wrong")).thenReturn(wrong);
        Mockito.when(cardRepository.save(new Card(USER,wrong, new BigDecimal(0), LAST_UPDATED_BY))).thenThrow(new ObjectNotFoundException("",""));

        Mockito.when(currencyRepository.findByName(TEST_CURRENCY)).thenReturn(currency);
        Mockito.when(transactionTypeRepository.getOne(typeCredit.getId())).thenReturn(typeCredit);
        Mockito.when(transactionTypeRepository.getOne(typeDebit.getId())).thenReturn(typeDebit);
        Mockito.when(cardService.findById(card1.getId())).thenReturn(card1);
        Mockito.when(cardService.findById(card2.getId())).thenReturn(card2);
        Mockito.when(cardService.findById(1001)).thenReturn(null);
    }

    //public List<Transaction> getTransactionsBycardId(@NotNull Integer cardId) throws CardException;
    @Test
    public void testGetTransactionsBycardId_Success() throws CardException {
        List<Transaction> found = transactionService.getTransactionsBycardId(card1.getId());
        assertNotNull(found);
        assertTrue(found.size() == 1);
        assertTrue(found.get(0).getId().equals(transactionCredit.getId()) );
     }

    @Test
    public void testGetTransactionsBycardId_Failed() throws CardException {
        String error = String.format(ErrorMessage.NO_CARD_FOUND,card2.getId().toString());
        Mockito.when(cardService.findById(card2.getId())).thenThrow(new CardException(error,HttpStatus.BAD_REQUEST.value()));
        try {
            List<Transaction> found = transactionService.getTransactionsBycardId(card2.getId());
            fail();
        } catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NO_CARD_FOUND,card2.getId().toString()));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    //public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currency, @NotBlank String cardId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws CardException;
    @Test
    public void testCreateTransaction_SuccessCredit() throws CardException {
        int amount = 100;
        Mockito.when(cardService.updateCardAmount(card1,String.valueOf(amount),true)).thenReturn(card1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = globalIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),card1.getId().toString(),typeCredit.getId(),String.valueOf(amount),"Success trn");
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionCredit.getId()) );
    }

    @Test
    public void testCreateTransaction_SuccessDebit() throws CardException {
        int amount = -10;
        Mockito.when(cardService.updateCardAmount(card2,String.valueOf(amount),false)).thenReturn(card2);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        int counter = globalIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),card2.getId().toString(), typeDebit.getId(),String.valueOf(amount),"Success trn");
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionDebit.getId()) );
    }

    @Test
    public void testCreateTransaction_DebitFailure() throws CardException {
        int amount = -100;
        int counter = globalIdCounter++;
        String error = String.format(ErrorMessage.NOT_ENOUGH_FUNDS,card2.getId(),String.valueOf(amount));
        Mockito.when(cardService.updateCardAmount(card2,String.valueOf(amount),false)).
                thenThrow(new CardException(error, HttpStatus.BAD_REQUEST.value()));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),card2.getId().toString(), typeDebit.getId(),String.valueOf(amount),"Success trn");
            fail();
        } catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NOT_ENOUGH_FUNDS,card2.getId(),String.valueOf(amount)));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    public void testCreateTransaction_cardNotFound() throws CardException {
        int amount = 100;
        int counter = globalIdCounter++;
        String notFoundcardId = "1001";
        Mockito.when(cardService.updateCardAmount(card1,String.valueOf(amount),true)).thenReturn(card1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),notFoundcardId,typeCredit.getId(),String.valueOf(amount),"No card");
            fail();
        } catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NO_CARD_FOUND, notFoundcardId));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }


    @Test
    public void testCreateTransaction_AmountNotNumber() throws CardException {
        String wrongAmount = "AAAee";
        Mockito.when(cardService.updateCardAmount(card1,String.valueOf(wrongAmount),true)).thenReturn(card1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = globalIdCounter++;
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter), currency.getName(), card1.getId().toString(), typeCredit.getId(), wrongAmount, "Fail trn");
        }catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,wrongAmount));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
}
