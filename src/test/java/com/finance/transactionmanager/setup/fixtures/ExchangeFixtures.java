package com.finance.transactionmanager.setup.fixtures;

import com.finance.transactionmanager.models.response.ExchangeDetailsResponseModel;
import com.finance.transactionmanager.models.response.ExchangeResponseModel;
import com.finance.transactionmanager.rest.response.ExchangeApiResponseModel;
import com.finance.transactionmanager.rest.response.ExchangeDataModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ExchangeFixtures {
    public static final UUID ID = UUID.fromString("e7c9f1cd-da4e-4647-9830-ba4450d6f9a1");
    public static final String DESCRIPTION = "Test description";
    public static final String CURRENCY = "Currency";
    public static final String COUNTRY_CURRENCY = "Country-Currency";
    public static final LocalDateTime TRANSACTION_DATE_TIME = LocalDateTime.parse("1999-08-01T00:00:00");
    public static final BigDecimal PURCHASE_AMOUNT = BigDecimal.valueOf(123.45);
    public static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(123.45);
    public static final BigDecimal CONVERTED_AMOUNT = BigDecimal.valueOf(456.78);
    public static final LocalDate EXCHANGE_DATE = LocalDate.parse("1999-06-01");

    public static ExchangeResponseModel getExchangeResponseModel() {
        return ExchangeResponseModel
                .builder()
                .id(ID)
                .description(DESCRIPTION)
                .transactionDate(TRANSACTION_DATE_TIME)
                .purchaseAmount(PURCHASE_AMOUNT)
                .purchaseCurrency(CURRENCY)
                .exchangeDetails(getExchangeDetailsResponseModel())
                .build();
    }

    public static ExchangeDetailsResponseModel getExchangeDetailsResponseModel() {
        return ExchangeDetailsResponseModel
                .builder()
                .exchangeRate(EXCHANGE_RATE)
                .convertedAmount(CONVERTED_AMOUNT)
                .originatingCountry(COUNTRY_CURRENCY)
                .currencyLabel(CURRENCY)
                .build();
    }

    public static ExchangeApiResponseModel getExchangeApiResponseModel() {
        return ExchangeApiResponseModel
                .builder()
                .data(List.of(getExchangeDataModel()))
                .meta(MetaFixtures.getMeta())
                .build();
    }

    public static ExchangeDataModel getExchangeDataModel() {
        return ExchangeDataModel
                .builder()
                .originatingCountry(COUNTRY_CURRENCY)
                .currencyLabel(CURRENCY)
                .exchangeRate(EXCHANGE_RATE)
                .recordDate(EXCHANGE_DATE)
                .countryCurrencyDescription(COUNTRY_CURRENCY)
                .build();
    }
}
