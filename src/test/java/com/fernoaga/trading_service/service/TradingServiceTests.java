package com.fernoaga.trading_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fernoaga.trading_service.config.InMemoryMapConfig;
import com.fernoaga.trading_service.model.Stats;
import com.fernoaga.trading_service.repository.TradingDataRepository;

import reactor.test.StepVerifier;

public class TradingServiceTests {

    @Mock
    private TradingDataRepository tradingDataRepository;

    @Mock
    private InMemoryMapConfig mapConfig;

    @Mock
    private ReentrantLock mockLock;

    @InjectMocks
    private TradingService tradingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mapConfig.getHashMapSize()).thenReturn(100);
        tradingService = spy(new TradingService(tradingDataRepository, mapConfig));
        doReturn(mockLock).when(tradingService)
          .getLockForSymbol(anyString());
    }

    @Test
    public void givenSymbolAndValue_whenAddData_thenDataIsAdded() {
        // Given
        String symbol = "AAPL";
        double value = 150.0;

        // When
        StepVerifier.create(tradingService.addData(symbol, value))
          .verifyComplete();

        // Then
        verify(tradingDataRepository, times(1)).addTradingData(symbol, value);
    }

    @Test
    public void givenSymbolAndValues_whenAddBatchData_thenDataIsAdded() {
        // Given
        String symbol = "AAPL";
        List<Double> values = Arrays.asList(150.0, 152.0, 153.5);

        // When
        StepVerifier.create(tradingService.addBatchData(symbol, values))
          .verifyComplete();

        // Then
        verify(tradingDataRepository, times(1)).addTradingData(symbol, values);
    }

    @Test
    public void givenSymbolAndK_whenGetStats_thenStatsAreReturned() {
        // Given
        String symbol = "AAPL";
        int k = 1;
        ArrayDeque<Double> dataList = new ArrayDeque<>(Arrays.asList(150.0, 152.0, 153.5));
        Stats expectedStats = new Stats(150.0, 153.5, 153.5, 151.83, 2.05);

        // When
        when(tradingDataRepository.getDataPointsForSymbol(symbol)).thenReturn(dataList);

        StepVerifier.create(tradingService.getStats(symbol, k))
          .assertNext(stats -> {
              assertEquals(expectedStats.min(), stats.min(), 0.01);
              assertEquals(expectedStats.max(), stats.max(), 0.01);
              assertEquals(expectedStats.last(), stats.last(), 0.01);
              assertEquals(expectedStats.avg(), stats.avg(), 0.01);
              assertEquals(expectedStats.var(), stats.var(), 0.01);
          })
          .verifyComplete();

        // Then
        verify(tradingDataRepository, times(1)).getDataPointsForSymbol(symbol);
    }

    @Test
    public void givenConcurrentAccess_whenAddData_thenSerializedByLock() throws InterruptedException, ExecutionException {
        // Given
        String symbol = "AAPL";
        double value1 = 150.0;
        double value2 = 152.0;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // When
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> tradingService.addData(symbol, value1)
          .block(), executor);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> tradingService.addData(symbol, value2)
          .block(), executor);

        CompletableFuture.allOf(future1, future2)
          .join(); // Wait for all tasks to complete

        executor.shutdown();

        // Then
        verify(mockLock, times(2)).lock();
        verify(mockLock, times(2)).unlock();
        verify(tradingDataRepository, times(1)).addTradingData(symbol, value1);
        verify(tradingDataRepository, times(1)).addTradingData(symbol, value2);
    }
}
