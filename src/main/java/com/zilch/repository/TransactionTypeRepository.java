package com.zilch.repository;

import com.zilch.entities.TransactionType;
import com.zilch.exceptions.CardException;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

/**
 * Transaction type JPA repository
 *  <p> Generates SQL queries to access the database to manage TransactionType entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = CardException.class)
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
