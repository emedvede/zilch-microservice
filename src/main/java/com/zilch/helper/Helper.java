package com.zilch.helper;

import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Helper to check that condition is TRUE and generate new date in a week
 *
 * @author Elena Medvedeva
 */
public interface Helper {
    public void conditionIsTrue(@NotNull Boolean condition, @NotNull String errorMessage, int errorCode) throws CardException;
    public Date dateInAWeek(Date date);
}
