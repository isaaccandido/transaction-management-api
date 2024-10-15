package com.finance.transactionmanager.services;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.exceptions.custom.NotFoundException;
import com.finance.transactionmanager.mappers.TransactionMapper;
import com.finance.transactionmanager.models.generic.CollectionContentWrapper;
import com.finance.transactionmanager.models.request.TransactionRequestModel;
import com.finance.transactionmanager.models.response.ExchangeDetailsResponseModel;
import com.finance.transactionmanager.models.response.ExchangeResponseModel;
import com.finance.transactionmanager.models.response.TransactionResponseModel;
import com.finance.transactionmanager.repositories.TransactionRepository;
import com.finance.transactionmanager.rest.gateways.FiscalDataGateway;
import com.finance.transactionmanager.rest.response.ExchangeApiResponseModel;
import com.finance.transactionmanager.rest.response.ExchangeDataModel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final FiscalDataGateway fiscalDataGateway;

    public void refreshCache() {
        fiscalDataGateway.manualCacheRefresh();
    }

    public CollectionContentWrapper<TransactionResponseModel> getAll(int page, int size) {
        if (page < 0) throw new BadRequestException("Page number cannot be less than zero.");
        if (size < 1 || size > 100) throw new BadRequestException("Page size must be between 1 and 100.");

        var searchResult = transactionRepository.findAll(PageRequest.of(page, size));

        var transactions = searchResult
                .stream()
                .map(transactionMapper::fromEntityToTransactionResponseModel)
                .toList();

        return new CollectionContentWrapper<>(transactions,
                searchResult.getNumber(),
                searchResult.getTotalPages(),
                searchResult.getSize(),
                searchResult.getTotalElements());
    }

    public TransactionResponseModel create(@NotNull TransactionRequestModel request) {
        request.setPurchaseAmount(
                request.getPurchaseAmount().setScale(2, RoundingMode.HALF_UP)
        );

        var entity = transactionMapper.fromRequestModelToEntity(request);

        entity.setTransactionDate(LocalDateTime.now());

        var response = transactionRepository.save(entity);

        return transactionMapper.fromEntityToTransactionResponseModel(response);
    }

    public ExchangeResponseModel exchange(UUID id, String targetCurrency) {
        var searchResult = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction with id '" + id + "' was not found."));

        var fiscalDataResponse = fiscalDataGateway.getExchangeData(targetCurrency, searchResult.getTransactionDate());

        if (fiscalDataResponse.isEmpty() || fiscalDataResponse.get().getData().isEmpty()) {
            throw getBadRequestForFailedConversion("Exchange data is unavailable. Please ensure that the fiscal" +
                    " data source is accessible and contains valid exchange rates for the requested currency.");
        }

        var transactionModel = transactionMapper.fromEntityToExchangeResponseModel(searchResult);

        var exchangeDetails = assembleExchangeDetails(transactionModel, fiscalDataResponse.get());

        transactionModel.setExchangeDetails(exchangeDetails);

        return transactionModel;
    }

    private BadRequestException getBadRequestForFailedConversion(String detail) {
        return new BadRequestException("The purchase cannot be converted to the target currency. Reason: " + detail);
    }

    private ExchangeDetailsResponseModel assembleExchangeDetails(ExchangeResponseModel originalTransaction,
                                                                 ExchangeApiResponseModel fiscalDataResponse) {
        var exchangeData = getMostRecentExchangeData(
                originalTransaction.getTransactionDate(),
                fiscalDataResponse.getData()
        ).orElseThrow(() -> getBadRequestForFailedConversion("Could not get exchange data within " +
                "6 months from purchase date."));

        var exchangedValue = calculateExchangedValue(
                originalTransaction.getPurchaseAmount(),
                exchangeData.getExchangeRate()
        );

        return ExchangeDetailsResponseModel.builder()
                .exchangeRateRecordDate(exchangeData.getRecordDate())
                .originatingCountry(exchangeData.getOriginatingCountry())
                .currencyLabel(exchangeData.getCurrencyLabel())
                .exchangeRate(exchangeData.getExchangeRate())
                .convertedAmount(exchangedValue)
                .build();
    }

    private Optional<ExchangeDataModel> getMostRecentExchangeData(LocalDateTime originalTransactionDate,
                                                                  List<ExchangeDataModel> exchangeDataList) {
        if (originalTransactionDate == null) {
            throw new IllegalArgumentException("The original transaction date must always be provided.");
        }

        var comparisonDate = originalTransactionDate.toLocalDate();

        return exchangeDataList.stream()
                .filter(data -> data.getRecordDate() != null)
                .filter(data -> !data.getRecordDate().isAfter(comparisonDate))
                .max(Comparator.comparing(ExchangeDataModel::getRecordDate));
    }

    private BigDecimal calculateExchangedValue(BigDecimal originalValue, BigDecimal exchangeRate) {
        if (originalValue == null) {
            throw new IllegalArgumentException("The transaction original value should always be provided.");
        }

        if (Objects.equals(originalValue, BigDecimal.ZERO) || originalValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The transaction original value must be a positive non-zero value.");
        }

        return originalValue.multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
