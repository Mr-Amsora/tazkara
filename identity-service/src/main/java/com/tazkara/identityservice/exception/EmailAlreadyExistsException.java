package com.tazkara.identityservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
