package com.finance.transactionmanager.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(name = "Exchange Response")
public class ExchangeResponseModel extends TransactionResponseModel {
    private ExchangeDetailsResponseModel exchangeDetails;
}
