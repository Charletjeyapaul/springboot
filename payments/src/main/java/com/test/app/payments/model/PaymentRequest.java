package com.test.app.payments.model;

/**
 * POJO for handling received payment request
 */
public class PaymentRequest {

    private String senderAccountNo;
    private String transactionType;
    private String receiverAccountInfo;
    private String transactionAmount;

    public PaymentRequest() {
    }

    public PaymentRequest(String senderAccountNo, String transactionType, String receiverAccountInfo,
                          String transactionAmount) {
        this.senderAccountNo = senderAccountNo;
        this.transactionType = transactionType;
        this.receiverAccountInfo = receiverAccountInfo;
        this.transactionAmount = transactionAmount;
    }

    public String getSenderAccountNo() {
        return senderAccountNo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getReceiverAccountInfo() {
        return receiverAccountInfo;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }
}
