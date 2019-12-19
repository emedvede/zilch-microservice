package com.zilch.helper;

import com.zilch.exceptions.CardException;

import javax.validation.constraints.NotNull;

/**
 * Helper to check that condition is TRUE.
 * @param <K>
 * @param <V>
 *
 * @author Elena Medvedeva
 */
public interface Helper<K,V> {
    public void conditionIsTrue(@NotNull Boolean condition, @NotNull String errorMessage, int errorCode) throws CardException;

}
