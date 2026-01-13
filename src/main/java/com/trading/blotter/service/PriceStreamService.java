package com.trading.blotter.service;

import com.trading.blotter.dto.PriceUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

@Service
@Slf4j
public class PriceStreamService {

    //private final Sinks.Many<PriceUpdate> priceSink = Sinks.many().multicast().onBackpressureBuffer(256);
    private final Sinks.Many<PriceUpdate> priceSink = Sinks.many().multicast().onBackpressureBuffer(1024, false);
    private final Random random = new Random();
    private final ConcurrentHashMap<String, PriceState> priceCache = new ConcurrentHashMap<>();

    private static class PriceState {
        BigDecimal pnl;
        BigDecimal mtm;
        BigDecimal delta;
        BigDecimal gamma;

        PriceState(BigDecimal pnl, BigDecimal mtm, BigDecimal delta, BigDecimal gamma) {
            this.pnl = pnl;
            this.mtm = mtm;
            this.delta = delta;
            this.gamma = gamma;
        }
    }

    public PriceStreamService() {
        startPriceGenerator();
    }

    public Flux<PriceUpdate> getPriceStream() {
        return priceSink.asFlux()
                .doOnSubscribe(s -> log.info("New SSE subscriber connected"))
                .doOnCancel(() -> log.info("SSE subscriber disconnected"))
                .doOnError(error -> log.error("Error in price stream", error));
    }

    private void startPriceGenerator() {
        // Simulate price ticks every 1000ms
        Flux.interval(Duration.ofMillis(500))
                .onBackpressureDrop(tick -> log.warn("Dropping price tick due to backpressure"))
                .map(tick -> generatePriceUpdate())
                .subscribe(
                        update -> {
                            /*Sinks.EmitResult result = priceSink.tryEmitNext(update);
                            if (result.isFailure()) {
                                log.warn("Failed to emit price update: {}", result);
                            }*/
                            Sinks.EmitResult result = priceSink.tryEmitNext(update);
                            while (result == Sinks.EmitResult.FAIL_OVERFLOW) {
                                log.debug("Buffer full, retrying...");
                                LockSupport.parkNanos(10_000_000); // 10ms
                                result = priceSink.tryEmitNext(update);
                            }
                            if (result.isFailure() && result != Sinks.EmitResult.FAIL_OVERFLOW) {
                                log.warn("Failed to emit price update: {}", result);
                            }
                        },
                        error -> log.error("Price generation error", error)
                );

        log.info("Price tick generator started");
    }

    private PriceUpdate generatePriceUpdate() {
        // Randomly select a trade to update (simulating 100k trades)
        String tradeId = "TRD" + String.format("%06d", random.nextInt(100000) + 1);

        // Get or initialize price state
        PriceState state = priceCache.computeIfAbsent(tradeId, k ->
                new PriceState(
                        BigDecimal.valueOf(random.nextDouble() * 1000000),
                        BigDecimal.valueOf(random.nextDouble() * 1050000),
                        BigDecimal.valueOf(random.nextDouble() * 1000),
                        BigDecimal.valueOf(random.nextDouble() * 100)
                )
        );

        // Generate realistic price movement (mean-reverting random walk)
        BigDecimal priceChange = BigDecimal.valueOf((random.nextDouble() - 0.5) * 10000);
        BigDecimal newPnl = state.pnl.add(priceChange).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newMtm = newPnl.multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP);

        // Update Greeks slightly
        BigDecimal newDelta = state.delta.add(BigDecimal.valueOf((random.nextDouble() - 0.5) * 10))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal newGamma = state.gamma.add(BigDecimal.valueOf((random.nextDouble() - 0.5)))
                .setScale(2, RoundingMode.HALF_UP);

        // Update cache
        state.pnl = newPnl;
        state.mtm = newMtm;
        state.delta = newDelta;
        state.gamma = newGamma;

        // Return delta update with only changed fields
        return PriceUpdate.withGreeks(tradeId, newPnl, newMtm, newDelta, newGamma);
    }

    public void stopPriceGenerator() {
        priceSink.tryEmitComplete();
        log.info("Price tick generator stopped");
    }
}