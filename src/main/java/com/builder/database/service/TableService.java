package com.builder.database.service;

import com.builder.database.dto.GenericResultRowDto;
import com.builder.database.dto.IndexDefinitionDto;
import com.builder.database.dto.SelectQueryRequestDto;
import com.builder.database.dto.TableCreateRequestDto;

import java.util.List;

public interface TableService {
    void createTable(TableCreateRequestDto request);
    List<GenericResultRowDto> executeSelectQuery(SelectQueryRequestDto request);
    void createIndex(String schemaName, String tableName, IndexDefinitionDto index);
    void flushTempToActual(String schemaName, String tableName);
}
