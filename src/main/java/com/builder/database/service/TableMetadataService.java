package com.builder.database.service;

import com.builder.database.model.TableDefinitionRequest;

public interface TableMetadataService {
    TableDefinitionRequest getTableDefinition(String schemaName, String tableName, boolean includeIndexes);
}
