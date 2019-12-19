package com.zilch.service;

import com.zilch.entities.Currency;
import com.zilch.exceptions.ErrorMessage;
import com.zilch.exceptions.CardException;
import com.zilch.repository.CurrencyRepository;
import com.zilch.repository.TransactionRepository;
import com.zilch.repository.CardRepository;
import com.zilch.helper.Helper;
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

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.junit.Assert.*;
/**
 * CardService tests
 *
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
public class CardServiceTest {
    @TestConfiguration
    static class CardServiceImplTestContextConfiguration {
        @Bean
        public CardService cardService() {
            return new CardServiceImpl();
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
    public static final Integer CURRENCY_ID = 1;

    @Value("${db.updated_by}")
    String lastUpdatedBy;

    @Autowired
    private CardService cardService;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    Currency currency;
    Card card1;
    Card card2;

    @Before
    public void setUp() {
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY, LAST_UPDATED_BY);
        card1 = new Card(USER,currency, new BigDecimal(0), LAST_UPDATED_BY);
        card1.setId(1);
        card2 = new Card(USER,currency, new BigDecimal(20), LAST_UPDATED_BY);
        card2.setId(2);

        //CardService.findAll
        Mockito.when(cardRepository.findAllByOrderByIdAsc())
                .thenReturn(Arrays.asList(card1, card2));
        //findById
        Mockito.when(cardRepository.findById(card1.getId())).thenReturn(Optional.of(card1));
        Mockito.when(cardRepository.findById(110)).thenReturn(Optional.empty());
        //
        //CardService.findUserId
        Mockito.when(cardRepository.findByUserId(USER))
                .thenReturn(Arrays.asList(card1, card2));
        Mockito.when(cardRepository.findByUserId("test"))
                .thenReturn(new ArrayList<Card>());

        //createCard
        Currency wrong = new Currency(2, "Wrong",LAST_UPDATED_BY) ;
        Mockito.when(currencyRepository.findByName("Wrong")).thenReturn(wrong);
        Mockito.when(cardRepository.save(new Card(USER,wrong, new BigDecimal(0), LAST_UPDATED_BY))).thenThrow(new ObjectNotFoundException("",""));
        Mockito.when(currencyRepository.findByName(TEST_CURRENCY)).thenReturn(currency);
        Mockito.when(cardRepository.save(card1)).thenReturn(card1);
        Mockito.when(cardRepository.save(card2)).thenReturn(card2);
    }

    //public List<Card> findAll() throws CardException;

    @Test
    public void testFindAll() throws CardException {
        List<Card> found = cardService.findAll();
        assertNotNull(found);
        assertTrue(found.size() == 2);
        assertTrue(found.get(0).getId().equals(card1.getId()) );
        assertTrue(found.get(1).getId().equals(card2.getId()) );
    }

    //public Card findById(@NotNull Integer id) throws CardException;
    @Test
    public void testFindById_Success() throws CardException {
        Card found = cardService.findById(card1.getId());
        assertNotNull(found);
        assertTrue(found.getId().equals(card1.getId()) );
    }

    @Test(expected = ConstraintViolationException.class)
    public void testFindById_Null() throws CardException {
        Card found = cardService.findById(null);
    }

    @Test
    public void testFindById_DoesntExist() throws CardException {
        try {
            Card found = cardService.findById(110);
            assertNull(found);
            fail();
        }catch(CardException e){
            assertEquals(e.getMessage(),String.format(ErrorMessage.NO_CARD_FOUND,"110"));
            assertEquals(e.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
    // public List<Card> findByUserId(@NotBlank String userId) throws CardException;
    @Test
    public void testFindByUserId_Success() throws CardException {
        List<Card> found = cardService.findByUserId(card1.getUserId());
        assertNotNull(found);
        assertTrue(found.size() == 2 );
    }

    @Test(expected = ConstraintViolationException.class)
    public void testFindByUserId_Null() throws CardException {
        cardService.findByUserId(null);
    }

    @Test
    public void testFindByUserId_DoesntExist() throws CardException {
            List<Card> found = cardService.findByUserId("test");
            assertNotNull(found);
            assertTrue(found.size() == 0);
    }

    //public Card createCard(@NotBlank String currency) throws CardException;

    @Test(expected = ConstraintViolationException.class)
    public void testCreatecard_Null() throws CardException {
        Card found = cardService.createCard(USER,null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreatecard_Blank() throws CardException {
        Card found = cardService.createCard(USER,"");
    }

    @Test
    public void testCreatecard_CurrencyNotFound() throws CardException {
        try {
        Card found = cardService.createCard(USER,"Wrong");
        }catch(CardException e){
            assertEquals(e.getMessage(),String.format(ErrorMessage.NO_CURRENCY_PRESENT,"Wrong"));
            assertEquals(e.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    public void testCreatecard_Success() throws CardException {
        Mockito.when(cardRepository.save(Mockito.any(Card.class))).thenReturn(card1);
        Card found = cardService.createCard(USER,TEST_CURRENCY);
        assertEquals(found.getId(),card1.getId());
    }

    //   public Card updateCardAmount(@NotNull Card card,@NotNull String amount,@NotNull Boolean isCredit) throws CardException;

    @Test
    public void testUpdatecardAmount_isCredit() throws CardException {
        int amount = 30;
        Card found = cardService.updateCardAmount(card1,String.valueOf(amount),true);
        assertEquals(found.getId(),card1.getId());
        assertEquals(found.getBalance(),new BigDecimal(amount));
    }

    @Test
    public void testUpdatecardAmount_isDebitSuccess() throws CardException {
        int amount = 10;
        Card found = cardService.updateCardAmount(card2,String.valueOf(amount),false);
        assertEquals(found.getId(),card2.getId());
        assertEquals(found.getBalance(),new BigDecimal(10));
    }

    @Test
    public void testUpdatecardAmount_isDebitSuccess2() throws CardException {
        int amount = -10;
        Card found = cardService.updateCardAmount(card2,String.valueOf(amount),false);
        assertEquals(found.getId(),card2.getId());
        assertEquals(found.getBalance(),new BigDecimal(10));
    }

    @Test
    public void testUpdatecardAmount_isDebitFailure() throws CardException {
        int amount = 100;
        try {
            Card found = cardService.updateCardAmount(card2, String.valueOf(amount), false);
            fail();
        } catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NOT_ENOUGH_FUNDS,card2.getId(),String.valueOf(amount)));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    public void testUpdatecardAmount_AmountNotANumber() throws CardException {
        String badAmount = "STTTT";
        try {
            Card found = cardService.updateCardAmount(card2, badAmount, false);
            fail();
        } catch (CardException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,badAmount));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
}
