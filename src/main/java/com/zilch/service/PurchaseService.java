package com.zilch.service;

import com.zilch.entities.Card;
import com.zilch.entities.Currency;
import com.zilch.entities.Purchase;
import com.zilch.entities.Transaction;
import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for managing purchases
 * @author Elena Medvedeva
 */
public interface PurchaseService {
    public List<Purchase> findAll() throws CardException;
    public List<Purchase> getPurchasesByCardId(@NotNull Integer cardId) throws CardException;
    public Purchase findById(@NotNull Integer id) throws CardException;
    public Purchase createPurchase(@NotBlank String globalId,@NotBlank String shopId, @NotBlank  String currencyName, @NotBlank String cardId, @NotBlank String amount, String description) throws CardException;
}
