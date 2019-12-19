package com.zilch.helper;

import com.zilch.exceptions.CardException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Helper to check that some condition is TRUE and throw exception otherwise.
 * @author Elena Medvedeva
 */
@Validated
@Component
public class HelperImpl implements Helper<String,String> {

    /**
     * Throws CardException with errorMessage and errorCode if condition is not true
     * @param condition
     * @param errorMessage
     * @param errorCode
     * @throws CardException
     */
    @Override
    public void conditionIsTrue(@NotNull Boolean condition, @NotNull String errorMessage, int errorCode) throws CardException{
        if(!condition){
            throw new CardException(errorMessage, errorCode);
        }
    }
}
