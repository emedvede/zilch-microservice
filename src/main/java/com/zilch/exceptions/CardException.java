package com.zilch.exceptions;

/**
 * Custom card exception
 *
 * @author Elena Medvedeva
 */
public class CardException extends Exception{
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public CardException(String message, int errorCode){
        super(message);
        this.errorCode = errorCode;
    }

    public CardException(){
        super();
    }

    public CardException(String message){
        super(message);
    }

    public CardException(Exception e){
        super(e);
    }

}
