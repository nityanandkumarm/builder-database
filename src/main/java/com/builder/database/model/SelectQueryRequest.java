package com.builder.database.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectQueryRequest {
    private String schemaName;
    private String tableName;
    private List<String> columns;
    private Map<String, Object> filters;
    private List<AggregationRequest> aggregations;
}
