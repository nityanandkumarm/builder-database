package com.builder.database.service;

import com.builder.database.config.errors.DatabaseOperationException;
import com.builder.database.model.ColumnDefinition;
import com.builder.database.model.IndexDefinition;
import com.builder.database.model.TableDefinitionRequest;
import com.builder.database.repository.TableMetadataRepository;
import com.builder.database.repository.TempTableMetadataRepository;
import com.builder.database.entity.TableMetadata;
import com.builder.database.entity.TempTableMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableMetadataServiceImpl implements TableMetadataService {

    private final JdbcTemplate jdbcTemplate;
    private final TableMetadataRepository tableMetadataRepository;
    private final TempTableMetadataRepository tempTableMetadataRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "tableDefinitions", key = "#schema + '.' + #table + '.' + #includeIndexes")
    public TableDefinitionRequest getTableDefinition(String schema, String table, boolean includeIndexes) {
        // Check main table metadata
        Optional<TableMetadata> tableMetadata = tableMetadataRepository.findBySchemaNameAndTableName(schema, table);

        if (tableMetadata.isPresent()) {
            List<String> columnNames = parseColumnsJson(tableMetadata.get().getColumnsJson());
            List<ColumnDefinition> columns = getColumnsFromMetadata(schema, table, columnNames);
            List<IndexDefinition> indexes = includeIndexes ? getIndexes(schema, table) : List.of();

            return TableDefinitionRequest.builder()
                    .schemaName(schema)
                    .tableName(table)
                    .columns(columns)
                    .indexes(indexes)
                    .temporaryWriteTable(false)
                    .build();
        }

        // Check temp table metadata
        Optional<TempTableMetadata> tempMetadata = tempTableMetadataRepository
                .findBySchemaNameAndOriginalTableName(schema, table);

        if (tempMetadata.isPresent()) {
            List<String> columnNames = parseColumnsJson(tempMetadata.get().getColumnsJson());
            List<ColumnDefinition> columns = getColumnsFromMetadata(schema, tempMetadata.get().getTempTableName(), columnNames);

            return TableDefinitionRequest.builder()
                    .schemaName(schema)
                    .tableName(table)
                    .columns(columns)
                    .indexes(List.of())
                    .temporaryWriteTable(true)
                    .build();
        }

        throw new IllegalArgumentException("No table or temp write table found in schema: " + schema);
    }

    private List<String> parseColumnsJson(String columnsJson) {
        try {
            return objectMapper.readValue(columnsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse columns JSON", e);
            return List.of();
        }
    }

    private List<ColumnDefinition> getColumnsFromMetadata(String schema, String table, List<String> columnNames) {
        try (var connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            var metaData = connection.getMetaData();
            List<ColumnDefinition> columns = new ArrayList<>();

            try (var rs = metaData.getColumns(null, schema, table, null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    if (columnNames.contains(columnName)) {
                        columns.add(ColumnDefinition.builder()
                                .name(columnName)
                                .type(rs.getString("TYPE_NAME"))
                                .notNull(!"YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                                .defaultValue(rs.getString("COLUMN_DEF"))
                                .primaryKey(false)
                                .build());
                    }
                }
            }

            // Get primary key information
            try (var pkRs = metaData.getPrimaryKeys(null, schema, table)) {
                Set<String> primaryKeys = new HashSet<>();
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
                columns.forEach(col -> col.setPrimaryKey(primaryKeys.contains(col.getName())));
            }

            return columns;
        } catch (Exception ex) {
            throw new DatabaseOperationException("Failed to read column metadata for " + schema + "." + table, ex);
        }
    }

    private List<IndexDefinition> getIndexes(String schema, String table) {
        String sql = "SELECT indexdef FROM pg_indexes WHERE schemaname = ? AND tablename = ?";

        return jdbcTemplate.query(sql, new Object[]{schema, table}, (rs, rowNum) -> {
            String indexDef = rs.getString("indexdef");
            return parseIndexDefinition(indexDef);
        }).stream().filter(Objects::nonNull).toList();
    }

    private IndexDefinition parseIndexDefinition(String indexDef) {
        try {
            boolean unique = indexDef.toUpperCase().contains("UNIQUE");

            // Extract columns between ( ... )
            String[] parts = indexDef.split("ON")[1].split("\\(");
            String columnPart = parts[1].split("\\)")[0];
            List<String> columns = Arrays.stream(columnPart.split(","))
                    .map(String::trim)
                    .map(c -> c.replace("\"", ""))
                    .toList();

            // Extract index type after USING (optional)
            String indexType = null;
            if (indexDef.toUpperCase().contains("USING")) {
                indexType = indexDef.split("USING")[1].trim().split(" ")[0].toUpperCase();
            }

            return IndexDefinition.builder()
                    .columnNames(columns)
                    .indexType(indexType)
                    .unique(unique)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse index definition: {}", indexDef, e);
            return null;
        }
    }


}
