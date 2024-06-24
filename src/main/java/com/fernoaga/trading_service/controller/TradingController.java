package com.fernoaga.trading_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernoaga.trading_service.dto.BulkTradingDataDto;
import com.fernoaga.trading_service.dto.StatsDto;
import com.fernoaga.trading_service.dto.TradingDataDto;
import com.fernoaga.trading_service.service.TradingService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @PostMapping("/add")
    public Mono<String> addData(@RequestBody TradingDataDto data) {
        return tradingService.addData(data.symbol(), data.value())
          .thenReturn("Data added successfully");
    }

    @PostMapping("/add_batch")
    public Mono<String> addBatchData(@RequestBody BulkTradingDataDto tradingData) {
        return tradingService.addBatchData(tradingData.symbol(), tradingData.values())
          .thenReturn("Batch data added successfully");
    }

    @GetMapping("/stats")
    public Mono<StatsDto> getStats(@RequestParam String symbol, @RequestParam int k) {
        return tradingService.getStats(symbol, k)
          .map(stats -> new StatsDto(stats.min(), stats.max(), stats.last(), stats.avg(), stats.var()));
    }
}