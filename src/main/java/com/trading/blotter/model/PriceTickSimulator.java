package com.trading.blotter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PriceTickSimulator {
    private String instrument;
    private BigDecimal price;
    private BigDecimal change;
    private long timestamp;
}