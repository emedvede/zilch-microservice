package com.zilch.service;

import com.zilch.entities.Currency;
import com.zilch.entities.Card;
import com.zilch.entities.Transaction;
import com.zilch.entities.TransactionType;
import com.zilch.exceptions.ErrorMessage;
import com.zilch.exceptions.CardException;
import com.zilch.repository.CurrencyRepository;
import com.zilch.repository.TransactionRepository;
import com.zilch.repository.TransactionTypeRepository;
import com.zilch.helper.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.validation.annotation.Validated;

//import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

import static com.zilch.exceptions.ErrorMessage.NUMBER_FORMAT_MISMATCH;

@Validated
@PropertySource("classpath:application.properties")
@Service
public class TransactionServiceImpl implements TransactionService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private Helper inputParametersValidator;


    @Value("${db.updated_by}")
    private String updatedBy;

    @Value("${application.transaction.type.credit}")
    private String transactionTypeCredit;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getTransactionTypeCredit() {
        return transactionTypeCredit;
    }

    public void setTransactionTypeCredit(String transactionTypeCredit) {
        this.transactionTypeCredit = transactionTypeCredit;
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public List<Transaction> getTransactionsBycardId(@NotNull Integer cardId) throws CardException {
        Card card = cardService.findById(cardId);
        if(card != null) {
            return transactionRepository.findBycard(card);
        } else {
            throw new CardException(String.format(ErrorMessage.NO_CARD_FOUND,cardId.toString()), HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Creates transaction for card.
     * If there is not enough funds on card balance, throws CardException
     * If transactionTypeId='C' (credit transaction), takes absolute amount from  @param amount  and adds it to card balance.
     * If transactionTypeId='D' (debit transaction), takes absolute amount from  @param amount  and subtracts it from card balance.
     * Valid refence to transaction type, currency, card should be provided.
     * Global id should be unique.
     * Transaction should have the same currency as card.
     * No additional SQL query is used to select currency by Id and transaction type by Id
     * because JPARepository.getOne is used, which returns only reference for transaction object.
     *
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues (in case of deploying application to multiple hosts)
     *
     * @param globalId unique global id
     * @param currencyName valid currency name
     * @param cardId valid card id
     * @param transactionTypeId valid transaction type - 'C' or 'D'
     * @param amount transaction amount
     * @param description
     * @return created transaction
     * @throws CardException if couldn't create transaction
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = CardException.class)
    @Override
    public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currencyName, @NotBlank String cardId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws CardException{
        try {
            //Check for unique transaction globalId happens due to entity constrains on Transaction.globalId (unique=true)

            //Get currency reference
            Currency currency = currencyRepository.findByName(currencyName);
            String error = String.format(ErrorMessage.NO_CURRENCY_PRESENT, currencyName);
            inputParametersValidator.conditionIsTrue(currency != null,error,HttpStatus.BAD_REQUEST.value());

            //Get transactionType reference
            TransactionType transactionType = transactionTypeRepository.getOne(transactionTypeId);

            //Check card is present
            Card card = cardService.findById(Integer.valueOf(cardId));
            error = String.format(ErrorMessage.NO_CARD_FOUND, cardId);
            inputParametersValidator.conditionIsTrue(card != null,error,HttpStatus.BAD_REQUEST.value());

            //check that transaction and card have the same currency
            error = String.format(ErrorMessage.TRANSACTION_CURRENCY_NOT_EQ_card_CURRENCY,currency.getName(), card.getCurrency().getName());
            inputParametersValidator.conditionIsTrue(card.getCurrency().getId().equals(currency.getId()),error,HttpStatus.BAD_REQUEST.value());

            //Update card, checks if there is enough funds for debit transaction. If not, throws CardException
            card = cardService.updateCardAmount(card,amount,transactionTypeId.equalsIgnoreCase(transactionTypeCredit));

            //Create transaction
            Transaction transaction = new Transaction(globalId,transactionType,new BigDecimal(amount),card,currency,description,updatedBy);

            return transactionRepository.save(transaction);

        } catch(NumberFormatException e){
            throw new CardException(String.format(NUMBER_FORMAT_MISMATCH,amount),HttpStatus.BAD_REQUEST.value());
        }

    }
}
