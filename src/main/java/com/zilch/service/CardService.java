package com.zilch.service;

import com.zilch.entities.Card;
import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for managing cards
 * @author Elena Medvedeva
 */
public interface CardService {
    public List<Card> findAll() throws CardException;
    public Card findById(@NotNull Integer id) throws CardException;
    public List<Card> findByUserId(@NotBlank String userId) throws CardException;
    public Card createCard(@NotBlank String userId, @NotBlank String currencyName) throws CardException;
    public Card updateCardAmount(@NotNull Card card, @NotBlank String amount, @NotNull Boolean isCredit) throws CardException;

}