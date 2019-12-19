package com.zilch.gson.exclusion;

import com.zilch.entities.Card;
import com.zilch.entities.Transaction;

/**
 * Fields to be excluded from serialization when using gson serialization
 *
 * @author Elena Medvedeva
 */
public class ExcludeField {
    public static final String EXCLUDE_CARD = Transaction.class.getCanonicalName()+ ".card";
    public static final String EXCLUDE_TRANSACTIONS = Card.class.getCanonicalName() + ".transactions";

}
