package com.zilch.controller;

import com.zilch.entities.Currency;
import com.zilch.helper.Helper;
import com.zilch.service.CardService;
import com.zilch.helper.HelperImpl;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CardController tests
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CardController.class)
public class CardControllerTest {

    @TestConfiguration
    static class CardControllerTestContextConfiguration {
        @Bean
        public Helper validator() {
            return new HelperImpl();
        }

    }
    public static final Integer CURRENCY_ID = 1;
    public static final String TEST_CURRENCY = "GBP";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CardService service;

    private Currency currency;
    private Card card;

    @Before
    public void before(){
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY );
        card = new Card(USER,new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);
        card.setId(1);
    }

    @Test
    public void testGetAll_whenGetCard_thenReturnJsonArray() throws Exception {
        List<Card> allcards = Arrays.asList(card);

        given(service.findAll()).willReturn(allcards);

        mvc.perform(get("/cards")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(card.getId())));
    }

    @Test
    public void testGetCardById_thenReturnJson() throws Exception {

        given(service.findById(card.getId())).willReturn(card);

        mvc.perform(get("/cards/" + card.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(card.getId())))
                .andExpect(jsonPath("$.userId", is(card.getUserId())))
                .andExpect(jsonPath("$.currency.id", is(card.getCurrency().getId())))
                .andExpect(jsonPath("$.balance", is(card.getBalance().intValue())))
                .andExpect(jsonPath("$.lastUpdatedBy", is(card.getLastUpdatedBy())));
    }

    @Test
    public void testGetCardByUserId_thenReturnJson() throws Exception {

        given(service.findByUserId(card.getUserId())).willReturn(Arrays.asList(card));

        mvc.perform(get("/cards/user")
                .param("userId",card.getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(card.getUserId())));

    }

    @Test
    public void testCreateCard_thenReturnJson() throws Exception {

        given(service.createCard(USER,TEST_CURRENCY)).willReturn(card);
        String validCurrencyJson = "{\"userId\":\"" + USER +"\",\"currency\":\"" + TEST_CURRENCY + "\"}";

        mvc.perform(post("/cards")
                .content(validCurrencyJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(card.getId())))
                .andExpect(jsonPath("$.userId", is(card.getUserId())))
                .andExpect(jsonPath("$.currency.name", is(TEST_CURRENCY)))
                .andExpect(jsonPath("$.balance", is(card.getBalance().intValue())))
                .andExpect(jsonPath("$.lastUpdatedBy", is(card.getLastUpdatedBy())));
    }

    @Test
    public void testCreateCard_NoCurrency() throws Exception {

        given(service.createCard(USER,currency.getName())).willReturn(card);
        String json = "{\"userId\":\"" + USER +"\"}";
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "currency");

        mvc.perform(post("/cards")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/cards")));
    }

    @Test
    public void testCreateCard_NoUserId() throws Exception {

        given(service.createCard(USER,currency.getName())).willReturn(card);
        String json = "{\"currency\":\"" + TEST_CURRENCY +"\"}";
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "userId");

        mvc.perform(post("/cards")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/cards")));
    }

    @Test
    public void testCreateCard_MalformedJson() throws Exception {

        given(service.createCard(USER,currency.getName())).willReturn(card);
        String json = "{mmmm";
        String errorMessage = String.format(ErrorMessage.NO_MANDATORY_FIELD, "currency");

        mvc.perform(post("/cards")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("JSON parse error: Unexpected character")))
                .andExpect(jsonPath("$.details", is("uri=/cards")));
    }

}
