package com.builder.database.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregationRequest {
    private String column;
    private String function; // e.g., SUM, AVG, COUNT
    private String alias;    // name for the aggregated column
}
