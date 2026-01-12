package com.trading.blotter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {
    private String fieldName;
    private String displayName;
    private String dataType;
    private boolean filterable;
    private boolean sortable;
    private int width;
    private String category;
    private String description;
    private boolean defaultVisible;
}