package com.finance.transactionmanager.controllers;

import com.finance.transactionmanager.models.generic.CollectionContentWrapper;
import com.finance.transactionmanager.models.response.TransactionResponseModel;
import com.finance.transactionmanager.services.TransactionService;
import com.finance.transactionmanager.setup.TestBase;
import com.finance.transactionmanager.setup.fixtures.ExchangeFixtures;
import com.finance.transactionmanager.setup.fixtures.TransactionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TransactionControllerTests extends TestBase {
    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionServiceMock;

    @Test
    @DisplayName("refreshCache(), should execute successfully")
    void refreshCache_should_execute_successfully() {
        doNothing().when(transactionServiceMock).refreshCache();

        var response = transactionController.refreshCache();

        verify(transactionServiceMock, times(1)).refreshCache();

        assertNotNull(response);
        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getAll(), should get an empty list when no records are found")
    void getAll_should_get_an_empty_list_when_no_records_are_found() {
        doReturn(new CollectionContentWrapper<TransactionResponseModel>(
                List.of(),
                0,
                0,
                10,
                0L)).when(transactionServiceMock)
                .getAll(anyInt(), anyInt());

        var result = transactionController.getAll(0, 10);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getData().size());
    }

    @Test
    @DisplayName("getAll(), should get a list with one item to simulate existing records")
    void getAll_should_get_a_list_with_one_item_to_simulate_existing_records() {
        var items = List.of(TransactionFixtures.getTransactionResponseModel());

        doReturn(new CollectionContentWrapper<>(
                items,
                0,
                1,
                10,
                1L))
                .when(transactionServiceMock)
                .getAll(anyInt(), anyInt());

        var result = transactionController.getAll(0, 10);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    @DisplayName("create(), should execute correctly")
    void create_should_execute_correctly() {
        var requestModel = TransactionFixtures.getTransactionRequestModel();
        var responseModel = TransactionFixtures.getTransactionResponseModel();

        doReturn(responseModel)
                .when(transactionServiceMock)
                .create(any());

        var result = transactionController.create(requestModel);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());

        var body = result.getBody();

        assertNotNull(body);

        assertNotNull(body.getId());
        assertNotNull(body.getDescription());
        assertNotNull(body.getTransactionDate());
        assertNotNull(body.getPurchaseAmount());

        assertEquals(TransactionFixtures.ID, body.getId());
        assertEquals(TransactionFixtures.DESCRIPTION, body.getDescription());
        assertEquals(TransactionFixtures.TRANSACTION_DATE_TIME, body.getTransactionDate());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, body.getPurchaseAmount());
    }

    @Test
    @DisplayName("exchange(), should execute correctly")
    void exchange_should_execute_correctly() {
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();

        doReturn(exchangeResponse)
                .when(transactionServiceMock)
                .exchange(any(), any());

        var result = transactionController.exchange(ExchangeFixtures.ID, "Country-Currency");

        assertEquals(HttpStatus.OK, result.getStatusCode());

        var body = result.getBody();

        assertNotNull(body);

        assertNotNull(body.getId());
        assertNotNull(body.getDescription());
        assertNotNull(body.getTransactionDate());
        assertNotNull(body.getPurchaseAmount());
        assertNotNull(body.getExchangeDetails());
        assertNotNull(body.getExchangeDetails().getExchangeRate());
        assertNotNull(body.getExchangeDetails().getConvertedAmount());

        assertEquals(TransactionFixtures.ID, body.getId());
        assertEquals(TransactionFixtures.DESCRIPTION, body.getDescription());
        assertEquals(TransactionFixtures.TRANSACTION_DATE_TIME, body.getTransactionDate());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, body.getPurchaseAmount());
        assertEquals(ExchangeFixtures.EXCHANGE_RATE, body.getExchangeDetails().getExchangeRate());
        assertEquals(ExchangeFixtures.CONVERTED_AMOUNT, body.getExchangeDetails().getConvertedAmount());
    }
}
