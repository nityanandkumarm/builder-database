package com.builder.database.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexDefinition {
    private List<String> columnNames;
    private String indexType;  // e.g., btree, gin, etc.
    private boolean unique;
}
