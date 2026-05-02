package com.codenbugs.adsservice.exceptions;

public class NotFoundException extends ServiceException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
    }
}
