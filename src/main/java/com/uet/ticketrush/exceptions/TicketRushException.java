package com.uet.ticketrush.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter @Setter
public class TicketRushException extends RuntimeException {
    private final HttpStatus status;

    public TicketRushException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}