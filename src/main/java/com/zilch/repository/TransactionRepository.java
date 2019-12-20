package com.zilch.repository;

import com.zilch.entities.Transaction;
import com.zilch.exceptions.CardException;
import com.zilch.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Transaction JPA repository
 *  <p> Generates SQL queries to access the database to manage Transaction entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = CardException.class)
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByCard(Card card);
    Transaction findByGlobalId(String globalId);
}
