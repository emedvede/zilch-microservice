package com.zilch.service;

import com.zilch.entities.Transaction;
import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for managing transactions
 * @author Elena Medvedeva
 */
public interface TransactionService {
    public List<Transaction> getTransactionsBycardId(@NotNull Integer cardId) throws CardException;
    public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currencyName, @NotBlank String cardId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws CardException;

}
