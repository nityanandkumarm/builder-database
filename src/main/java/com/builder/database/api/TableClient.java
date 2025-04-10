package com.builder.database.api;

import com.builder.database.dto.*;

import java.util.List;

public interface TableClient {

    void createTable(TableCreateRequestDto requestDto);

    void createIndex(String schemaName, String tableName, IndexDefinitionDto indexDto);

    List<GenericResultRowDto> selectQuery(SelectQueryRequestDto requestDto);
}
