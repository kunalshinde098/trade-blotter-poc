package com.trading.blotter.controller;

import com.trading.blotter.dto.PriceUpdate;
import com.trading.blotter.dto.TradeSearchRequest;
import com.trading.blotter.dto.TradeSearchResponse;
import com.trading.blotter.model.ColumnMetadata;
import com.trading.blotter.service.ColumnMetadataService;
import com.trading.blotter.service.PriceStreamService;
import com.trading.blotter.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeController {

    private final TradeService tradeService;
    private final ColumnMetadataService columnMetadataService;
    private final PriceStreamService priceStreamService;

    @PostMapping("/search")
    public Mono<TradeSearchResponse> searchTrades(@Valid @RequestBody TradeSearchRequest request) {
        log.info("Received search request for book: {}, fields: {}",
                request.getBook(),
                request.getRequestedFields() != null ? request.getRequestedFields().size() : "all");
        return tradeService.searchTrades(request);
    }

    @GetMapping("/columns")
    public Mono<List<ColumnMetadata>> getAllColumns() {
        return Mono.just(columnMetadataService.getAllColumns());
    }

    @GetMapping("/columns/default")
    public Mono<List<ColumnMetadata>> getDefaultColumns() {
        return Mono.just(columnMetadataService.getDefaultGTIDColumns());
    }

    @GetMapping("/columns/category/{category}")
    public Mono<List<ColumnMetadata>> getColumnsByCategory(@PathVariable String category) {
        return Mono.just(columnMetadataService.getColumnsByCategory(category));
    }

    /**
     * Server-Sent Events (SSE) endpoint for real-time price updates
     * Chosen over WebSocket for:
     * - Unidirectional flow (server → client)
     * - Automatic reconnection
     * - HTTP/2 multiplexing
     * - Simpler backpressure handling
     */
    @GetMapping(value = "/prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<PriceUpdate>> streamPrices(
            @RequestParam(required = false) String tradeIds) {  // ADD THIS

        Set<String> subscribedTrades = tradeIds != null
                ? Set.of(tradeIds.split(","))
                : Collections.emptySet();

        return priceStreamService.getPriceStream(subscribedTrades)
                .filter(update -> subscribedTrades.isEmpty() ||
                        subscribedTrades.contains(update.getTradeId()))  // ADD THIS
                .map(update -> ServerSentEvent.<PriceUpdate>builder()
                        .id(String.valueOf(update.getTimestamp()))
                        .event("price-update")
                        .data(update)
                        .build())
                .timeout(Duration.ofMinutes(30), Flux.empty());
    }
}