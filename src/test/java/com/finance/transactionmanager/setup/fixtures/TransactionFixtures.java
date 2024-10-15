package com.finance.transactionmanager.setup.fixtures;

import com.finance.transactionmanager.entities.TransactionEntity;
import com.finance.transactionmanager.models.request.TransactionRequestModel;
import com.finance.transactionmanager.models.response.TransactionResponseModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionFixtures {
    public static final UUID ID = UUID.fromString("e7c9f1cd-da4e-4647-9830-ba4450d6f9a1");
    public static final String DESCRIPTION = "Test description";
    public static final String CURRENCY = "Currency";
    public static final LocalDateTime TRANSACTION_DATE_TIME = LocalDateTime.parse("1999-08-01T00:00:00");
    public static final BigDecimal PURCHASE_AMOUNT = BigDecimal.valueOf(123.45);

    public static TransactionEntity getTransactionEntity() {
        return TransactionEntity.builder()
                .id(ID)
                .description(DESCRIPTION)
                .transactionDate(TRANSACTION_DATE_TIME)
                .purchaseAmount(PURCHASE_AMOUNT)
                .build();
    }

    public static TransactionRequestModel getTransactionRequestModel() {
        return TransactionRequestModel.builder()
                .description(DESCRIPTION)
                .purchaseAmount(PURCHASE_AMOUNT)
                .build();
    }

    public static TransactionResponseModel getTransactionResponseModel() {
        return TransactionResponseModel.builder()
                .id(ID)
                .description(DESCRIPTION)
                .transactionDate(TRANSACTION_DATE_TIME)
                .purchaseCurrency(CURRENCY)
                .purchaseAmount(PURCHASE_AMOUNT)
                .build();
    }
}
