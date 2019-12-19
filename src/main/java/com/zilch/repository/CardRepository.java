package com.zilch.repository;

import com.zilch.exceptions.CardException;
import com.zilch.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Card JPA repository
 * <p> Generates SQL queries to access the database to manage Card entities</p>
 * @author Elena Medvedeva
 */
@Transactional(rollbackOn = CardException.class)
public interface CardRepository extends JpaRepository<Card, Integer> {
    List<Card> findAllByOrderByIdAsc();
    List<Card> findByUserId(String userId);

}