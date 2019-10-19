package com.test.app.payments.controller;

import com.test.app.payments.error.TransactionProcessingException;
import com.test.app.payments.model.PaymentRequest;
import com.test.app.payments.model.UserAccount;
import com.test.app.payments.service.SameBankPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class PaymentController {

    @Autowired
    SameBankPaymentService paymentService;

    @RequestMapping("/healthCheck")
    public String index() {
        return "1";
    }

    @PostMapping(path = "/transfer", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> doPayment(@RequestBody @Valid PaymentRequest paymentRequest) {
        try {
            UserAccount senderAccount = paymentService.fetchAndValidateSenderAccount(paymentRequest);
            UserAccount receiverAccount = paymentService.fetchAndValidateReceiverAccount(paymentRequest);
            if (paymentService.checkIfAmountValid(paymentRequest, senderAccount)) {
                paymentService.processTransaction(paymentRequest, senderAccount, receiverAccount);
                return status(OK).body("Transaction Successful!!");
            } else {
                return status(PRECONDITION_FAILED).body("Insufficient account balance");
            }
        } catch (TransactionProcessingException e) {
            return status(BAD_REQUEST).body(e.getMessage());
        }
    }
}
