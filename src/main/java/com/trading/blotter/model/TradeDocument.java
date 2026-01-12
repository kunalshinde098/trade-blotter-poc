package com.trading.blotter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "trades")
public class TradeDocument {

    @Id
    private String tradeId;

    @Field(type = FieldType.Keyword)
    private String book;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;

    @Field(type = FieldType.Keyword)
    private String instrument;

    @Field(type = FieldType.Keyword)
    private String trader;

    @Field(type = FieldType.Keyword)
    private String counterparty;

    @Field(type = FieldType.Double)
    private BigDecimal notional;

    @Field(type = FieldType.Double)
    private BigDecimal pnl;

    @Field(type = FieldType.Double)
    private BigDecimal mtm;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Keyword)
    private String tradeType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settlementDate;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;

    @Field(type = FieldType.Double)
    private BigDecimal fixedRate;

    @Field(type = FieldType.Double)
    private BigDecimal floatingRate;

    @Field(type = FieldType.Double)
    private BigDecimal delta;

    @Field(type = FieldType.Double)
    private BigDecimal gamma;

    @Field(type = FieldType.Double)
    private BigDecimal vega;

    @Field(type = FieldType.Double)
    private BigDecimal theta;

    // Dynamic additional fields (remaining ~380 fields)
    @Field(type = FieldType.Object, enabled = true)
    private Map<String, Object> additionalFields;

    // Transient field for search_after pagination
    private transient Object[] searchAfter;
}