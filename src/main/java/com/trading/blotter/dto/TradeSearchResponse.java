package com.trading.blotter.dto;

import com.trading.blotter.model.TradeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSearchResponse {
    private List<TradeDocument> trades;
    private long totalCount;
    private Object[] lastSearchAfter;
    private boolean hasMore;
}
