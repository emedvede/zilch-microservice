package com.zilch.repository;

import com.zilch.entities.Currency;

import javax.validation.ConstraintViolationException;

import com.zilch.entities.Card;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
/**
 * CardRepository tests
 * Use in-memory h2database
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class CardRepositoryTest {
    public static final String TEST_CURRENCY = "GBP";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";
    public static final Integer CURRENCY_ID = 1;


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Card card1;
    private Card card2;
    private Currency currency;


    @Before
    public void before(){
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY );
        entityManager.persistAndFlush(currency);

        card1 = new Card( USER ,new Currency(CURRENCY_ID,TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);

        card2 = new Card(USER,new Currency(CURRENCY_ID,TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);

        entityManager.persist(card1);
        entityManager.persist(card2);
        entityManager.flush();
    }

    @Test
    public void whenFindById_thenReturncard() {
        // when
        Optional<Card> found = cardRepository.findById(card2.getId());
        assertTrue(found.isPresent());

        // then
        assertTrue(found.get().getCurrency().getName().equals(TEST_CURRENCY));
        assertTrue(found.get().getUserId().equals(USER));
        assertTrue(found.get().getBalance().equals(new BigDecimal(0)));
    }

    @Test
    public void whenFindById_Nocard() {
        Optional<Card> found = cardRepository.findById(10);
        assertTrue(!found.isPresent());
    }

    @Test
    public void whenFindByUserId_thenReturnCard() {
        // when
        List<Card> found = cardRepository.findByUserId(USER);
        //then
        assertNotNull(found);
        assertTrue(found.size() == 2);
        assertTrue(found.get(0).getUserId().equals(USER));
        assertTrue(found.get(1).getUserId().equals(USER));
    }

    @Test
    public void whenFindByUserId_NotFound() {
        // when
        List<Card> found = cardRepository.findByUserId("wrongUser");
        //then
        assertNotNull(found);
        assertTrue(found.size() == 0);
    }

    @Test
    public void testFindAllByOrderByIdAsc() {
        List<Card> found = cardRepository.findAllByOrderByIdAsc();
        assertNotNull(found);
        assertTrue(!found.isEmpty());
        assertTrue(found.size() >= 2);
        System.out.println(found.get(0).getId());
        System.out.println(found.get(1).getId());
        assertTrue(found.get(0).getId().equals(card1.getId()));
        assertTrue(found.get(1).getId().equals(card2.getId()));
    }

    @Test
    public void whenSave_Success() {
        Card card = new Card(USER,new Currency(CURRENCY_ID,TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);
        Card found = cardRepository.save(card);
        assertNotNull(found);
        assertTrue(found.getCurrency().getName().equals(TEST_CURRENCY));
        assertTrue(found.getBalance().equals(new BigDecimal(0)));
    }

    @Test
    public void whenSave_FailWrongCurrency() {
        Currency currency = currencyRepository.findByName("AAA");
        Card card = new Card(USER,currency,new BigDecimal(0),LAST_UPDATED_BY);
        try{
        Card found = cardRepository.save(card);
        fail();
        } catch(ConstraintViolationException ex){
            assertTrue( ex.getMessage().contains("Card currency must be provided"));
        }
    }

    @Test
    public void whenSave_FailWrongCurrencyLong() {
        Currency currency = currencyRepository.findByName("AAA+++");
        Card card = new Card(USER,currency,new BigDecimal(0),LAST_UPDATED_BY);
        try{
            Card found = cardRepository.save(card);
            fail();
        } catch(ConstraintViolationException ex){
            assertTrue( ex.getMessage().contains("Card currency must be provided"));
        }
    }



    @Test
    public void update_Balance() {
        Optional<Card> found = cardRepository.findById(card1.getId());
        Card updated = found.get();
        updated.setBalance(new BigDecimal(300));
        Card found1 = cardRepository.save(updated);
        assertNotNull(found1);
        assertTrue(found1.getBalance().equals(new BigDecimal(300)));
    }

    @Test
    public void update_BalanceNegative() {
        Optional<Card> found = cardRepository.findById(card2.getId());
        Card updated = found.get();
        updated.setBalance(new BigDecimal(-300));
        Card found1 = cardRepository.save(updated);
        try{
            entityManager.flush();
            fail();
        } catch(ConstraintViolationException ex){
            assertFalse(ex.getConstraintViolations().isEmpty());
            assertTrue(ex.getConstraintViolations().iterator().next().getMessage().contains("must be greater than or equal to 0"));
        }
    }

    @Test
    public void update_BalanceNull() {
        Optional<Card> found = cardRepository.findById(card2.getId());
        Card updated = found.get();
        updated.setBalance(null);
        Card found1 = cardRepository.save(updated);
        try{
            entityManager.flush();
            fail();
        } catch(ConstraintViolationException ex){
            assertFalse(ex.getConstraintViolations().isEmpty());
            System.out.println(ex.getConstraintViolations().iterator().next().getMessage());
            assertTrue(ex.getConstraintViolations().iterator().next().getMessage().contains("Card balance must be provided"));
        }
    }

    @After
    public void after(){
    }

}
