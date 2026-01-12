package com.trading.blotter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceUpdate {

    private String tradeId;

    // Only send fields that changed (delta update)
    private Map<String, Object> updatedFields;

    private long timestamp;

    public static PriceUpdate create(String tradeId, BigDecimal newPnl, BigDecimal newMtm) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("pnl", newPnl);
        fields.put("mtm", newMtm);

        return PriceUpdate.builder()
                .tradeId(tradeId)
                .updatedFields(fields)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static PriceUpdate withGreeks(String tradeId, BigDecimal pnl, BigDecimal mtm,
                                         BigDecimal delta, BigDecimal gamma) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("pnl", pnl);
        fields.put("mtm", mtm);
        fields.put("delta", delta);
        fields.put("gamma", gamma);

        return PriceUpdate.builder()
                .tradeId(tradeId)
                .updatedFields(fields)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}