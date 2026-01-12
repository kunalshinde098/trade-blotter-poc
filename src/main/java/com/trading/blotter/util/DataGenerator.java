package com.trading.blotter.util;

import com.trading.blotter.model.TradeDocument;
import com.trading.blotter.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataGenerator implements CommandLineRunner {

    private final TradeRepository tradeRepository;
    private final Random random = new Random();

    private static final String[] BOOKS = {
            "EMEA_RATES", "US_RATES", "ASIA_RATES", "EMEA_FX", "US_FX"
    };

    private static final String[] INSTRUMENTS = {
            "EUR_IRS_10Y", "USD_IRS_5Y", "GBP_IRS_7Y", "JPY_IRS_3Y",
            "CHF_IRS_10Y", "AUD_IRS_5Y", "CAD_IRS_7Y"
    };

    private static final String[] TRADERS = {
            "john.smith", "jane.doe", "bob.wilson", "alice.johnson",
            "charlie.brown", "david.lee", "emma.davis", "frank.moore"
    };

    private static final String[] COUNTERPARTIES = {
            "BANK_ABC", "BANK_XYZ", "BANK_123", "BROKER_A", "BROKER_B",
            "HEDGE_FUND_1", "ASSET_MGR_1", "INSURANCE_CO_1"
    };

    private static final String[] STATUSES = {
            "ACTIVE", "PENDING", "SETTLED", "CANCELLED"
    };

    @Override
    public void run(String... args) {
        // Check if we should generate data
        if (args.length > 0 && "generate-data".equals(args[0])) {
            int count = args.length > 1 ? Integer.parseInt(args[1]) : 10000;
            generateTrades(count);
        }
    }

    private void generateTrades(int count) {
        log.info("Generating {} trades...", count);

        Flux.range(1, count)
                .map(this::createTrade)
                .flatMap(tradeRepository::save)
                .buffer(1000)
                .doOnNext(batch -> log.info("Saved {} trades", batch.size()))
                .blockLast();

        log.info("Data generation complete!");
    }

    private TradeDocument createTrade(int index) {
        String tradeId = String.format("TRD%06d", index);
        LocalDate tradeDate = LocalDate.now().minusDays(random.nextInt(365));

        BigDecimal notional = BigDecimal.valueOf(
                (random.nextInt(9000000) + 1000000) // 1M to 10M
        );

        BigDecimal pnl = BigDecimal.valueOf(
                (random.nextDouble() - 0.5) * 200000 // -100K to +100K
        );

        BigDecimal mtm = pnl.multiply(BigDecimal.valueOf(1.05));

        Map<String, Object> additionalFields = new HashMap<>();
        for (int i = 1; i <= 380; i++) {
            additionalFields.put("field" + i, "value_" + random.nextInt(1000));
        }

        return TradeDocument.builder()
                .tradeId(tradeId)
                .book(BOOKS[random.nextInt(BOOKS.length)])
                .tradeDate(tradeDate)
                .instrument(INSTRUMENTS[random.nextInt(INSTRUMENTS.length)])
                .trader(TRADERS[random.nextInt(TRADERS.length)])
                .counterparty(COUNTERPARTIES[random.nextInt(COUNTERPARTIES.length)])
                .notional(notional)
                .pnl(pnl)
                .mtm(mtm)
                .currency("USD")
                .tradeType("IRS")
                .status(STATUSES[random.nextInt(STATUSES.length)])
                .settlementDate(tradeDate.plusDays(2))
                .maturityDate(tradeDate.plusYears(5 + random.nextInt(6)))
                .fixedRate(BigDecimal.valueOf(2.0 + random.nextDouble() * 2))
                .floatingRate(BigDecimal.valueOf(2.0 + random.nextDouble() * 2))
                .delta(BigDecimal.valueOf((random.nextDouble() - 0.5) * 2000))
                .gamma(BigDecimal.valueOf((random.nextDouble() - 0.5) * 100))
                .vega(BigDecimal.valueOf((random.nextDouble() - 0.5) * 1000))
                .theta(BigDecimal.valueOf((random.nextDouble() - 0.5) * 50))
                .additionalFields(additionalFields)
                .build();
    }
}