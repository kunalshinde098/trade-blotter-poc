package com.trading.blotter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSearchRequest {

    private String book;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDateTo;

    private String instrument;

    private String trader;

    private String counterparty;

    private String status;

    // Pagination
    @Min(1)
    @Max(1000)
    @Builder.Default
    private int pageSize = 100;

    private Object[] searchAfter;

    // CRITICAL: Field projection to avoid loading all 400 fields
    private List<String> requestedFields;

    // Sorting
    @Builder.Default
    private String sortField = "tradeDate";

    @Builder.Default
    private String sortOrder = "desc"; // asc or desc
}