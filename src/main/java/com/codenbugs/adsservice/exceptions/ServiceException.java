package com.codenbugs.adsservice.exceptions;

public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException() {
    }
}
