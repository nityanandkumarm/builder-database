package com.builder.database.api.impl;

import com.builder.database.api.TableClient;
import com.builder.database.dto.GenericResultRowDto;
import com.builder.database.dto.IndexDefinitionDto;
import com.builder.database.dto.SelectQueryRequestDto;
import com.builder.database.dto.TableCreateRequestDto;
import com.builder.database.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalTableClient implements TableClient {

    private final TableService tableService;

    @Autowired
    public LocalTableClient(final TableService tableService){
        this.tableService = tableService;
    }

    @Override
    public void createTable(TableCreateRequestDto requestDto) {
        tableService.createTable(requestDto);
    }

    @Override
    public void createIndex(String schemaName, String tableName, IndexDefinitionDto indexDto) {
        tableService.createIndex(schemaName, tableName, indexDto);
    }

    @Override
    public List<GenericResultRowDto> selectQuery(SelectQueryRequestDto requestDto) {
        return tableService.executeSelectQuery(requestDto);
    }
}
