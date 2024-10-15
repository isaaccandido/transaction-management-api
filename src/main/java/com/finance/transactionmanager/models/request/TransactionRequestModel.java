package com.finance.transactionmanager.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@Schema(name = "Transaction Request")
public class TransactionRequestModel {
    @Size(max = 50, message = "Descriptiasdfracters")
    private String description;

    @NotNull(message = "asdf")
    @DecimalMin(value = "0.0", inclusive = false, message = "asdf")
    private BigDecimal purchaseAmount;
}
