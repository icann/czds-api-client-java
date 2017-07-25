package org.icann.czds.sdk.model;

public class AuthenticationException extends Exception{

    public AuthenticationException(String message, Throwable e){
        super(message, e);
    }

    public AuthenticationException(String message){
        super(message);
    }

    public AuthenticationException(Throwable e){
        super(e);
    }
}
