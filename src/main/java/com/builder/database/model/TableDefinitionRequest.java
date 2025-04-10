package com.builder.database.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDefinitionRequest {
    private String schemaName;
    private String tableName;
    private List<ColumnDefinition> columns;
    private List<IndexDefinition> indexes;
    private boolean temporaryWriteTable;
}
