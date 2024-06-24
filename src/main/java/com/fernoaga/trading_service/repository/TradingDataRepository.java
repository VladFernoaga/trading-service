package com.fernoaga.trading_service.repository;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.fernoaga.trading_service.config.InMemoryMapConfig;

@Repository
public class TradingDataRepository {

    private final InMemoryMapConfig mapConfig;

    private final ConcurrentHashMap<String, ArrayDeque<Double>> inMemoryDataStore;

    public TradingDataRepository(InMemoryMapConfig mapConfig) {
        this.mapConfig = mapConfig;
        inMemoryDataStore = new ConcurrentHashMap<>(mapConfig.getHashMapSize());
    }

    public void addTradingData(String symbol, double value) {
        inMemoryDataStore.computeIfAbsent(symbol, k -> new ArrayDeque<>())
          .addLast(value);
    }

    public void addTradingData(String symbol, List<Double> values) {
        inMemoryDataStore.computeIfAbsent(symbol, k -> new ArrayDeque<>())
          .addAll(values);
    }

    public ArrayDeque<Double> getDataPointsForSymbol(String symbol) {
        var values = inMemoryDataStore.get(symbol);
        return values != null ? values : new ArrayDeque<>();
    }
}
