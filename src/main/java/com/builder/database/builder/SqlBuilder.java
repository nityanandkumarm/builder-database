package com.builder.database.builder;

import com.builder.database.model.AggregationRequest;
import com.builder.database.model.IndexDefinition;
import com.builder.database.model.TableDefinitionRequest;

import java.util.List;
import java.util.Map;

public interface SqlBuilder {

    String buildCreateTableSql(TableDefinitionRequest request);

    String buildCreateTempWriteTableSql(TableDefinitionRequest request);

    String buildCreateIndexSql(String schemaName, String tableName, IndexDefinition index);

    List<String> buildAllCreateIndexSql(String schemaName, String tableName, List<IndexDefinition> indexes);

    String buildSelectQuerySql(
            String schemaName,
            String tableName,
            List<String> columns,
            Map<String, Object> filters,
            List<AggregationRequest> aggregations
    );
    String buildFlushFromTempToActualSql(TableDefinitionRequest definition, int batchSize);
}
