package com.zilch.service;

import com.zilch.entities.Currency;
import com.zilch.entities.Card;
import com.zilch.exceptions.ErrorMessage;
import com.zilch.exceptions.CardException;
import com.zilch.repository.CurrencyRepository;
import com.zilch.repository.TransactionRepository;
import com.zilch.repository.CardRepository;
import com.zilch.helper.Helper;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.validation.annotation.Validated;

import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
/**
 * Service for managing cards
 * @author Elena Medvedeva
 */
@Validated
@PropertySource("classpath:application.properties")
@Service
class CardServiceImpl implements CardService{

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private Helper inputParametersValidator;

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
    public List<Card> findAll() throws CardException {
        return cardRepository.findAllByOrderByIdAsc();
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public Card findById(@NotNull Integer id) throws CardException {
       Optional<Card> optionalcard =  cardRepository.findById(id);
       //validate
       inputParametersValidator.conditionIsTrue(optionalcard.isPresent(),String.format(ErrorMessage.NO_CARD_FOUND,id.toString()),HttpStatus.BAD_REQUEST.value());
       return optionalcard.get();
    }

    @Transactional(rollbackFor = CardException.class)
    @Override
    public List<Card> findByUserId(@NotBlank String userId) throws CardException {
        return cardRepository.findByUserId(userId);
    }

    /**
     * Creates card based on currency.
     * @param userId valid currency id
     * @param currencyName valid currency name
     * @return created card
     * @throws CardException
     */
    @Transactional(rollbackFor = CardException.class)
    @Override
    public Card createCard(@NotBlank String userId, @NotBlank String currencyName) throws CardException{
        try {
            Currency currency = currencyRepository.findByName(currencyName);
            String error = String.format(ErrorMessage.NO_CURRENCY_PRESENT,currencyName);
            inputParametersValidator.conditionIsTrue(currency != null,error,HttpStatus.BAD_REQUEST.value());
            return cardRepository.save(new Card(userId, currency, new BigDecimal(0), updatedBy));
        } catch (ObjectNotFoundException e){
            throw new CardException(String.format(ErrorMessage.NO_CURRENCY_PRESENT,currencyName),HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Updates card balance. Prior to it checks if there is enough funds on card balance.
     * If there is not enough funds, throws CardException
     * If isCredit is set to true, takes absolute amount from  @param amount  and adds it to card balance.
     * If isCredit is set to false, takes absolute amount from  @param amount  and subtracts it from card balance.
     *
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues (in case of deploying application to multiple hosts)
     * This will slow down performance.
     * @param card
     * @param amount
     * @param isCredit
     * @return updated card
     * @throws CardException if couldn't update card balance, e.g. not enough funds.
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = CardException.class)
    @Override
    public Card updateCardAmount(@NotNull Card card, @NotBlank String amount, @NotNull  Boolean isCredit) throws CardException{
        try {
            BigDecimal transactionAmount = (isCredit) ? new BigDecimal(amount).abs() : new BigDecimal(amount).abs().negate();

            //check that there is enough funds on card balance for debit transaction
            Boolean condition = (isCredit || (card.getBalance().compareTo(transactionAmount.abs()) >= 0) );
            inputParametersValidator.conditionIsTrue(condition, String.format(ErrorMessage.NOT_ENOUGH_FUNDS,card.getId(),amount),HttpStatus.BAD_REQUEST.value());

            //update card
            card.setBalance(card.getBalance().add(transactionAmount));
            card.setLastUpdatedBy(updatedBy);
            card.setLastUpdated(new Date());

            return cardRepository.save(card);

        }catch (NumberFormatException e){
            String error = String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,amount);
            throw new CardException(error, HttpStatus.BAD_REQUEST.value());
        }
    }
}
