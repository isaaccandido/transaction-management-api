package com.finance.transactionmanager.rest.gateways;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.exceptions.custom.InternalServerErrorException;
import com.finance.transactionmanager.rest.response.ExchangeApiResponseModel;
import com.finance.transactionmanager.rest.response.ExchangeDataModel;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@Log4j2
public class FiscalDataGateway {
    private final WebClient webClient;
    private final AtomicReference<HashSet<ExchangeDataModel>> cachedData = new AtomicReference<>(new HashSet<>());
    @Value("${system.gateways.fiscal-gateway.base-url}")
    private String baseUrl;
    @Value("${system.gateways.fiscal-gateway.max-connection-attempts}")
    private Integer maxConnectionAttempts;
    @Value("${system.gateways.fiscal-gateway.timeout-between-fetch-attempts-in-millis}")
    private Integer timeoutBetweenAttemptsInMillis;
    @Value("${system.gateways.fiscal-gateway.enable-caching}")
    private boolean cacheEnabled;

    private boolean cacheDisabledMessageSentAtStartup;

    public FiscalDataGateway(WebClient webClient) {
        this.webClient = webClient;
    }

    @Scheduled(fixedDelayString = "${system.gateways.fiscal-gateway.refresh-interval-in-milliseconds}")
    public void scheduledCacheRefresh() {
        if (!cacheEnabled) {
            if (!cacheDisabledMessageSentAtStartup) {
                log.info("[{}] Cache is disabled. All exchange rates will be retrieved via API calls, " +
                                "which may slow down performance. High request frequency could lead to errors, " +
                                "as the Treasury Fiscal API has limited capacity for handling frequent requests.",
                        this.getClass().getSimpleName());

                cacheDisabledMessageSentAtStartup = true;
            }

            return;
        }

        doRefresh();
    }

    public void manualCacheRefresh() {
        if (!cacheEnabled) {
            throw new BadRequestException("Cache is currently disabled. Data cannot be refreshed. Please check " +
                    "the system configuration or contact the system administrator for assistance.");
        }

        doRefresh();
    }

    private void doRefresh() {
        log.info("[{}] Refreshing cache...", this.getClass().getSimpleName());
        var tempSet = new HashSet<ExchangeDataModel>();

        int currentPage = 1;
        long totalItemsFromPayload;

        while (true) {
            var requestUri = assembleCompleteDataApiRequestUri(currentPage);
            var apiResponse = communicate(requestUri)
                    .orElseThrow(() -> {
                        log.error("[{}] Failed to fetch data from API.", this.getClass().getSimpleName());
                        return new BadRequestException("Failed to fetch data from API.");
                    });

            tempSet.addAll(apiResponse.getData());

            totalItemsFromPayload = apiResponse.getMeta().getTotalCount();

            int remainingItems = (int) (totalItemsFromPayload - tempSet.size());

            if (remainingItems <= 0) break;

            currentPage++;
        }

        cachedData.get().addAll(tempSet);

        log.info("[{}] Cache refreshed successfully. Received {} exchange entries.",
                this.getClass().getSimpleName(),
                cachedData.get().size()
        );
    }

    public Optional<ExchangeApiResponseModel> getExchangeData(@NotNull final String targetCurrency,
                                                              @NotNull final LocalDateTime transactionDate) {
        List<ExchangeDataModel> filteredData = List.of();

        if (cacheEnabled && !cachedData.get().isEmpty()) {
            filteredData = filterCacheData(targetCurrency, transactionDate);
        }

        return filteredData.isEmpty()
                ? communicate(assembleFilteredApiRequestUri(normalizeCurrencyText(targetCurrency), transactionDate))
                : Optional.of(ExchangeApiResponseModel.builder().data(filteredData).build());
    }

    private List<ExchangeDataModel> filterCacheData(String targetCurrency, LocalDateTime transactionDate) {
        return cachedData.get()
                .stream()
                .filter(item -> item.getCountryCurrencyDescription().equalsIgnoreCase(targetCurrency))
                .filter(item ->
                        item.getRecordDate().isAfter(transactionDate.minusMonths(6).toLocalDate()))
                .toList();
    }

    private Optional<ExchangeApiResponseModel> communicate(String requestUri) {
        try {
            return webClient
                    .get()
                    .uri(requestUri)
                    .retrieve()
                    .bodyToMono(ExchangeApiResponseModel.class)
                    .retryWhen(Retry.fixedDelay(maxConnectionAttempts, Duration.ofMillis(timeoutBetweenAttemptsInMillis))
                            .jitter(0.5))
                    .blockOptional();
        } catch (Exception ex) {
            throw new InternalServerErrorException("The purchase cannot be converted to the target currency. Reason: " +
                    "Failed to retrieve fiscal data from the server. The server may be " +
                    "unavailable or not responding.");
        }
    }

    private String assembleCompleteDataApiRequestUri(int page) {
        return baseUrl
                + "/v1/accounting/od/rates_of_exchange"
                + "?fields=record_date,exchange_rate,country,currency,country_currency_desc"
                + "&page[number]="
                + page
                + "&page[size]=10000";
    }

    private String assembleFilteredApiRequestUri(String targetCurrency, LocalDateTime originalTransactionDate) {
        targetCurrency = normalizeCurrencyText(targetCurrency);
        var sixMonthsBeforeTransaction = originalTransactionDate.minusMonths(6)
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return baseUrl
                + "/v1/accounting/od/rates_of_exchange"
                + "?fields=record_date,exchange_rate,country,currency, country_currency_desc"
                + "&filter=country_currency_desc:in:("
                + targetCurrency
                + "),record_date:gte:"
                + sixMonthsBeforeTransaction
                + "&sort=-record_date";
    }

    private String normalizeCurrencyText(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency input cannot be null or empty.");
        }

        var parts = targetCurrency.trim().split("-");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Currency input must consist of exactly two parts separated by a dash.");
        }

        return Arrays.stream(parts)
                .map(this::capitalizeFirstLetter)
                .reduce((firstPart, secondPart) -> firstPart + "-" + secondPart)
                .orElseThrow();
    }

    private String capitalizeFirstLetter(String word) {
        return Arrays.stream(word.split(" "))
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
}
