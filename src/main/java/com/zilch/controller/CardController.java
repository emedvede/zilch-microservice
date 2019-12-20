package com.zilch.controller;

import com.zilch.exceptions.CardException;
import com.zilch.gson.adapter.HibernateProxyTypeAdapter;
import com.zilch.gson.exclusion.ExcludeField;
import com.zilch.gson.exclusion.GsonExclusionStrategy;
import com.zilch.helper.Helper;
import com.zilch.service.CardService;
import com.zilch.view.model.CardModel;
import com.google.gson.GsonBuilder;
import com.zilch.entities.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.util.List;

/**
 * Restful controller for managing cards
 *  @author Elena Medvedeva
 */
@RestController
class CardController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CardService cardService;

    @Autowired
    private Helper inputParametersValidator;

    @GetMapping(
            value = "/test",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public String test() throws CardException, ClassNotFoundException {
        return "Hello from Zilch microservice!";
    }

    private  GsonExclusionStrategy[] getExclusionStrategiesForCard() throws ClassNotFoundException  {
        return new GsonExclusionStrategy[]{new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_PURCHASE),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_CARD),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_TRANSACTIONS),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_PURCHASE_CARD)};

    }

    private  GsonExclusionStrategy[] getExclusionStrategiesForListOfCards() throws ClassNotFoundException  {
        return new GsonExclusionStrategy[]{new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_PURCHASES),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_TRANSACTIONS)};

    }

    @GetMapping(
    value = "/cards",
    produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getAll() throws CardException, ClassNotFoundException {
        logger.debug("Called cardController.getAll");
        return new GsonBuilder().setExclusionStrategies(getExclusionStrategiesForListOfCards())
                .create().toJson(cardService.findAll());
    }

    @GetMapping(
            value = "/cards/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getCardById( @PathVariable("id") int id) throws CardException, ClassNotFoundException {
        logger.debug("Called cardController.getCardById with id={}",id);
        Card card = cardService.findById(id);
        return new GsonBuilder().setExclusionStrategies(getExclusionStrategiesForCard())
                .create().toJson(card);
    }

    @GetMapping(
            value = "/cards/user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getCardsByUserId( @RequestParam("userId") String userId) throws CardException, ClassNotFoundException {
        logger.debug("Called cardController.getCardsByUserId with userId={}",userId);
        List<Card> cards = cardService.findByUserId(userId);
        return new GsonBuilder().setExclusionStrategies(getExclusionStrategiesForListOfCards())
                .create().toJson(cards);
    }

    /**
     * Creates new card.currency must be provided. In the form {"userId":"user",currency":"GBP"}
     * @param cardModel Expecting currency to be set, e. g. {"userId":"user","currency":"GBP"}. Expects cardModel in JSON format.
     * @return new card in JSON format
     * @throws CardException when failed to create card
     */
    @PostMapping(value = "/cards",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createCard(@Valid @RequestBody CardModel cardModel) throws CardException {
        logger.debug("Called cardController.createCard");
        Card card = cardService.createCard(cardModel.getUserId(),cardModel.getCurrency());
        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).create().toJson(card);
    }

}
