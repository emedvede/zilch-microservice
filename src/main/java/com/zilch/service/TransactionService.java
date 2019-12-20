package com.zilch.service;

import com.zilch.entities.Card;
import com.zilch.entities.Currency;
import com.zilch.entities.Transaction;
import com.zilch.entities.TransactionType;
import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Service for managing transactions
 * @author Elena Medvedeva
 */
public interface TransactionService {
    public List<Transaction> getTransactionsByCardId(@NotNull Integer cardId) throws CardException;
    public Transaction createTransaction(@NotBlank String globalId, @NotNull Currency currency, @NotNull Card card, @NotNull TransactionType transactionType, @NotBlank String amount, @NotBlank String purchaseId, Boolean submitted, Date dueDate, String description) throws CardException;
    public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currencyName, @NotBlank String cardId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws CardException;

}
