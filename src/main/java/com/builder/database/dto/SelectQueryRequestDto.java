package com.builder.database.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectQueryRequestDto {
    private String schemaName;
    private String tableName;
    private List<String> columns; // Specific fields to retrieve
    private Map<String, Object> filters; // Optional where conditions
    private List<AggregationRequestDto> aggregations; // Optional rollups
}
