package com.finance.transactionmanager.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Exchange Details Response")
public class ExchangeDetailsResponseModel {
    private String originatingCountry;
    private String currencyLabel;
    private LocalDate exchangeRateRecordDate;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
}
