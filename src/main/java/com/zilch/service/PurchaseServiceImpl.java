package com.zilch.service;

import com.zilch.entities.*;
import com.zilch.entities.Currency;
import com.zilch.exceptions.CardException;
import com.zilch.exceptions.ErrorMessage;
import com.zilch.helper.Helper;
import com.zilch.repository.*;
import org.hibernate.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

import static com.zilch.exceptions.ErrorMessage.NUMBER_FORMAT_MISMATCH;

/**
 * Service for managing purchases
 * @author Elena Medvedeva
 */
@Validated
@PropertySource("classpath:application.properties")
@Service
public class PurchaseServiceImpl implements PurchaseService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CardService cardService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private Helper helper;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    public static int TRANSACTIONS_IN_PURCHASE_COUNT = 4;

    @Value("${application.transaction.type.debit}")
    private String transactionTypeDebit;

    @Value("${db.updated_by}")
    private String updatedBy;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public List<Purchase> findAll() throws CardException {
        return purchaseRepository.findAllByOrderByIdAsc();
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public Purchase findById(@NotNull Integer id) throws CardException {
        Optional<Purchase> optionalPurchase =  purchaseRepository.findById(id);
        //validate
        helper.conditionIsTrue(optionalPurchase.isPresent(),String.format(ErrorMessage.NO_PURCHASE_FOUND,id.toString()),HttpStatus.BAD_REQUEST.value());
        return optionalPurchase.get();
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public List<Purchase> getPurchasesByCardId(@NotNull Integer cardId) throws CardException {
        Card card = cardService.findById(cardId);
        if(card != null) {
            return purchaseRepository.findByCard(card);
        } else {
            throw new CardException(String.format(ErrorMessage.NO_CARD_FOUND,cardId.toString()), HttpStatus.BAD_REQUEST.value());
        }
    }

    private List<Transaction>  createFourTransactions(Currency currency, Card card, String globalId, String amount, String description,Purchase purchase, int transactionsInPurchase ) throws CardException {
        try{
            List<Transaction> transactions = new ArrayList<>();
            Date date = new Date();
            BigDecimal transactionAmount = new BigDecimal(amount).divide(new BigDecimal(transactionsInPurchase),BigDecimal.ROUND_HALF_UP);
            TransactionType debitTransactionType = transactionTypeRepository.getOne(transactionTypeDebit);

            //create first transaction which is currently withdrawing the money
            Transaction transaction  = transactionService.createTransaction(globalId + "_0", currency, card, debitTransactionType,transactionAmount.toString(),purchase.getId().toString(), true,date,description );
            transactions.add(transaction);
            for(int i = 1; i < transactionsInPurchase ; i++){
                date = helper.dateInAWeek(date);
                transaction  = transactionService.createTransaction(globalId + "_" + i, currency, card, debitTransactionType,transactionAmount.toString(),purchase.getId().toString(), false,date,description );
                transactions.add(transaction);
            }
            return  transactions;

        } catch(NumberFormatException e){
            String error = String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,amount);
            throw new CardException(error, HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Creates purchase for Zilch Card.
     * @param globalId unique global id
     * @param shopId identifier of a shop
     * @param currencyName valid currency name
     * @param cardId valid card id
     * @param amount purchase amount
     * @param description
     * @return created purchase
     * @throws CardException
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = CardException.class)
    @Override
    public Purchase createPurchase(@NotBlank String globalId, @NotBlank String shopId, @NotBlank String currencyName, @NotBlank String cardId, @NotBlank String amount, String description) throws CardException {
        try {

            //Check for unique purchase globalId happens due to entity constrains on Purchase.globalId (unique=true)

            //Get currency reference
            Currency currency = currencyRepository.findByName(currencyName);
            String error = String.format(ErrorMessage.NO_CURRENCY_PRESENT, currencyName);
            helper.conditionIsTrue(currency != null,error,HttpStatus.BAD_REQUEST.value());

            //Check card is present
            Card card = cardService.findById(Integer.valueOf(cardId));
            error = String.format(ErrorMessage.NO_CARD_FOUND, cardId);
            helper.conditionIsTrue(card != null,error,HttpStatus.BAD_REQUEST.value());

            //check that purchase and card have the same currency
            error = String.format(ErrorMessage.PURCHASE_CURRENCY_NOT_EQ_CARD_CURRENCY,currency.getName(), card.getCurrency().getName());
            helper.conditionIsTrue(card.getCurrency().getId().equals(currency.getId()),error,HttpStatus.BAD_REQUEST.value());

            Purchase purchase =  purchaseRepository.save(new Purchase(globalId,shopId,currency, new BigDecimal(amount),card, description, updatedBy));

            //created four debit transactions and save them
            List<Transaction> transactions = createFourTransactions(currency, card, globalId, amount, description, purchase, TRANSACTIONS_IN_PURCHASE_COUNT );
            purchase.setTransactions(transactions);

            return purchase;
        } catch(NumberFormatException e){
            throw new CardException(String.format(NUMBER_FORMAT_MISMATCH,amount),HttpStatus.BAD_REQUEST.value());
        }
    }
}
