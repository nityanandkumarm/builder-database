package com.builder.database.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregationRequestDto {
    private String function; // e.g., SUM, AVG, COUNT
    private String column;
    private String alias;
}
