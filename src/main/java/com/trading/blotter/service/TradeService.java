package com.trading.blotter.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.trading.blotter.dto.TradeSearchRequest;
import com.trading.blotter.dto.TradeSearchResponse;
import com.trading.blotter.model.TradeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    //private final ReactiveElasticsearchTemplate elasticsearchTemplate;
    private final ReactiveElasticsearchOperations elasticsearchTemplate;

    public Mono<TradeSearchResponse> searchTrades(TradeSearchRequest request) {
        log.info("Searching trades - pageSize: {}, requestedFields: {}, searchAfter: {}",
                request.getPageSize(),
                request.getRequestedFields() != null ? request.getRequestedFields().size() : "all",
                request.getSearchAfter() != null ? "present" : "null");

        // Validate pageSize to prevent excessive queries
        if (request.getPageSize() > 1000) {
            log.warn("PageSize {} exceeds maximum, limiting to 1000", request.getPageSize());
            request.setPageSize(1000);
        }

        NativeQuery query = buildQuery(request);

        return elasticsearchTemplate.search(query, TradeDocument.class)
                .collectList()
                .map(hits -> {
                    List<TradeDocument> trades = hits.stream()
                            .map(SearchHit::getContent)
                            .toList();

                    Object[] lastSearchAfter = hits.isEmpty() ? null :
                            hits.get(hits.size() - 1).getSortValues().toArray();

                    // hasMore is true ONLY if we got a FULL page
                    boolean hasMore = trades.size() >= request.getPageSize();

                    log.info("Returning {} trades, hasMore: {}, lastSearchAfter: {}",
                            trades.size(),
                            hasMore,
                            lastSearchAfter != null ? "present" : "null");

                    return TradeSearchResponse.builder()
                            .trades(trades)
                            .totalCount(hits.size())
                            .lastSearchAfter(lastSearchAfter)
                            .hasMore(trades.size() >= request.getPageSize())
                            .build();
                })
                .doOnSuccess(response -> log.info("Search completed, returned {} trades", response.getTotalCount()))
                .doOnError(error -> log.error("Search failed", error));
    }

    private NativeQuery buildQuery(TradeSearchRequest request) {
        List<Query> mustClauses = new ArrayList<>();

        // Book filter
        if (request.getBook() != null && !request.getBook().isEmpty()) {
            mustClauses.add(Query.of(q -> q.term(t -> t
                    .field("book")
                    .value(request.getBook()))));
        }

        // Trade date range filter
        if (request.getTradeDateFrom() != null || request.getTradeDateTo() != null) {
            mustClauses.add(Query.of(q -> q.range(r -> {
                var range = r.field("tradeDate");
                if (request.getTradeDateFrom() != null) {
                    // TODO: Enable date range filtering once date serialization is handled
                    //range.gte(request.getTradeDateFrom().toString());
                }
                if (request.getTradeDateTo() != null) {
                    // TODO: Enable date range filtering once date serialization is handled
                    //range.lte(request.getTradeDateTo().toString());
                }
                return range;
            })));
        }

        // Instrument filter
        if (request.getInstrument() != null && !request.getInstrument().isEmpty()) {
            mustClauses.add(Query.of(q -> q.term(t -> t
                    .field("instrument")
                    .value(request.getInstrument()))));
        }

        // Trader filter
        if (request.getTrader() != null && !request.getTrader().isEmpty()) {
            mustClauses.add(Query.of(q -> q.term(t -> t
                    .field("trader")
                    .value(request.getTrader()))));
        }

        // Counterparty filter
        if (request.getCounterparty() != null && !request.getCounterparty().isEmpty()) {
            mustClauses.add(Query.of(q -> q.term(t -> t
                    .field("counterparty")
                    .value(request.getCounterparty()))));
        }

        // Status filter
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            mustClauses.add(Query.of(q -> q.term(t -> t
                    .field("status")
                    .value(request.getStatus()))));
        }

        // If no filters, match all
        BoolQuery boolQuery = mustClauses.isEmpty() ?
                BoolQuery.of(b -> b.must(Query.of(q -> q.matchAll(m -> m)))) :
                BoolQuery.of(b -> b.must(mustClauses));

        // Build native query
        var nativeQueryBuilder = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolQuery)))
                .withMaxResults(request.getPageSize());

        // CRITICAL: Field projection - only fetch requested fields
        if (request.getRequestedFields() != null && !request.getRequestedFields().isEmpty()) {
            String[] fields = request.getRequestedFields().toArray(new String[0]);
            nativeQueryBuilder.withSourceFilter(new FetchSourceFilter(fields, null));
            log.debug("Applied source filter for {} fields", fields.length);
        }

        // Sorting
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) ?
                SortOrder.Asc : SortOrder.Desc;

        nativeQueryBuilder.withSort(s -> s.field(f -> f
                .field(request.getSortField())
                .order(sortOrder)));

        // Add search_after for deep pagination
        if (request.getSearchAfter() != null && request.getSearchAfter().length > 0) {
            nativeQueryBuilder.withSearchAfter(List.of(request.getSearchAfter()));
            log.debug("Using search_after pagination");
        }

        return nativeQueryBuilder.build();
    }
}