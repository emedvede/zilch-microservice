package com.zilch.view.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.zilch.exceptions.ErrorMessage.PART_NO_MANDATORY_FIELD;

/**
 * @author Elena Medvedeva
 */
public class PurchaseModel {

    @NotBlank(message = "Field globalId" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field globalId" + PART_NO_MANDATORY_FIELD)
    private String globalId;

    @NotBlank(message = "Field currency" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field currency" + PART_NO_MANDATORY_FIELD)
    private String currency;

    @NotBlank(message = "Field cardId" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field cardId" + PART_NO_MANDATORY_FIELD)
    private String cardId;

    @NotBlank(message = "Field amount" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field amount" + PART_NO_MANDATORY_FIELD)
    private String amount;

    @NotBlank(message = "Field shopId" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field shopId" + PART_NO_MANDATORY_FIELD)
    private String shopId;

    private String description;

    public PurchaseModel(){}

    public PurchaseModel(String globalId,String currency,String cardId, String amount, String shopId, String description){
        this.globalId = globalId;
        this.currency = currency;
        this.cardId = cardId;
        this.amount = amount;
        this.shopId = shopId;
        this.description = description;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
