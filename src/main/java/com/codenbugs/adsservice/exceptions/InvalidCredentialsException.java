package com.codenbugs.adsservice.exceptions;

public class InvalidCredentialsException extends ServiceException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
    }
}
