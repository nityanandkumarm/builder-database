package com.builder.database.service;

import com.builder.database.entity.TableMetadata;
import com.builder.database.entity.TempTableMetadata;
import com.builder.database.repository.TableMetadataRepository;
import com.builder.database.repository.TempTableMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class DatabaseMetadataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final TableMetadataRepository tableMetadataRepository;
    private final TempTableMetadataRepository tempTableMetadataRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String TEMP_PREFIX = "__tmp_write_";

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, List<String>> allColumns = fetchColumnsGroupedByTable();
        Set<String> tempTables = fetchTempTables();
        populateTableMetadata(allColumns, tempTables);
        populateTempTableMetadata(allColumns);
    }

    private Map<String, List<String>> fetchColumnsGroupedByTable() {
        String sql = "SELECT table_schema, table_name, column_name FROM information_schema.columns " +
                "WHERE table_schema NOT IN ('information_schema', 'pg_catalog')";
        return jdbcTemplate.query(sql, rs -> {
            Map<String, List<String>> map = new HashMap<>();
            while (rs.next()) {
                String key = rs.getString("table_schema") + "." + rs.getString("table_name");
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(rs.getString("column_name"));
            }
            return map;
        });
    }

    private Set<String> fetchTempTables() {
        String sql = "SELECT table_schema, table_name FROM information_schema.tables " +
                "WHERE table_name LIKE '" + TEMP_PREFIX + "%'";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getString("table_schema") + "." + rs.getString("table_name")
        ));
    }

    private void populateTableMetadata(Map<String, List<String>> allColumns, Set<String> tempTables) {
        allColumns.entrySet().stream()
                .filter(e -> !e.getKey().contains(TEMP_PREFIX))
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\.");
                    String schema = parts[0];
                    String table = parts[1];
                    String tempKey = schema + "." + TEMP_PREFIX + table;

                    return TableMetadata.builder()
                            .schemaName(schema)
                            .tableName(table)
                            .hasTempTable(tempTables.contains(tempKey))
                            .columnsJson(writeJson(entry.getValue()))
                            .build();
                })
                .forEach(tableMetadataRepository::save);
    }

    private void populateTempTableMetadata(Map<String, List<String>> allColumns) {
        allColumns.entrySet().stream()
                .filter(e -> e.getKey().contains(TEMP_PREFIX))
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\.");
                    String schema = parts[0];
                    String tempTable = parts[1];
                    String originalTable = tempTable.replaceFirst(TEMP_PREFIX, "");

                    return TempTableMetadata.builder()
                            .schemaName(schema)
                            .tempTableName(tempTable)
                            .originalTableName(originalTable)
                            .columnsJson(writeJson(entry.getValue()))
                            .build();
                })
                .forEach(tempTableMetadataRepository::save);
    }

    private String writeJson(List<String> columnNames) {
        try {
            return objectMapper.writeValueAsString(columnNames);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON for columns", e);
        }
    }
}
