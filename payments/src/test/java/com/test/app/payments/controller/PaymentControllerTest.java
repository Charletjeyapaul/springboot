package com.test.app.payments.controller;

import com.test.app.payments.error.TransactionProcessingException;
import com.test.app.payments.model.PaymentRequest;
import com.test.app.payments.model.UserAccount;
import com.test.app.payments.service.SameBankPaymentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import static com.test.app.payments.model.TransactionType.BANK_ACCOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static sun.plugin2.util.PojoUtil.toJson;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    private static final String SENDER_ACCOUNT = "12400133333333";
    private static final String RECEIVER_ACCOUNT = "12400111111111";
    private static final String AMOUNT = "1";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SameBankPaymentService paymentService;

    @Test
    public void shouldProcessValidRequestCorrectly() throws Exception {
        PaymentRequest request = new PaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(), RECEIVER_ACCOUNT, AMOUNT);
        mockUserAccounts();
        when(paymentService.checkIfAmountValid(isA(PaymentRequest.class), isA(UserAccount.class))).thenReturn(true);
        RequestBuilder requestBuilder = post("/transfer").contentType(APPLICATION_JSON).content(toJson(request));
        MvcResult result = mvc.perform(requestBuilder).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("Transaction Successful!!");
        assertThat(result.getResponse().getStatus()).isEqualTo(OK.value());
    }

    @Test
    public void shouldGiveErrorResponseIfInsufficientBalance() throws Exception {
        PaymentRequest request = new PaymentRequest(SENDER_ACCOUNT, BANK_ACCOUNT.name(), RECEIVER_ACCOUNT, AMOUNT);
        mockUserAccounts();
        when(paymentService.checkIfAmountValid(isA(PaymentRequest.class), isA(UserAccount.class))).thenReturn(false);
        RequestBuilder requestBuilder = post("/transfer").contentType(APPLICATION_JSON).content(toJson(request));
        MvcResult result = mvc.perform(requestBuilder).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("Insufficient account balance");
        assertThat(result.getResponse().getStatus()).isEqualTo(PRECONDITION_FAILED.value());
    }

    @Test
    public void shouldGiveErrorResponseTransactionProcessingFailed() throws Exception {
        PaymentRequest request = new PaymentRequest("UNKNOWN_SENDER", BANK_ACCOUNT.name(),
                RECEIVER_ACCOUNT, AMOUNT);
        when(paymentService.fetchAndValidateSenderAccount(isA(PaymentRequest.class)))
                .thenThrow(new TransactionProcessingException("Sender account not registered with bank"));
        RequestBuilder requestBuilder = post("/transfer").contentType(APPLICATION_JSON).content(toJson(request));
        MvcResult result = mvc.perform(requestBuilder).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("Sender account not registered with bank");
        assertThat(result.getResponse().getStatus()).isEqualTo(BAD_REQUEST.value());
    }

    private void mockUserAccounts() {
        UserAccount senderAccount = mock(UserAccount.class);
        UserAccount receiverAccount = mock(UserAccount.class);
        when(paymentService.fetchAndValidateSenderAccount(isA(PaymentRequest.class))).thenReturn(senderAccount);
        when(paymentService.fetchAndValidateReceiverAccount(isA(PaymentRequest.class))).thenReturn(receiverAccount);
    }
}