package com.test.app.payments.error;

/**
 * Class to handle transaction exceptions
 */
public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(String errorMessage) {
        super(errorMessage);
    }
}
