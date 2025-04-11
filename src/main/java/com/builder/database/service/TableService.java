package com.builder.database.service;

import com.builder.database.dto.GenericResultRowDto;
import com.builder.database.dto.IndexDefinitionDto;
import com.builder.database.dto.SelectQueryRequestDto;
import com.builder.database.dto.TableCreateRequestDto;

import java.util.List;
import java.util.Map;

public interface TableService {
    void createTable(TableCreateRequestDto request);
    List<GenericResultRowDto> executeSelectQuery(SelectQueryRequestDto request);
    void createIndex(String schemaName, String tableName, IndexDefinitionDto index);
    void flushTempToActual(String schemaName, String tableName);
    void insertRows(String schema, String table, List<Map<String, String>> rows);
}
