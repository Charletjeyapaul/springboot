package com.test.app.payments.service;

import com.test.app.payments.model.PaymentRequest;
import com.test.app.payments.model.UserAccount;

import javax.validation.Valid;

/**
 * Class to handle payment request.
 */
public interface PaymentService {

    /**
     * Sender account information will be fetched and validated
     *
     * @param paymentRequest paymentRequest
     * @return sender userAccount
     */
    UserAccount fetchAndValidateSenderAccount(PaymentRequest paymentRequest);

    /**
     * Receiver account information will be fetched and validated. Receiver account will be fetched based on
     * {@link com.test.app.payments.model.TransactionType} )
     *
     * @param paymentRequest paymentRequest
     * @return receiver userAccount
     */
    UserAccount fetchAndValidateReceiverAccount(PaymentRequest paymentRequest);

    /**
     * This method will validate the amount to be transferred.
     *
     * @param paymentRequest payment request
     * @param senderAccount  sender user account
     * @return true if valid
     */
    boolean checkIfAmountValid(PaymentRequest paymentRequest, UserAccount senderAccount);

    /**
     * This method will process the transaction. Amount will be deducted from sender account and credited to
     * receiver account.
     *
     * @param paymentRequest  payment request
     * @param senderAccount   sender user account
     * @param receiverAccount receiver account
     */
    void processTransaction(@Valid PaymentRequest paymentRequest, UserAccount senderAccount,
                            UserAccount receiverAccount);
}
