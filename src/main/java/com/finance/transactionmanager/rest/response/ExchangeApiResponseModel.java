package com.finance.transactionmanager.rest.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeApiResponseModel {
    private List<ExchangeDataModel> data;
    private Meta meta;
}

