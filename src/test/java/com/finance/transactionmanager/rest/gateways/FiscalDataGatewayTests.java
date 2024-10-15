package com.finance.transactionmanager.rest.gateways;

import com.finance.transactionmanager.exceptions.custom.BadRequestException;
import com.finance.transactionmanager.exceptions.custom.InternalServerErrorException;
import com.finance.transactionmanager.rest.response.ExchangeApiResponseModel;
import com.finance.transactionmanager.rest.response.ExchangeDataModel;
import com.finance.transactionmanager.setup.TestBase;
import com.finance.transactionmanager.setup.fixtures.ExchangeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FiscalDataGatewayTests extends TestBase {
    @Mock
    WebClient webClientMock;
    @InjectMocks
    private FiscalDataGateway fiscalDataGateway;

    @BeforeEach
    void setupFields() {
        ReflectionTestUtils.setField(fiscalDataGateway, "baseUrl", "http://test.ar");
        ReflectionTestUtils.setField(fiscalDataGateway, "maxConnectionAttempts", 3);
        ReflectionTestUtils.setField(fiscalDataGateway, "timeoutBetweenAttemptsInMillis", 1000);
        ReflectionTestUtils.setField(fiscalDataGateway, "webClient", webClientMock);
    }

    @Test
    @DisplayName("scheduledCacheRefresh(), should not run when cache is disabled, message not sent")
    void scheduledCacheRefresh_should_not_run_when_cache_is_disabled_message_not_sent() {
        fiscalDataGateway.scheduledCacheRefresh();

        verify(webClientMock, times(0))
                .get();

        var field = ReflectionTestUtils.getField(fiscalDataGateway, "cacheDisabledMessageSentAtStartup");
        assert field != null;

        assertTrue((Boolean) field);
    }

    @Test
    @DisplayName("scheduledCacheRefresh(), should not run when cache is disabled, already sent")
    void scheduledCacheRefresh_should_not_run_when_cache_is_disabled_message_already_sent() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheDisabledMessageSentAtStartup", true);
        var fieldBefore = ReflectionTestUtils.getField(fiscalDataGateway, "cacheDisabledMessageSentAtStartup");
        assert fieldBefore != null;

        assertTrue((Boolean) fieldBefore);

        fiscalDataGateway.scheduledCacheRefresh();

        verify(webClientMock, times(0))
                .get();

        var fieldAfter = ReflectionTestUtils.getField(fiscalDataGateway, "cacheDisabledMessageSentAtStartup");
        assert fieldAfter != null;

        assertTrue((Boolean) fieldAfter);
    }

    @Test
    @DisplayName("manualCacheRefresh(), should throw BadRequestException when cache is disabled")
    void manualCacheRefresh_should_throw_BadRequestException_when_cache_is_disabled() {
        try {
            fiscalDataGateway.manualCacheRefresh();
        } catch (Exception ex) {
            assertInstanceOf(BadRequestException.class, ex);
            assertEquals("Cache is currently disabled. Data cannot be refreshed. Please check the system " +
                    "configuration or contact the system administrator for assistance.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("manualCacheRefresh(), should run correctly")
    void manualCacheRefresh_should_run_correctly() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var response = ExchangeFixtures.getExchangeApiResponseModel();

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(response))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        fiscalDataGateway.manualCacheRefresh();

        verify(webClientMock, times(1))
                .get();
    }

    @Test
    @DisplayName("scheduledCacheRefresh(), should run correctly with no cache and only one page")
    void scheduledCacheRefresh_should_run_correctly_no_cache_only_one_page() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var response = ExchangeFixtures.getExchangeApiResponseModel();

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(response))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        fiscalDataGateway.scheduledCacheRefresh();

        verify(webClientMock, times(1))
                .get();
    }


    @Test
    @DisplayName("scheduledCacheRefresh(), should run correctly with no cache and more than one page")
    void scheduledCacheRefresh_should_run_correctly_no_cache_more_than_one_page() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var firstResponse = ExchangeFixtures.getExchangeApiResponseModel();
        firstResponse.getMeta().setTotalPages(2L);
        firstResponse.getMeta().setTotalCount(2);

        var secondResponse = ExchangeFixtures.getExchangeApiResponseModel();
        secondResponse.getMeta().setTotalPages(1L);
        secondResponse.getMeta().setTotalCount(1);

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(firstResponse))
                .doReturn(Mono.just(secondResponse))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        fiscalDataGateway.scheduledCacheRefresh();

        verify(webClientMock, times(2))
                .get();
    }

    @Test
    @DisplayName("scheduledCacheRefresh(), should throw BadRequestException when unable to retrieve data from server")
    void scheduledCacheRefresh_should_throw_BadRequestException_when_unable_to_retrieve_data_from_server() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.empty())
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        try {
            fiscalDataGateway.scheduledCacheRefresh();
        } catch (Exception e) {
            assertInstanceOf(BadRequestException.class, e);
            assertEquals("Failed to fetch data from API.", e.getMessage());
        }

        verify(webClientMock, times(1))
                .get();
    }

    @Test
    @DisplayName("getExchangeData(), should throw IllegalArgumentException when targetCurrency is empty")
    void getExchangeData_should_throw_IllegalArgumentException_when_targetCurrency_is_empty() {
        var date = LocalDateTime.now();

        try {
            fiscalDataGateway.getExchangeData("", date);
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals("Currency input cannot be null or empty.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("getExchangeData(), should throw IllegalArgumentException when cache is disabled and targetCurrency is null")
    void getExchangeData_should_throw_IllegalArgumentException_when_cache_is_disabled_and_targetCurrency_is_null() {
        var date = LocalDateTime.now();

        try {
            fiscalDataGateway.getExchangeData(null, date);
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals("Currency input cannot be null or empty.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("getExchangeData(), should throw IllegalArgumentException when cache is disabled and targetCurrency has no dash")
    void getExchangeData_should_throw_IllegalArgumentException_when_cache_is_disabled_and_targetCurrency_has_no_dash() {
        var date = LocalDateTime.now();
        var targetCurrency = "IncorrectTargetCurrency";

        try {
            fiscalDataGateway.getExchangeData(targetCurrency, date);
        } catch (Exception ex) {
            assertInstanceOf(IllegalArgumentException.class, ex);
            assertEquals(
                    "Currency input must consist of exactly two parts separated by a dash.",
                    ex.getMessage()
            );
        }
    }

    @Test
    @DisplayName("getExchangeData(), should execute correctly when cache is disabled and has no cached data")
    void getExchangeData_should_execute_correctly_when_cache_is_disabled_and_has_no_cached_data() {
        var date = LocalDateTime.now();
        var targetCurrency = "Brazil-Real";
        var response = ExchangeFixtures.getExchangeApiResponseModel();

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(response))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        var result = fiscalDataGateway.getExchangeData(targetCurrency, date);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(response.getData().size(), result.get().getData().size());
        assertEquals(response.getData(), result.get().getData());

        assertEquals(response.getData().getFirst().getRecordDate(),
                result.get().getData().getFirst().getRecordDate());

        assertEquals(response.getData().getFirst().getExchangeRate(),
                result.get().getData().getFirst().getExchangeRate());

        assertEquals(response.getData().getFirst().getCurrencyLabel(),
                result.get().getData().getFirst().getCurrencyLabel());

        assertEquals(response.getData().getFirst().getCountryCurrencyDescription(),
                result.get().getData().getFirst().getCountryCurrencyDescription());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getExchangeData(), should execute correctly when cache is disabled and has cached data")
    void getExchangeData_should_execute_correctly_when_cache_is_disabled_and_has_cached_data() {
        var cachedField = (AtomicReference<HashSet<ExchangeDataModel>>)
                ReflectionTestUtils.getField(fiscalDataGateway, "cachedData");

        assert cachedField != null;
        var data = new HashSet<ExchangeDataModel>();

        var responseData = ExchangeFixtures.getExchangeDataModel();
        data.add(responseData);

        cachedField.set(data);

        var date = LocalDateTime.now();
        var targetCurrency = "Brazil-Real";
        var response = ExchangeFixtures.getExchangeApiResponseModel();

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(response))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        var result = fiscalDataGateway.getExchangeData(targetCurrency, date);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(response.getData().size(), result.get().getData().size());
        assertEquals(response.getData(), result.get().getData());

        assertEquals(response.getData().getFirst().getRecordDate(),
                result.get().getData().getFirst().getRecordDate());

        assertEquals(response.getData().getFirst().getExchangeRate(),
                result.get().getData().getFirst().getExchangeRate());

        assertEquals(response.getData().getFirst().getCurrencyLabel(),
                result.get().getData().getFirst().getCurrencyLabel());

        assertEquals(response.getData().getFirst().getCountryCurrencyDescription(),
                result.get().getData().getFirst().getCountryCurrencyDescription());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getExchangeData(), should execute correctly when cache is enabled and has no cached data")
    void getExchangeData_should_execute_correctly_when_cache_is_enabled_and_has_no_cached_data() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var cachedField = (AtomicReference<HashSet<ExchangeDataModel>>)
                ReflectionTestUtils.getField(fiscalDataGateway, "cachedData");

        assert cachedField != null;

        cachedField.set(new HashSet<>());

        var date = LocalDateTime.now();
        var targetCurrency = "Brazil-Real";
        var response = ExchangeFixtures.getExchangeApiResponseModel();

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doReturn(Mono.just(response))
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        var result = fiscalDataGateway.getExchangeData(targetCurrency, date);

        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getExchangeData(), should execute correctly when cache is enabled and has cached data")
    void getExchangeData_should_execute_correctly_when_cache_is_enabled_and_has_cached_data() {
        ReflectionTestUtils.setField(fiscalDataGateway, "cacheEnabled", true);

        var cachedField = (AtomicReference<HashSet<ExchangeDataModel>>)
                ReflectionTestUtils.getField(fiscalDataGateway, "cachedData");

        assert cachedField != null;
        var data = new HashSet<ExchangeDataModel>();

        var responseData = ExchangeFixtures.getExchangeDataModel();
        data.add(responseData);

        cachedField.set(data);

        var response = fiscalDataGateway.getExchangeData(
                ExchangeFixtures.COUNTRY_CURRENCY,
                responseData.getRecordDate().atStartOfDay().minusMonths(6)
        );

        assertNotNull(response);
    }

    @Test
    @DisplayName("getExchangeData(), should execute correctly when cache is disabled but connection fails")
    void getExchangeData_should_execute_correctly_when_cache_is_disabled_but_connection_fails() {
        var date = LocalDateTime.now();
        var targetCurrency = "Brazil-Real";

        var requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        var responseSpecMock = mock(WebClient.ResponseSpec.class);

        doReturn(requestHeadersUriSpecMock)
                .when(webClientMock)
                .get();

        doReturn(requestHeadersSpecMock)
                .when(requestHeadersUriSpecMock)
                .uri(any(String.class));

        doReturn(responseSpecMock)
                .when(requestHeadersSpecMock)
                .retrieve();

        doThrow(RuntimeException.class)
                .when(responseSpecMock)
                .bodyToMono(ExchangeApiResponseModel.class);

        try {
            fiscalDataGateway.getExchangeData(targetCurrency, date);
        } catch (Exception ex) {
            assertInstanceOf(InternalServerErrorException.class, ex);
            assertEquals("The purchase cannot be converted to the target currency. Reason: " +
                            "Failed to retrieve fiscal data from the server. The server may be unavailable " +
                            "or not responding.",
                    ex.getMessage());
        }
    }
}