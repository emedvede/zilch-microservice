package com.zilch.repository;


import com.zilch.entities.Currency;
import com.zilch.entities.Card;
import com.zilch.entities.Transaction;
import com.zilch.entities.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TransactionRepository tests
 * Use in-memory h2database
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@PropertySource("classpath:application.properties")
public class TransactionRepositoryTest {
    public static final String TEST_CURRENCY = "EUR";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";

    @Value("${application.transaction.type.credit}")
    String credit;

    @Value("${application.transaction.type.debit}")
    String debit;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Card card1;
    private Card card2;
    private Currency currency;
    private TransactionType typeCredit;
    private TransactionType typeDebit;
    private Transaction transaction;

    static int globalIdCounter = 1;

    public static final Integer CURRENCY_ID = 1;


    @Before
    public void before(){
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY );
        entityManager.persistAndFlush(currency);

        card1 = new Card( USER ,new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);
        card2 = new Card( USER ,new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);

        entityManager.persist(card1);
        entityManager.persist(card2);
        entityManager.flush();

        typeCredit = new TransactionType(credit,"credit trn", LAST_UPDATED_BY);
        typeDebit = new TransactionType(debit,"debit trn", LAST_UPDATED_BY);
        entityManager.persist(typeCredit);
        entityManager.persist(typeDebit);
        entityManager.flush();

        transaction = new Transaction(String.valueOf(globalIdCounter++),typeCredit,new BigDecimal(20),card1,currency,"Credit transaction");
        entityManager.persist(transaction);
        entityManager.flush();

    }

    @Test
    public void testFindBycard() {
        List<Transaction> trns = transactionRepository.findBycard(card1);
        assertTrue(trns.size() > 0);
        assertTrue(trns.get(0).getcard().getId().equals(card1.getId()));
        assertTrue(trns.get(0).getId().equals(transaction.getId()));
    }

    @Test
    public void testSave_Credit() {
        int counter = globalIdCounter++;
        Transaction transaction = new Transaction(String.valueOf(counter),typeCredit,new BigDecimal(20),card2,currency,"Credit transaction");
        Transaction found = transactionRepository.save(transaction);
        assertNotNull(found);
        assertTrue(found.getCurrency().getName().equals(TEST_CURRENCY));
        assertTrue(found.getAmount().equals(new BigDecimal(20)));
        assertTrue(found.getType().getId().equals(credit));
        assertTrue(found.getGlobalId().equals(String.valueOf(counter)));
        assertTrue(found.getcard().getId().equals(card2.getId()));
    }

    @Test
    public void testSave_Debit() {
        int counter = globalIdCounter++;
        Transaction transactionDebit = new Transaction(String.valueOf(counter),typeCredit,new BigDecimal(-10),card1,currency,"Credit transaction");
        Transaction found = transactionRepository.save(transactionDebit );
        assertNotNull(found);
        assertTrue(found.getCurrency().getName().equals(TEST_CURRENCY));
        assertTrue(found.getAmount().equals(new BigDecimal(-10)));
        assertTrue(found.getType().getId().equals(credit));
        assertTrue(found.getGlobalId().equals(String.valueOf(counter)));
        assertTrue(found.getcard().getId().equals(card1.getId()));
    }

    @Test
    public void whenSave_FailWrongCurrency() {
        Currency currency = currencyRepository.findByName("AAA");
        int counter = globalIdCounter++;
        Transaction transaction = new Transaction(String.valueOf(counter),typeCredit,new BigDecimal(20),card2,currency,"Credit transaction");
        try{
            Transaction found = transactionRepository.save(transaction);
            fail();
        } catch(ConstraintViolationException ex){
            assertTrue( ex.getMessage().contains("Transaction currency must be provided"));
        }
    }

    @Test
    public void whenSave_NotUniqueGlobalId() {
        int counter = globalIdCounter - 1;
        Transaction transaction = new Transaction(String.valueOf(counter),typeCredit,new BigDecimal(20),card2,currency,"Credit transaction");
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(DataIntegrityViolationException ex){
            assertTrue( ex.getMessage().contains("could not execute statement"));
        }
    }

    @Test
    public void whenSave_NoBalance() {
        int counter =  globalIdCounter++;
        Transaction transaction = new Transaction(String.valueOf(counter),typeCredit,null,card2,currency,"Credit transaction");
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(ConstraintViolationException ex){
            assertFalse(ex.getConstraintViolations().isEmpty());
            assertTrue(ex.getConstraintViolations().iterator().next().getMessage().contains("Transaction amount must be provided"));

        }
    }

    @Test
    public void whenSave_FailWrongcard() {
        Card card = cardRepository.getOne(100);
        int counter = globalIdCounter++;
        Transaction transaction = new Transaction(String.valueOf(counter),typeCredit,new BigDecimal(20),card,currency,"Credit transaction");
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(DataIntegrityViolationException ex){
            assertTrue( ex.getMessage().contains("could not execute statement"));
        }
    }

    @Test
    public void whenSave_FailWrongType() {
        TransactionType type = transactionTypeRepository.getOne("wrong");
        int counter = globalIdCounter++;
        Transaction transaction = new Transaction(String.valueOf(counter),type,new BigDecimal(20),card2,currency,"Credit transaction");
        try{
            Transaction found = transactionRepository.save(transaction);
            entityManager.flush();
            fail();
        } catch(DataIntegrityViolationException ex){
            assertTrue( ex.getMessage().contains("could not execute statement"));
        }
    }
}
