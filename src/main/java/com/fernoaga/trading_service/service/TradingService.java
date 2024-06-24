package com.fernoaga.trading_service.service;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.fernoaga.trading_service.config.InMemoryMapConfig;
import com.fernoaga.trading_service.model.Stats;
import com.fernoaga.trading_service.repository.TradingDataRepository;

import reactor.core.publisher.Mono;

@Service
public class TradingService {

    private final TradingDataRepository tradingDataRepository;
    private final InMemoryMapConfig mapConfig;

    private final ConcurrentHashMap<String, ReentrantLock> locks;

    public TradingService(TradingDataRepository tradingDataRepository, InMemoryMapConfig mapConfig) {
        this.tradingDataRepository = tradingDataRepository;
        this.mapConfig = mapConfig;
        this.locks = new ConcurrentHashMap<>(this.mapConfig.getHashMapSize());
    }

    ReentrantLock getLockForSymbol(String symbol) {
        return locks.computeIfAbsent(symbol, k -> new ReentrantLock());
    }

    /**
     * Adds a trading data point for the specified symbol.
     * <p>
     * This method takes a symbol and a trading data point and adds it to the store.
     * The method is non-blocking and returns a {@link Mono<Void>} that completes when the operation
     * is done.
     * </p>
     * <p>
     *
     * @param symbol the symbol representing the financial instrument (e.g., stock ticker).
     * @param value the list of floating-point numbers representing the trading prices to be added.
     * @return a {@link Mono<Void>} that completes when the batch data addition is done.
     */
    public Mono<Void> addData(String symbol, double value) {
        return Mono.fromRunnable(() -> {
            var lock = getLockForSymbol(symbol);
            lock.lock();
            try {
                tradingDataRepository.addTradingData(symbol, value);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * Adds a batch of trading data points for the specified symbol.
     * <p>
     * This method takes a symbol and a list of values representing the trading data points.
     * It adds each value to the data store.
     * The method is non-blocking and returns a {@link Mono<Void>} that completes when the operation
     * is done.
     * </p>
     *
     * @param symbol the symbol representing the financial instrument (e.g., stock ticker).
     * @param values the list of floating-point numbers representing the trading prices to be added.
     * @return a {@link Mono<Void>} that completes when the batch data addition is done.
     */
    public Mono<Void> addBatchData(String symbol, List<Double> values) {
        return Mono.fromRunnable(() -> {
            var lock = getLockForSymbol(symbol);
            lock.lock();
            try {
                tradingDataRepository.addTradingData(symbol, values);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * Computes and returns statistical measures (min, max, last, average, variance) for the given symbol.
     * <p>
     * This method calculates statistics based on the most recent 10^k data points for the specified symbol.
     * The method is non-blocking and returns a {@link Mono<Stats>} that emits the computed statistics
     * when the calculation is complete.
     * </p>
     *
     * @param symbol the symbol representing the financial instrument (e.g., stock ticker).
     * @param k      the exponent to determine the number of most recent data points to consider, where
     *               the limit is calculated as 10^k.
     * @return a {@link Mono<Stats>} that emits the computed statistical measures when available.
     */
    public Mono<Stats> getStats(String symbol, int k) {
        return Mono.fromSupplier(() -> {
            ReentrantLock lock = getLockForSymbol(symbol);
            lock.lock();
            try {
                ArrayDeque<Double> dataList = tradingDataRepository.getDataPointsForSymbol(symbol);
                int limit = getLimit(k, dataList);
                return computeStats(dataList, limit);
            } finally {
                lock.unlock();
            }
        });
    }

    private Stats computeStats(ArrayDeque<Double> dataList, int limit) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        double sumOfSquares = 0;
        double last = 0;

        var iterator = dataList.descendingIterator();
        for (int i = 0; i < limit && iterator.hasNext(); i++) {
            double value = iterator.next();
            if (value < min)
                min = value;
            if (value > max)
                max = value;
            if (i == 0)
                last = value;
            sum += value;
            sumOfSquares += value * value;
        }

        double avg = sum / limit;
        double variance = (sumOfSquares / limit) - (avg * avg);

        return new Stats(min, max, last, avg, variance);
    }

    private int getLimit(int k, Collection<?> array) {
        int limit = (int) Math.pow(10, k);
        if (array.size() < limit) {
            limit = array.size();
        }
        return limit;
    }
}