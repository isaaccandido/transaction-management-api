package com.finance.transactionmanager.mappers;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.setup.TestBase;
import com.finance.transactionmanager.setup.fixtures.TransactionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTests extends TestBase {
    @InjectMocks
    private TransactionMapper transactionMapper;

    @Test
    @DisplayName("fromRequestModelToEntity(), should throw BadRequestException when request model is null")
    void fromRequestModelToEntity_should_throw_bad_request_when_requestModel_ToEntity_is_null() {
        try {
            transactionMapper.fromRequestModelToEntity(null);
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Request model cannot be null.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("fromRequestModelToEntity(), should map request model to entity correctly")
    void fromRequestModelToEntity_ToEntity_should_run_correctly() {
        var result = transactionMapper.fromRequestModelToEntity(TransactionFixtures.getTransactionRequestModel());

        assertNotNull(result);
        assertNull(result.getId());
        assertNotNull(result.getDescription());
        assertNull(result.getTransactionDate());
        assertNotNull(result.getPurchaseAmount());

        assertEquals(TransactionFixtures.DESCRIPTION, result.getDescription());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, result.getPurchaseAmount());
    }

    @Test
    @DisplayName("fromEntityToTransactionResponseModel(), should throw BadRequestException when entity is null")
    void fromEntityToTransactionResponseModel_ToResponseModel_should_throw_bad_request_when_requestModel_is_null() {
        try {
            transactionMapper.fromEntityToTransactionResponseModel(null);
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Entity cannot be null.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("fromEntityToTransactionResponseModel(), should map entity to response model correctly")
    void fromEntityToTransactionResponseModel_ToResponseModel_should_run_correctly() {
        var result = transactionMapper.fromEntityToTransactionResponseModel(
                TransactionFixtures.getTransactionEntity()
        );

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
    @DisplayName("fromEntityToExchangeResponseModel(), should throw BadRequestException when entity is null")
    void fromEntityToExchangeResponseModel_should_throw_bad_request_when_requestModel_is_null() {
        try {
            transactionMapper.fromEntityToExchangeResponseModel(null);
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Entity cannot be null.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("fromEntityToExchangeResponseModel(), should map entity to exchange response model correctly")
    void fromEntityToExchangeResponseModel_should_run_correctly() {
        var result = transactionMapper.fromEntityToExchangeResponseModel(
                TransactionFixtures.getTransactionEntity()
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getDescription());
        assertNotNull(result.getTransactionDate());
        assertNotNull(result.getPurchaseAmount());
        assertNull(result.getExchangeDetails());

        assertEquals(TransactionFixtures.ID, result.getId());
        assertEquals(TransactionFixtures.DESCRIPTION, result.getDescription());
        assertEquals(TransactionFixtures.TRANSACTION_DATE_TIME, result.getTransactionDate());
        assertEquals(TransactionFixtures.PURCHASE_AMOUNT, result.getPurchaseAmount());
    }
}
