package com.finance.transactionmanager.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.finance.transactionmanager.configs.ApiConstants.DEFAULT_DATE_PATTERN;
import static com.finance.transactionmanager.configs.ApiConstants.DEFAULT_TIME_ZONE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(name = "Transaction Response")
public class TransactionResponseModel {
    private UUID id;
    private String description;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DEFAULT_DATE_PATTERN,
            timezone = DEFAULT_TIME_ZONE
    )
    private LocalDateTime transactionDate;
    private String purchaseCurrency;
    private BigDecimal purchaseAmount;
}
