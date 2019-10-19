package com.test.app.payments.service;

import com.test.app.payments.error.TransactionProcessingException;
import com.test.app.payments.model.PaymentRequest;
import com.test.app.payments.model.TransactionType;
import com.test.app.payments.model.UserAccount;
import com.test.app.payments.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;

import static com.test.app.payments.model.TransactionType.BANK_ACCOUNT;
import static com.test.app.payments.model.TransactionType.PAY_ID;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * Payment service implementation to process transactions within same bank. Both the sender and receiver
 * will be from same bank/branch
 */
@Service
public class SameBankPaymentService implements PaymentService {

    @Autowired
    UserAccountRepository accountRepository;

    @Override
    public UserAccount fetchAndValidateSenderAccount(PaymentRequest paymentRequest)
            throws TransactionProcessingException {
        String senderAccountNo = paymentRequest.getSenderAccountNo();
        if (isEmpty(senderAccountNo)) {
            throw new TransactionProcessingException("Invalid sender account information");
        }
        UserAccount senderAccount = accountRepository.findUserByAccountNo(senderAccountNo);
        if (senderAccount == null) {
            throw new TransactionProcessingException("Sender account not registered with bank");
        }
        return senderAccount;
    }

    @Override
    public UserAccount fetchAndValidateReceiverAccount(PaymentRequest paymentRequest)
            throws TransactionProcessingException {
        TransactionType transactionType = getTransactionType(paymentRequest);
        String receiverAccountInfo = paymentRequest.getReceiverAccountInfo();
        if (isEmpty(receiverAccountInfo)) {
            throw new TransactionProcessingException("Invalid receiver account information");
        }
        UserAccount receiverAccount = getReceiverAccount(transactionType, receiverAccountInfo);
        if (receiverAccount == null) {
            throw new TransactionProcessingException("Receiver " + transactionType + " not registered with bank");
        }
        return receiverAccount;
    }

    private TransactionType getTransactionType(PaymentRequest paymentRequest) throws TransactionProcessingException {
        try {
            return TransactionType.valueOf(paymentRequest.getTransactionType());
        } catch (IllegalArgumentException ex) {
            throw new TransactionProcessingException("Unknown transaction type: " + paymentRequest.getTransactionType());
        }
    }

    private UserAccount getReceiverAccount(TransactionType transactionType, String receiverAccount) {
        if (transactionType == BANK_ACCOUNT) {
            return accountRepository.findUserByAccountNo(receiverAccount);
        } else if (transactionType == PAY_ID) {
            return accountRepository.findUserByPayId(receiverAccount);
        }
        return null;
    }

    @Override
    public boolean checkIfAmountValid(PaymentRequest paymentRequest, UserAccount senderAccount)
            throws TransactionProcessingException {
        try {
            BigDecimal amountToTransfer = new BigDecimal(paymentRequest.getTransactionAmount());
            if (amountToTransfer.signum() <= 0) {
                throw new TransactionProcessingException("Amount to be transferred is invalid");
            }
            return hasEnoughBalance(senderAccount.getAccountBalance(), amountToTransfer);
        } catch (NumberFormatException e) {
            throw new TransactionProcessingException("Amount to be transferred is invalid");
        }
    }

    private boolean hasEnoughBalance(BigDecimal accountBalance, BigDecimal amountToTransfer) {
        return accountBalance.compareTo(amountToTransfer) >= 0;
    }

    @Override
    public void processTransaction(@Valid PaymentRequest paymentRequest, UserAccount senderAccount,
                                   UserAccount receiverAccount) throws TransactionProcessingException {
        BigDecimal amountToTransfer = new BigDecimal(paymentRequest.getTransactionAmount());
        senderAccount.setAccountBalance(senderAccount.getAccountBalance().subtract(amountToTransfer));
        accountRepository.save(senderAccount);
        receiverAccount.setAccountBalance(receiverAccount.getAccountBalance().add(amountToTransfer));
        accountRepository.save(receiverAccount);
    }
}