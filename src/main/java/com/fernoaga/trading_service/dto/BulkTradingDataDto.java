package com.fernoaga.trading_service.dto;

import java.util.List;

public record BulkTradingDataDto(String symbol, List<Double> values) {
}
