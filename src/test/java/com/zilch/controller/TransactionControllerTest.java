package com.zilch.controller;

import com.zilch.entities.Currency;
import com.zilch.entities.Transaction;
import com.zilch.entities.TransactionType;
import com.zilch.service.TransactionService;
import com.zilch.helper.Helper;
import com.zilch.helper.HelperImpl;
import com.google.gson.GsonBuilder;
import com.zilch.entities.Card;
import com.zilch.exceptions.ErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TransactionController tests
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    @TestConfiguration
    static class cardControllerTestContextConfiguration {
        @Bean
        public Helper validator() {
            return new HelperImpl();
        }

    }

    public static final Integer CURRENCY_ID = 1;
    public static final String TEST_CURRENCY = "GBP";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";


    public static final String CREDIT = "C";


    @Autowired
    private MockMvc mvc;

    @MockBean
    private TransactionService service;

    private Currency currency;
    private Card card;
    private Transaction transactionCredit;
    private TransactionType typeCredit;
    static int globalIdCounter = 1;

    @Before
    public void before(){
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY );
        card = new Card(USER,new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);
        card.setId(1);
        typeCredit = new TransactionType(CREDIT,"credit trn", LAST_UPDATED_BY);
        transactionCredit = new Transaction(String.valueOf(globalIdCounter++) ,typeCredit,new BigDecimal(20),card,null, currency,"Credit transaction");
        transactionCredit.setId(5);
    }

    @Test
    public void testGetcardTransactionsById_whenGetTransaction_thenReturnJsonArray() throws Exception {
        List<Transaction> allTransactions = Arrays.asList(transactionCredit);

        given(service.getTransactionsByCardId(card.getId())).willReturn(allTransactions);

        mvc.perform(get("/cards/" + card.getId()+ "/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(transactionCredit.getId())))
                .andExpect(jsonPath("$[0].globalId", is(transactionCredit.getGlobalId())))
                .andExpect(jsonPath("$[0].type.id", is(CREDIT)))
                .andExpect(jsonPath("$[0].type.description", is(transactionCredit.getType().getDescription())))
                .andExpect(jsonPath("$[0].amount", is(transactionCredit.getAmount().intValue())))
                .andExpect(jsonPath("$[0].currency.name", is(TEST_CURRENCY)))
                .andExpect(jsonPath("$[0].description", is(transactionCredit.getDescription())));
    }

    @Test
    public void testCreateTransaction_thenReturnJson() throws Exception {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("globalId",transactionCredit.getGlobalId());
        dataMap.put("currency",transactionCredit.getCurrency().getName());
        dataMap.put("cardId",transactionCredit.getCard().getId().toString());
        dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
              service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String validJson = new GsonBuilder().create().toJson(dataMap);

        mvc.perform(post("/transactions")
                .content(validJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(transactionCredit.getId())))
                .andExpect(jsonPath("$.globalId", is(transactionCredit.getGlobalId())))
                .andExpect(jsonPath("$.type.id", is(CREDIT)))
                .andExpect(jsonPath("$.type.description", is(transactionCredit.getType().getDescription())))
                .andExpect(jsonPath("$.amount", is(transactionCredit.getAmount().intValue())))
                .andExpect(jsonPath("$.currency.name", is(TEST_CURRENCY)))
                .andExpect(jsonPath("$.description", is(transactionCredit.getDescription())));
    }

    @Test
    public void testCreateTransaction_NoCurrency() throws Exception {

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("globalId",transactionCredit.getGlobalId());
        //dataMap.put("currency",transactionCredit.getCurrency().getName());
        dataMap.put("cardId",transactionCredit.getCard().getId().toString());
        dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
                service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String json = new GsonBuilder().create().toJson(dataMap);
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "currency");

        mvc.perform(post("/transactions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/transactions")));
    }

    @Test
    public void testCreateTransaction_NoGlobalId() throws Exception {

        Map<String, String> dataMap = new HashMap<>();
        //dataMap.put("globalId",transactionCredit.getGlobalId());
        dataMap.put("currency",transactionCredit.getCurrency().getName());
        dataMap.put("cardId",transactionCredit.getCard().getId().toString());
        dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
                service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String json = new GsonBuilder().create().toJson(dataMap);
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "globalId");

        mvc.perform(post("/transactions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/transactions")));
    }

    @Test
    public void testCreateTransaction_NocardId() throws Exception {

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("globalId",transactionCredit.getGlobalId());
        dataMap.put("currency",transactionCredit.getCurrency().getName());
        //dataMap.put("cardId",transactionCredit.getcard().getId().toString());
        dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
                service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String json = new GsonBuilder().create().toJson(dataMap);
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "cardId");

        mvc.perform(post("/transactions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/transactions")));
    }

    @Test
    public void testCreateTransaction_NoTransactionTypeIdId() throws Exception {

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("globalId",transactionCredit.getGlobalId());
        dataMap.put("currency",transactionCredit.getCurrency().getName());
        dataMap.put("cardId",transactionCredit.getCard().getId().toString());
        //dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
                service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String json = new GsonBuilder().create().toJson(dataMap);
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "transactionTypeId");

        mvc.perform(post("/transactions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/transactions")));
    }

    @Test
    public void testCreateTransaction_NoAmountId() throws Exception {

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("globalId",transactionCredit.getGlobalId());
        dataMap.put("currency",transactionCredit.getCurrency().getName());
        dataMap.put("cardId",transactionCredit.getCard().getId().toString());
        dataMap.put("transactionTypeId",transactionCredit.getType().getId());
        //dataMap.put("amount",transactionCredit.getAmount().toString());
        dataMap.put("description",transactionCredit.getDescription());

        given(
                service.createTransaction(dataMap.get("globalId"),dataMap.get("currency"),dataMap.get("cardId"),dataMap.get("transactionTypeId"),dataMap.get("amount"),dataMap.get("description")))
                .willReturn(transactionCredit);
        String json = new GsonBuilder().create().toJson(dataMap);
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "amount");

        mvc.perform(post("/transactions")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/transactions")));
    }
}
