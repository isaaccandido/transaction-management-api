package com.finance.transactionmanager.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class ExchangeDataModel {
    @JsonProperty("country")
    private String originatingCountry;

    @JsonProperty("currency")
    private String currencyLabel;

    @JsonProperty("record_date")
    private LocalDate recordDate;

    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;

    @JsonProperty("country_currency_desc")
    private String countryCurrencyDescription;
}
