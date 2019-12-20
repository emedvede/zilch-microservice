package com.zilch.controller;

import com.google.gson.GsonBuilder;
import com.zilch.entities.Purchase;
import com.zilch.exceptions.CardException;
import com.zilch.gson.adapter.HibernateProxyTypeAdapter;
import com.zilch.gson.exclusion.ExcludeField;
import com.zilch.gson.exclusion.GsonExclusionStrategy;
import com.zilch.helper.Helper;
import com.zilch.service.PurchaseService;
import com.zilch.view.model.PurchaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Restful controller for managing card transactions
 *
 * @author Elena Medvedeva
 */
@RestController
public class PurchaseController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private Helper inputParametersValidator;



    private  GsonExclusionStrategy[] getExclusionStrategies() throws ClassNotFoundException  {
        return new GsonExclusionStrategy[]{new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_PURCHASE),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTION_CARD),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_TRANSACTIONS),
                new GsonExclusionStrategy(ExcludeField.EXCLUDE_CARD_PURCHASES)};

    }

    @GetMapping(
            value = "/purchases",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getAll() throws CardException, ClassNotFoundException {
        logger.debug("Called purchaseController.getAll");
        List<Purchase> purchasesList = purchaseService.findAll();
        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).setExclusionStrategies(getExclusionStrategies())
                .create().toJson(purchasesList);
    }

    @GetMapping(
            value = "/cards/{id}/purchases",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getCardPurchasesById( @PathVariable("id") int id) throws CardException, ClassNotFoundException {
        logger.debug("Called PurchasesController.getCardPurchasesById with parameter cardId={}",id);
        List<Purchase> purchasesList = purchaseService.getPurchasesByCardId(id);
        return new GsonBuilder().
                setExclusionStrategies(getExclusionStrategies()).
                create().toJson(purchasesList);
    }

    /**
     * Creates purchase against Zilch card.
     * <p>
     * Example of purchase JSON body
     * {"globalId":"123","currency":"GBP","cardId": "1","transactionTypeId":"C","amount":"100","description":"add money"}
     * </p>
     * @param purchaseModel contains input parameters in the following format:
     *                {"globalId":"123","shopId": "ZARA","currency":"GBP","cardId": "1","amount":"100","description":"bought trousers and skirt"}
     * @return created purchase in JSON format
     * @throws CardException when couldn't create transaction (e.g. globalId not unique, not enough funds on card balance, etc.)
     * @throws ClassNotFoundException
     */

    @PostMapping(
            value = "/purchases",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String createPurchase(@Valid @RequestBody PurchaseModel purchaseModel) throws CardException, ClassNotFoundException {
        logger.debug("Called PurchasesController.createPurchase" );


        Purchase purchase = purchaseService.createPurchase(purchaseModel.getGlobalId(),purchaseModel.getShopId(),purchaseModel.getCurrency(),
               purchaseModel.getCardId(),purchaseModel.getAmount(),purchaseModel.getDescription());
        logger.info("Purchase created with id=" + purchase.getId() );

        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).
                setExclusionStrategies(getExclusionStrategies()).
                create().toJson(purchase);
    }
}
