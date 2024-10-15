package com.finance.transactionmanager.mappers;

import com.finance.transactionmanager.entities.TransactionEntity;
import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.models.request.TransactionRequestModel;
import com.finance.transactionmanager.models.response.ExchangeResponseModel;
import com.finance.transactionmanager.models.response.TransactionResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    private static final String NULL_MODEL_MESSAGE = "Request model cannot be null.";
    private static final String NULL_ENTITY_MESSAGE = "Entity cannot be null.";
    @Value("${system.defaults.currency}")
    private String defaultCurrency;

    public TransactionEntity fromRequestModelToEntity(TransactionRequestModel requestModel) {
        if (requestModel == null) throw new BadRequestException(NULL_MODEL_MESSAGE);

        return TransactionEntity.builder()
                .id(null)
                .description(requestModel.getDescription())
                .transactionDate(null)
                .purchaseAmount(requestModel.getPurchaseAmount())
                .build();
    }

    public TransactionResponseModel fromEntityToTransactionResponseModel(TransactionEntity entity) {
        if (entity == null) throw new BadRequestException(NULL_ENTITY_MESSAGE);

        return TransactionResponseModel.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .purchaseAmount(entity.getPurchaseAmount())
                .purchaseCurrency(defaultCurrency)
                .build();
    }

    public ExchangeResponseModel fromEntityToExchangeResponseModel(TransactionEntity entity) {
        if (entity == null) throw new BadRequestException(NULL_ENTITY_MESSAGE);

        return ExchangeResponseModel.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .purchaseCurrency(defaultCurrency)
                .purchaseAmount(entity.getPurchaseAmount())
                .exchangeDetails(null)
                .build();
    }
}
