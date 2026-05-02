package com.codenbugs.adsservice.exceptions;

public class DuplicatedEntityException extends ServiceException {
    public DuplicatedEntityException(String message) {
        super(message);
    }

    public DuplicatedEntityException() {
    }
}
