package com.trading.blotter.service;

import com.trading.blotter.model.ColumnMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColumnMetadataService {

    private static final List<ColumnMetadata> ALL_COLUMNS = initializeColumns();

    public List<ColumnMetadata> getAllColumns() {
        return new ArrayList<>(ALL_COLUMNS);
    }

    public List<ColumnMetadata> getDefaultGTIDColumns() {
        return ALL_COLUMNS.stream()
                .filter(ColumnMetadata::isDefaultVisible)
                .collect(Collectors.toList());
    }

    public List<ColumnMetadata> getColumnsByCategory(String category) {
        return ALL_COLUMNS.stream()
                .filter(col -> category.equals(col.getCategory()))
                .collect(Collectors.toList());
    }

    private static List<ColumnMetadata> initializeColumns() {
        List<ColumnMetadata> columns = new ArrayList<>();

        // Core fields - always visible by default (GTID standard)
        columns.add(buildColumn("tradeId", "Trade ID", "string", true, true, 120, "Core",
                "Unique trade identifier", true));
        columns.add(buildColumn("book", "Book", "string", true, true, 120, "Core",
                "Trading book", true));
        columns.add(buildColumn("tradeDate", "Trade Date", "date", true, true, 130, "Core",
                "Date of trade execution", true));
        columns.add(buildColumn("instrument", "Instrument", "string", true, true, 150, "Core",
                "Financial instrument", true));
        columns.add(buildColumn("trader", "Trader", "string", true, true, 120, "Core",
                "Trader name", true));
        columns.add(buildColumn("counterparty", "Counterparty", "string", true, true, 150, "Core",
                "Counterparty name", true));
        columns.add(buildColumn("notional", "Notional", "number", true, true, 130, "Core",
                "Notional amount", true));
        columns.add(buildColumn("currency", "Currency", "string", true, true, 90, "Core",
                "Currency code", true));
        columns.add(buildColumn("tradeType", "Type", "string", true, true, 100, "Core",
                "Trade type", true));
        columns.add(buildColumn("status", "Status", "string", true, true, 100, "Core",
                "Trade status", true));

        // Risk fields - visible by default
        columns.add(buildColumn("pnl", "P&L", "number", true, true, 130, "Risk",
                "Profit and Loss", true));
        columns.add(buildColumn("mtm", "MTM", "number", true, true, 130, "Risk",
                "Mark to Market", true));
        columns.add(buildColumn("delta", "Delta", "number", true, true, 110, "Risk",
                "Delta risk", true));
        columns.add(buildColumn("gamma", "Gamma", "number", true, true, 110, "Risk",
                "Gamma risk", true));
        columns.add(buildColumn("vega", "Vega", "number", true, true, 110, "Risk",
                "Vega risk", true));
        columns.add(buildColumn("theta", "Theta", "number", true, true, 110, "Risk",
                "Theta risk", true));

        // Settlement fields - not visible by default
        columns.add(buildColumn("settlementDate", "Settlement Date", "date", true, true, 140, "Settlement",
                "Settlement date", false));
        columns.add(buildColumn("maturityDate", "Maturity Date", "date", true, true, 140, "Settlement",
                "Maturity date", false));

        // Pricing fields - not visible by default
        columns.add(buildColumn("fixedRate", "Fixed Rate", "number", true, true, 120, "Pricing",
                "Fixed rate", false));
        columns.add(buildColumn("floatingRate", "Floating Rate", "number", true, true, 130, "Pricing",
                "Floating rate", false));

        // Generate remaining ~380 fields (simulating 400 total fields)
        // These represent additional regulatory, pricing, and operational fields
        String[] categories = {"Regulatory", "Pricing", "Settlement", "Operations", "Compliance", "Audit"};
        String[] dataTypes = {"string", "number", "date", "boolean"};

        for (int i = 1; i <= 380; i++) {
            String category = categories[i % categories.length];
            String dataType = dataTypes[i % dataTypes.length];

            columns.add(buildColumn(
                    "field" + i,
                    "Field " + i,
                    dataType,
                    false, // not filterable
                    false, // not sortable
                    100,
                    category,
                    "Additional field " + i,
                    false // not visible by default
            ));
        }

        return columns;
    }

    private static ColumnMetadata buildColumn(String fieldName, String displayName, String dataType,
                                              boolean filterable, boolean sortable, int width,
                                              String category, String description, boolean defaultVisible) {
        return ColumnMetadata.builder()
                .fieldName(fieldName)
                .displayName(displayName)
                .dataType(dataType)
                .filterable(filterable)
                .sortable(sortable)
                .width(width)
                .category(category)
                .description(description)
                .defaultVisible(defaultVisible)
                .build();
    }
}