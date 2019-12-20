package com.zilch.exceptions;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Class to store custom error messages.
 *  Also generates some error messages based on exception messages
 *
 *  @author Elena Medvedeva
 */
public class ErrorMessage {
    //Messages to display to user
    public static final String NO_CURRENCY_PRESENT = "No currency %s exists in the system.";
    public static final String MALFORMED_CURRENCY = "Field currency is invalid.";
    public static final String NO_CARD_FOUND = "No card with id %s exists in the system.";
    public static final String NO_PURCHASE_FOUND = "No purchase with id %s exists in the system.";
    public static final String ARGUMENT_TYPE_MISMATCH = "%s should be of type %s";
    public static final String NO_CURRENCY = "No field 'currency' provided";
    public static final String TRANSACTION_WITH_GLOBAL_ID_PRESENT = "Transaction with globalId=%s already present.";
    public static final String PURCHASE_WITH_GLOBAL_ID_PRESENT = "Purchase with globalId=%s already present.";
    public static final String NO_TRANSACTION_TYPE_PRESENT = "Undefined transactionType %s.";
    public static final String NUMBER_FORMAT_MISMATCH = "'%s' should be a number";
    public static final String NOT_ENOUGH_FUNDS = "Card %d has not enough funds to perform debit transaction with amount %s";
    public static final String PART_NO_MANDATORY_FIELD = " is mandatory. It should be provided and can't be empty.";
    public static final String NO_MANDATORY_FIELD = "Field %s" + PART_NO_MANDATORY_FIELD;
    public static final String TRANSACTION_CURRENCY_NOT_EQ_CARD_CURRENCY = "Transaction can't be saved. Transaction currency %s differs from card currency %s.";
    public static final String PURCHASE_CURRENCY_NOT_EQ_CARD_CURRENCY = "Purchase can't be saved. Purchase currency %s differs from card currency %s.";

    //Template messages to compare
    public static final String DUPLICATE_KEY_TRANSACTION = "duplicate key value violates unique constraint \"transaction_global_id_key\"";
    public static final String DUPLICATE_KEY_PURCHASE = "duplicate key value violates unique constraint \"purchase_global_id_key\"";
    public static final String CURRENCY_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_currency_id_fkey\"";
    public static final String CURRENCY_FK_VIOLATES_CARD = "violates foreign key constraint \"card_currency_id_fkey\"";
    public static final String PURCHASE_FK_VIOLATES_CARD = "violates foreign key constraint \"purchase_currency_id_fkey\"";
    public static final String CURRENCY_TOO_LONG = "value too long";
    public static final String TYPE_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_type_id_fkey\"";
    public static final String CARD_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_card_id_fkey\"";

    public static final String [][] ERRORS = {
            {DUPLICATE_KEY_TRANSACTION,                         TRANSACTION_WITH_GLOBAL_ID_PRESENT},
            {DUPLICATE_KEY_PURCHASE,                         PURCHASE_WITH_GLOBAL_ID_PRESENT},
            {CURRENCY_FK_VIOLATES_TRANSACTION,      NO_CURRENCY_PRESENT },
            {CURRENCY_FK_VIOLATES_CARD,           NO_CURRENCY_PRESENT},
            {PURCHASE_FK_VIOLATES_CARD,           NO_CURRENCY_PRESENT},
            {CURRENCY_TOO_LONG,                     MALFORMED_CURRENCY},
            {TYPE_FK_VIOLATES_TRANSACTION,          NO_TRANSACTION_TYPE_PRESENT},
            {CARD_FK_VIOLATES_TRANSACTION,        NO_CARD_FOUND}
    };

    /**
     * Generates error message besed on DataIntegrityViolationException error message using ErrorMessage.ERRORS
     * @param errorMessage from exception from DataIntegrityViolationException
     * @return Generated message for user
     */
    public static String generateErrorMessageForDataIntegrityViolationException(String errorMessage){
        String bodyOfResponse = errorMessage;
        //create map of array ERRORS
         Map<String, String> errors =
                Arrays.stream(ERRORS).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
         //check if error message contains any of ERRORS keys and generate corresponding response
         for(String key:errors.keySet()){
             if(errorMessage.contains(key)){
                 if(!key.equals(CURRENCY_TOO_LONG)) {
                     String id = getId(errorMessage);
                     bodyOfResponse = String.format(errors.get(key),id);
                 } else {
                     bodyOfResponse = errors.get(key);
                 }
             }
         }
         return bodyOfResponse;
    }


    private static String getId(String error){
        return error.substring(error.lastIndexOf("(") + 1,error.lastIndexOf(")"));
    }

}
