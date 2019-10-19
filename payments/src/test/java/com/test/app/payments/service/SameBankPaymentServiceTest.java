package com.test.app.payments.service;

import com.test.app.payments.error.TransactionProcessingException;
import com.test.app.payments.model.PaymentRequest;
import com.test.app.payments.model.UserAccount;
import com.test.app.payments.repository.UserAccountRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.test.app.payments.model.TransactionType.BANK_ACCOUNT;
import static com.test.app.payments.model.TransactionType.PAY_ID;
import static java.lang.Integer.parseInt;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

// Data from H2 repository(resource/data.sql) is used for testing
@RunWith(SpringRunner.class)
@SpringBootTest
public class SameBankPaymentServiceTest {

    private static final String SENDER_ACCOUNT = "12400111111111";
    private static final String RECEIVER_ACCOUNT = "12400133333333";
    private static final String RECEIVER_PAY_ID = "0411000111";
    private static final String AMOUNT = "10";

    @Autowired
    UserAccountRepository accountRepository;

    @Autowired
    SameBankPaymentService paymentService;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldFetchSenderAccountCorrectly() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        UserAccount userAccount = paymentService.fetchAndValidateSenderAccount(paymentRequest);
        assertThat(userAccount.getAccountNo()).isEqualTo(SENDER_ACCOUNT);
    }

    @Test
    public void shouldThrowErrorIfSenderAccountEmpty() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Invalid sender account information");

        PaymentRequest paymentRequest = buildSamplePaymentRequest(EMPTY, BANK_ACCOUNT.name(), RECEIVER_ACCOUNT, AMOUNT);
        paymentService.fetchAndValidateSenderAccount(paymentRequest);
    }

    @Test
    public void shouldThrowErrorIfSenderAccountNotPresent() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Sender account not registered with bank");

        PaymentRequest paymentRequest = buildSamplePaymentRequest("INVALID_NO", BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        paymentService.fetchAndValidateSenderAccount(paymentRequest);
    }

    @Test
    public void shouldFetchReceiverAccountCorrectly() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        UserAccount userAccount = paymentService.fetchAndValidateReceiverAccount(paymentRequest);
        assertThat(userAccount.getAccountNo()).isEqualTo(RECEIVER_ACCOUNT);
    }

    @Test
    public void shouldThrowErrorIfReceiverAccountEmpty() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Invalid receiver account information");

        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(), EMPTY, AMOUNT);
        paymentService.fetchAndValidateReceiverAccount(paymentRequest);
    }

    @Test
    public void shouldThrowErrorIfReceiverAccountNotPresent() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Receiver " + BANK_ACCOUNT + " not registered with bank");

        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                "INVALID_NO", AMOUNT);
        paymentService.fetchAndValidateReceiverAccount(paymentRequest);
    }

    @Test
    public void shouldThrowErrorForUnknownTransactionType() {
        String transactionType = "INVALID";
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Unknown transaction type: " + transactionType);

        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, transactionType,
                RECEIVER_ACCOUNT, AMOUNT);
        paymentService.fetchAndValidateReceiverAccount(paymentRequest);
    }

    @Test
    public void shouldFetchPayIdCorrectlyForReceiverAccount() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, PAY_ID.name(),
                RECEIVER_PAY_ID, AMOUNT);
        UserAccount userAccount = paymentService.fetchAndValidateReceiverAccount(paymentRequest);
        assertThat(userAccount.getPayId()).isEqualTo(RECEIVER_PAY_ID);
    }

    @Test
    public void shouldCheckAndValidateAmountCorrectly() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        UserAccount userAccount = paymentService.fetchAndValidateSenderAccount(paymentRequest);
        assertThat(paymentService.checkIfAmountValid(paymentRequest, userAccount)).isTrue();
    }

    @Test
    public void shouldReturnFalseIfNotEnoughBalanceInSenderAccount() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, "100000000");
        UserAccount userAccount = paymentService.fetchAndValidateSenderAccount(paymentRequest);
        assertThat(paymentService.checkIfAmountValid(paymentRequest, userAccount)).isFalse();
    }

    @Test
    public void shouldThrowErrorIfAmountIsInvalid() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Amount to be transferred is invalid");

        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, "INVALID_AMOUNT");
        UserAccount userAccount = new UserAccount();
        paymentService.checkIfAmountValid(paymentRequest, userAccount);
    }

    @Test
    public void shouldThrowErrorIfAmountIsNegative() {
        expectedEx.expect(TransactionProcessingException.class);
        expectedEx.expectMessage("Amount to be transferred is invalid");

        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, "-1");
        UserAccount userAccount = new UserAccount();
        paymentService.checkIfAmountValid(paymentRequest, userAccount);
    }

    @Test
    public void shouldProcessTransactionCorrectly() {
        PaymentRequest paymentRequest = buildSamplePaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        UserAccount senderAccount = paymentService.fetchAndValidateSenderAccount(paymentRequest);
        UserAccount receiverAccount = paymentService.fetchAndValidateReceiverAccount(paymentRequest);
        int senderInitialAccountBalance = senderAccount.getAccountBalance().intValue();
        int receiverInitialAccountBalance = receiverAccount.getAccountBalance().intValue();

        paymentService.processTransaction(paymentRequest, senderAccount, receiverAccount);
        assertThat(senderAccount.getAccountBalance().intValue()).isEqualTo(senderInitialAccountBalance - parseInt(AMOUNT));
        assertThat(receiverAccount.getAccountBalance().intValue()).isEqualTo(receiverInitialAccountBalance + parseInt(AMOUNT));
    }

    private PaymentRequest buildSamplePaymentRequest(String senderAccount, String transactionType,
                                                     String receiverAccount, String amount) {
        return new PaymentRequest(senderAccount, transactionType, receiverAccount, amount);
    }
}