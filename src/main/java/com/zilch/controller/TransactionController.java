package com.zilch.controller;

import com.zilch.entities.Transaction;
import com.zilch.exceptions.CardException;
import com.zilch.gson.adapter.HibernateProxyTypeAdapter;
import com.zilch.gson.exclusion.ExcludeField;
import com.zilch.gson.exclusion.GsonExclusionStrategy;
import com.zilch.helper.Helper;
import com.zilch.service.TransactionService;
import com.zilch.view.model.TransactionModel;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Restful controller for managing card transactions
 *
 * @author Elena Medvedeva
 */
@RestController
public class TransactionController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private Helper inputParametersValidator;

    private  GsonExclusionStrategy[] getExclusionStrategies() throws ClassNotFoundException  {
        return new GsonExclusionStrategy[]{
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_CARD),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_PURCHASE_TRANSACTIONS),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_PURCHASE_CARD)};

    }

    @GetMapping(
            value = "/cards/{id}/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getCardTransactionsById( @PathVariable("id") int id) throws CardException, ClassNotFoundException {
        logger.debug("Called TransactionController.getCardTransactionsById with parameter cardId={}",id);
        List<Transaction> transactionList = transactionService.getTransactionsByCardId(id);
        return new GsonBuilder().
                setExclusionStrategies(getExclusionStrategies()).
                create().toJson(transactionList);

    }

    /**
     * Creates card transaction.
     * <p>
     * Example of  credit transaction JSON body
     * {"globalId":"123","currency":"GBP","cardId": "1","transactionTypeId":"C","amount":"100","description":"add money"}
     *
     * Example of debit transaction JSON body
     * {"globalId":"123","currency":"GBP","cardId": "1","transactionTypeId":"D","amount":"100","description":"withdraw money"}
     * </p>
     * @param transactionModel contains input parameters in the following format:
     *                {"globalId":"123","currency":"GBP","cardId": "1","transactionTypeId":"C","amount":"100","description":"add money"}
     * @return created transaction in JSON format
     * @throws CardException when couldn't create transaction (e.g. globalId not unique, not enough funds on card balance, etc.)
     * @throws ClassNotFoundException
     */

    @PostMapping(
            value = "/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String createCardTransaction(@Valid @RequestBody TransactionModel transactionModel) throws CardException, ClassNotFoundException {
        logger.debug("Called TransactionController.createCardTransaction" );


        Transaction transaction = transactionService.createTransaction(transactionModel.getGlobalId(),transactionModel.getCurrency(),transactionModel.getcardId(),
                transactionModel.getTransactionTypeId(),transactionModel.getAmount(),transactionModel.getDescription());
        logger.info("Transaction created with id=" + transaction.getId() );

        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).
                setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_TRANSACTIONS),new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_PURCHASE)).
                create().toJson(transaction);
    }
}
