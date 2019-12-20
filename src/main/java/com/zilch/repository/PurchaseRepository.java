package com.zilch.repository;

import com.zilch.entities.Card;
import com.zilch.entities.Purchase;
import com.zilch.entities.Transaction;
import com.zilch.exceptions.CardException;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Purchase JPA repository
 *  <p> Generates SQL queries to access the database to manage Purchase entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = CardException.class)
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByCard(Card card);
    List<Purchase> findAllByOrderByIdAsc();
    //Purchase findByGlobalId(String globalId);
}
