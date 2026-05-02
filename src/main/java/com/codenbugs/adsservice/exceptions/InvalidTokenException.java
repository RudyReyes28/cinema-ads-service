package com.codenbugs.adsservice.exceptions;

public class InvalidTokenException extends ServiceException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
    }
}
