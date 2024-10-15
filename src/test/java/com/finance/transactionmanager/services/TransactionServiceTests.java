package com.finance.transactionmanager.services;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.exceptions.custom.NotFoundException;
import com.finance.transactionmanager.mappers.TransactionMapper;
import com.finance.transactionmanager.repositories.TransactionRepository;
import com.finance.transactionmanager.rest.gateways.FiscalDataGateway;
import com.finance.transactionmanager.setup.TestBase;
import com.finance.transactionmanager.setup.fixtures.ExchangeFixtures;
import com.finance.transactionmanager.setup.fixtures.TransactionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTests extends TestBase {
    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepositoryMock;

    @Mock
    private TransactionMapper transactionMapperMock;

    @Mock
    private FiscalDataGateway fiscalDataGatewayMock;

    @Test
    void refreshCache_should_call_one_time_the_fiscalDataGateway() {
        doNothing().when(fiscalDataGatewayMock).manualCacheRefresh();

        transactionService.refreshCache();

        verify(fiscalDataGatewayMock, times(1)).manualCacheRefresh();
    }

    @Test
    @DisplayName("getAll(), should fail when page is less than zero")
    void getAll_should_fail_when_page_is_less_than_zero() {
        try {
            transactionService.getAll(-1, 10);
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Page number cannot be less than zero.", ex.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({"0", "101"})
    @DisplayName("getAll(), should fail when size is less than one or is higher than a hundred")
    void getAll_should_fail_when_size_is_less_than_one_or_is_higher_than_a_hundred(String size) {
        try {
            var intSize = Integer.parseInt(size);

            transactionService.getAll(0, intSize);
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Page size must be between 1 and 100.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("getAll(), should run correctly")
    void getAll_should_run_correctly() {
        var entity = TransactionFixtures.getTransactionEntity();
        var model = TransactionFixtures.getTransactionResponseModel();

        doReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 0))
                .when(transactionRepositoryMock)
                .findAll(any(PageRequest.class));

        doReturn(model)
                .when(transactionMapperMock)
                .fromEntityToTransactionResponseModel(entity);

        var result = transactionService.getAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(entity.getId(), result.getData().getFirst().getId());
        assertEquals(entity.getDescription(), result.getData().getFirst().getDescription());
        assertEquals(entity.getTransactionDate(), result.getData().getFirst().getTransactionDate());
        assertEquals(entity.getId(), result.getData().getFirst().getId());
    }

    @Test
    @DisplayName("create(), should execute correctly when description is null")
    void create_should_execute_correctly_when_description_is_null() {
        var requestModel = TransactionFixtures.getTransactionRequestModel();
        var entity = TransactionFixtures.getTransactionEntity();
        var responseModel = TransactionFixtures.getTransactionResponseModel();

        requestModel.setDescription(null);
        responseModel.setDescription(null);

        doReturn(entity)
                .when(transactionRepositoryMock)
                .save(any());

        doReturn(entity)
                .when(transactionMapperMock)
                .fromRequestModelToEntity(any());

        doReturn(responseModel)
                .when(transactionMapperMock)
                .fromEntityToTransactionResponseModel(any());

        var result = transactionService.create(requestModel);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNull(result.getDescription());
        assertNotNull(result.getTransactionDate());
        assertNotNull(result.getPurchaseAmount());

        assertEquals(TransactionFixtures.ID, result.getId());
        assertEquals(TransactionFixtures.TRANSACTION_DATE_TIME, result.getTransactionDate());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, result.getPurchaseAmount());
    }

    @Test
    @DisplayName("create(), should execute correctly when no fields are null and valid")
    void create_should_execute_correctly_when_no_fields_are_null_and_valid() {
        var requestModel = TransactionFixtures.getTransactionRequestModel();
        var entity = TransactionFixtures.getTransactionEntity();
        var responseModel = TransactionFixtures.getTransactionResponseModel();

        doReturn(entity)
                .when(transactionRepositoryMock)
                .save(any());

        doReturn(entity)
                .when(transactionMapperMock)
                .fromRequestModelToEntity(any());

        doReturn(responseModel)
                .when(transactionMapperMock)
                .fromEntityToTransactionResponseModel(any());

        var result = transactionService.create(requestModel);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getDescription());
        assertNotNull(result.getTransactionDate());
        assertNotNull(result.getPurchaseAmount());

        assertEquals(TransactionFixtures.ID, result.getId());
        assertEquals(TransactionFixtures.DESCRIPTION, result.getDescription());
        assertEquals(TransactionFixtures.TRANSACTION_DATE_TIME, result.getTransactionDate());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, result.getPurchaseAmount());
    }

    @Test
    @DisplayName("exchange(), should throw NotFoundException when transaction is not found with provided id")
    void exchange_should_throw_NotFoundException_when_transaction_is_found_with_provided_id() {
        doReturn(Optional.empty())
                .when(transactionRepositoryMock)
                .findById(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(NotFoundException.class, ex);
            assertEquals(
                    "Transaction with id '" + TransactionFixtures.ID + "' was not found.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw BadRequestException when fiscal data is empty")
    void exchange_should_throw_bad_request_when_fiscal_data_is_empty() {
        var entity = TransactionFixtures.getTransactionEntity();

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.empty())
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals(
                    "The purchase cannot be converted to the target currency. Reason: Exchange data " +
                            "is unavailable. Please ensure that the fiscal data source is accessible and " +
                            "contains valid exchange rates for the requested currency.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw BadRequestException when fiscal data returns an empty data list")
    void exchange_should_throw_bad_request_when_fiscal_data_returns_empty_data_list() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        apiResponse.setData(List.of());

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals(
                    "The purchase cannot be converted to the target currency. Reason: Exchange data is " +
                            "unavailable. Please ensure that the fiscal data source is accessible and contains " +
                            "valid exchange rates for the requested currency.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw IllegalArgumentException when DB-stored transaction has no date")
    void exchange_should_throw_IllegalArgumentException_when_db_stored_transaction_has_no_date() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();

        exchangeResponse.setTransactionDate(null);

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals(
                    "The original transaction date must always be provided.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw IllegalArgumentException when DB-stored transaction has no value")
    void exchange_should_throw_IllegalArgumentException_when_db_stored_transaction_has_no_value() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();
        exchangeResponse.setPurchaseAmount(null);

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals(
                    "The transaction original value should always be provided.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw IllegalArgumentException when DB-stored transaction has value of zero")
    void exchange_should_throw_IllegalArgumentException_when_db_stored_transaction_has_value_of_zero() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();
        exchangeResponse.setPurchaseAmount(BigDecimal.ZERO);

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals(
                    "The transaction original value must be a positive non-zero value.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw IllegalArgumentException when DB-stored transaction has value less than zero")
    void exchange_should_throw_IllegalArgumentException_when_db_stored_transaction_has_value_less_than_zero() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();
        exchangeResponse.setPurchaseAmount(BigDecimal.valueOf(-1));

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals(
                    "The transaction original value must be a positive non-zero value.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("exchange(), should throw BadRequestException when no retrieved item has a valid date")
    void exchange_should_throw_BadRequestException_when_no_retried_item_has_valid_date() {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();
        exchangeResponse.setPurchaseAmount(BigDecimal.valueOf(-1));

        apiResponse.getData().getFirst().setRecordDate(null);

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        try {
            transactionService.exchange(TransactionFixtures.ID, "any-currency");
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals(
                    "The purchase cannot be converted to the target currency. Reason: Could not get exchange " +
                            "data within 6 months from purchase date.",
                    ex.getMessage()
            );
        }
    }

    @ParameterizedTest
    @CsvSource({
            // rate     value       rounded result
            "1.2332,    9.828,      12.12",         // Result = 12.12125, round down
            "1.2332,    9.829,      12.12",         // Result = 12.12213, round down
            "1.2332,    9.830,      12.12",         // Result = 12.12302, round down
            "1.2332,    9.831,      12.12",         // Result = 12.12389, round down
            "1.2332,    9.832,      12.12",         // Result = 12.12477, round down
            "1.2332,    9.833,      12.13",         // Result = 12.12565, round up
            "1.2332,    9.834,      12.13",         // Result = 12.12653, round up
            "1.2332,    9.835,      12.13",         // Result = 12.12741, round up
            "1.2332,    9.836,      12.13",         // Result = 12.12829, round up
            "1.2332,    9.837,      12.13",         // Result = 12.12917, round up
    })
    @DisplayName("exchange(), should run correctly and values are rounded up")
    void exchange_should_run_correctly_and_values_are_rounded_up(String rate, String value, String expectedResult) {
        var entity = TransactionFixtures.getTransactionEntity();
        var apiResponse = ExchangeFixtures.getExchangeApiResponseModel();
        var exchangeResponse = ExchangeFixtures.getExchangeResponseModel();
        exchangeResponse.setExchangeDetails(null);

        var testRate = new BigDecimal(rate);
        var testValue = new BigDecimal(value);
        var expectedValue = new BigDecimal(expectedResult);

        apiResponse.getData().getFirst().setExchangeRate(testRate);
        exchangeResponse.setPurchaseAmount(testValue);

        doReturn(Optional.of(entity))
                .when(transactionRepositoryMock)
                .findById(any());

        doReturn(Optional.of(apiResponse))
                .when(fiscalDataGatewayMock)
                .getExchangeData(any(), any());

        doReturn(exchangeResponse)
                .when(transactionMapperMock)
                .fromEntityToExchangeResponseModel(any());

        var result = transactionService.exchange(TransactionFixtures.ID, "any-currency");

        assertNotNull(result);
        assertEquals(expectedValue, result.getExchangeDetails().getConvertedAmount());
    }
}