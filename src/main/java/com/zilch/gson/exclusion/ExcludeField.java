package com.zilch.gson.exclusion;

import com.zilch.entities.Card;
import com.zilch.entities.Purchase;
import com.zilch.entities.Transaction;

/**
 * Fields to be excluded from serialization when using gson serialization
 *
 * @author Elena Medvedeva
 */
public class ExcludeField {

    public static final String EXCLUDE_TRANSACTION_CARD = Transaction.class.getCanonicalName()+ ".card";
    public static final String EXCLUDE_CARD_TRANSACTIONS = Card.class.getCanonicalName() + ".transactions";
    public static final String EXCLUDE_CARD_PURCHASES = Card.class.getCanonicalName() + ".purchases";
    public static final String EXCLUDE_TRANSACTION_PURCHASE = Transaction.class.getCanonicalName()+ ".purchase";
    public static final String EXCLUDE_PURCHASE_TRANSACTIONS = Purchase.class.getCanonicalName() + ".transactions";
    public static final String EXCLUDE_PURCHASE_CARD = Purchase.class.getCanonicalName() + ".card";

}
