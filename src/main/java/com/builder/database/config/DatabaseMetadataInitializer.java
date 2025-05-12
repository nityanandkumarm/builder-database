package com.builder.database.config;

import com.builder.database.entity.TableMetadata;
import com.builder.database.entity.TempTableMetadata;
import com.builder.database.repository.TableMetadataRepository;
import com.builder.database.repository.TempTableMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerErrorException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class DatabaseMetadataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final TableMetadataRepository tableMetadataRepository;
    private final TempTableMetadataRepository tempTableMetadataRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String TEMP_PREFIX = "__tmp_write_";

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            // Clear existing data using TRUNCATE
            jdbcTemplate.execute("TRUNCATE TABLE table_metadata, temp_table_metadata RESTART IDENTITY CASCADE");

            Map<String, List<String>> allColumns = fetchColumnsGroupedByTable();
            Set<String> tempTables = fetchTempTables();
            if(allColumns == null || allColumns.isEmpty()) {
                return;
            }
            populateTableMetadata(allColumns, tempTables);
            populateTempTableMetadata(allColumns);
        } catch (Exception e) {
            throw new ServerErrorException("Failed to initialize metadata tables", e);
        }
    }

    private Set<String> fetchTempTables() {
        String sql = "SELECT table_schema, table_name FROM information_schema.tables " +
                "WHERE table_name LIKE '" + TEMP_PREFIX + "%'";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getString("table_schema") + "." + rs.getString("table_name")
        ));
    }

    private void populateTableMetadata(Map<String, List<String>> allColumns, Set<String> tempTables) {
        List<TableMetadata> tableMetadataList = allColumns.entrySet().stream()
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
                .toList();

        tableMetadataRepository.saveAll(tableMetadataList);
    }

    private void populateTempTableMetadata(Map<String, List<String>> allColumns) {
        List<TempTableMetadata> tempTableMetadataList = allColumns.entrySet().stream()
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
                .toList();

        tempTableMetadataRepository.saveAll(tempTableMetadataList);
    }

    private String writeJson(List<String> columnNames) {
        try {
            return objectMapper.writeValueAsString(columnNames);
        } catch (Exception e) {
            throw new ServerErrorException("Failed to write JSON for columns", e);
        }
    }

    private Map<String, List<String>> fetchColumnsGroupedByTable() {
        String sql = """
            SELECT table_schema, table_name, column_name 
            FROM information_schema.columns 
            WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'flyway') 
            AND table_schema NOT LIKE 'pg_%'
            ORDER BY table_schema, table_name, ordinal_position
            """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                    rs.getString("table_schema"),
                    rs.getString("table_name"),
                    rs.getString("column_name")
            }).stream().collect(
                    Collectors.groupingBy(
                            row -> (row)[0] + "." + (row)[1],
                            Collectors.mapping(
                                    row -> (String) (row)[2],
                                    Collectors.toList()
                            )
                    )
            );
        } catch (Exception e) {
            log.error("Failed to fetch columns from database", e);
            return new HashMap<>();
        }
    }
}
