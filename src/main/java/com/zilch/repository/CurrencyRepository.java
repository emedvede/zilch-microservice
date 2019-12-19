package com.zilch.repository;

import com.zilch.entities.Currency;
import com.zilch.exceptions.CardException;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

/**
 * Currency JPA repository
 * <p> Generates SQL queries to access the database to manage Currency entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = CardException.class)
public interface CurrencyRepository  extends JpaRepository<Currency, Integer> {
    Currency findByName(String name);
}