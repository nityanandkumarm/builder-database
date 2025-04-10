package com.builder.database.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexDefinitionDto {
    private List<String> columnNames;
    private String indexType; // BTREE, HASH, etc.
    private boolean unique;
}
