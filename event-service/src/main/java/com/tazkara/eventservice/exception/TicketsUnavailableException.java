package com.tazkara.eventservice.exception;

public class TicketsUnavailableException extends RuntimeException {

    public TicketsUnavailableException(String ticketTypeName) {
        super("Not enough tickets available for " + ticketTypeName);
    }
}