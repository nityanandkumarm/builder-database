package com.builder.database.service;

import com.builder.database.model.ColumnDefinition;
import com.builder.database.model.IndexDefinition;
import com.builder.database.model.TableDefinitionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableMetadataServiceImpl implements TableMetadataService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public TableDefinitionRequest getTableDefinition(String schema, String table, boolean includeIndexes) {
        boolean actualExists = tableExists(schema, table);
        boolean tempExists = tableExists(schema, "__tmp_write_" + table);

        String doesTempExists = tempExists ? "__tmp_write_" + table : null;
        String targetTable = actualExists ? table : doesTempExists;
        if (targetTable == null) {
            throw new IllegalArgumentException("No table or temp write table found in schema: " + schema);
        }

        List<ColumnDefinition> columns = getColumnsFromMetadata(schema, targetTable);
        List<IndexDefinition> indexes = (includeIndexes && actualExists)
                ? getIndexes(schema, table)
                : List.of();

        return TableDefinitionRequest.builder()
                .schemaName(schema)
                .tableName(table)
                .columns(columns)
                .indexes(indexes)
                .temporaryWriteTable(doesTempExists != null)
                .build();
    }
    private boolean tableExists(String schema, String tableName) {
        String sql = """
        SELECT EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = ? AND table_name = ?
        )
    """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, schema, tableName));
    }

    private List<ColumnDefinition> getColumnsFromMetadata(String schema, String table) {
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource(), "No DataSource Found").getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            List<ColumnDefinition> columns = new ArrayList<>();

            try (var rs = metaData.getColumns(null, schema, table, null)) {
                while (rs.next()) {
                    columns.add(ColumnDefinition.builder()
                            .name(rs.getString("COLUMN_NAME"))
                            .type(rs.getString("TYPE_NAME"))
                            .notNull(!"YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                            .defaultValue(rs.getString("COLUMN_DEF"))
                            .primaryKey(false)
                            .build());
                }
            }

            try (var pkRs = metaData.getPrimaryKeys(null, schema, table)) {
                Set<String> primaryKeys = new HashSet<>();
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
                columns.forEach(col -> col.setPrimaryKey(primaryKeys.contains(col.getName())));
            }

            return columns;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read column metadata for " + schema + "." + table, ex);
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
