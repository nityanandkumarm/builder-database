package com.builder.database.builder;

import com.builder.database.model.AggregationRequest;
import com.builder.database.model.ColumnDefinition;
import com.builder.database.model.IndexDefinition;
import com.builder.database.model.TableDefinitionRequest;

import java.util.*;
import java.util.stream.Collectors;

public class PostgresSqlBuilder implements SqlBuilder {

    static final String LAST_UPDATE_DATE = "lastUpdateDate";
    static final String IS_DELETED = "isDeleted";

    @Override
    public String buildCreateTableSql(TableDefinitionRequest request) {
        String fullTableName = quote(request.getSchemaName()) + "." + quote(request.getTableName());

        List<String> columnDefs = request.getColumns().stream()
                .map(this::buildActualColumnDefinition)
                .collect(Collectors.toList());

        columnDefs.add(quote(LAST_UPDATE_DATE) + " TIMESTAMP NOT NULL");
        columnDefs.add(quote(IS_DELETED) + " BOOLEAN NOT NULL DEFAULT FALSE");

        return "CREATE TABLE IF NOT EXISTS " + fullTableName + " (\n" +
                String.join(",\n", columnDefs) + "\n" +
                ");";
    }

    @Override
    public String buildCreateTempWriteTableSql(TableDefinitionRequest request) {
        final String COLUMN_DATA_TYPE = "TEXT";
        String tempTableName = quote(request.getSchemaName()) + "." +
                quote("__tmp_write_" + request.getTableName());

        List<String> columnDefs = request.getColumns().stream()
                .map(col -> quote(col.getName()) + " " + COLUMN_DATA_TYPE)
                .collect(Collectors.toList());

        columnDefs.add(quote(LAST_UPDATE_DATE) + " " + COLUMN_DATA_TYPE);
        columnDefs.add(quote(IS_DELETED) + " " + COLUMN_DATA_TYPE);

        return "CREATE TABLE IF NOT EXISTS " + tempTableName + " (\n" +
                String.join(",\n", columnDefs) + "\n" +
                ");";
    }

    private String buildActualColumnDefinition(ColumnDefinition col) {
        StringBuilder sb = new StringBuilder();
        sb.append(quote(col.getName())).append(" ").append(col.getType());
        if (col.isNotNull()) sb.append(" NOT NULL");
        if (col.isPrimaryKey()) sb.append(" PRIMARY KEY");
        if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
            sb.append(" DEFAULT ").append(col.getDefaultValue());
        }
        return sb.toString();
    }

    @Override
    public String buildCreateIndexSql(String schemaName, String tableName, IndexDefinition index) {
        String indexName = String.format("%s_%s_idx", tableName, String.join("_", index.getColumnNames()));
        String columns = index.getColumnNames().stream().map(this::quote).collect(Collectors.joining(", "));
        String unique = index.isUnique() ? "UNIQUE " : "";
        String type = index.getIndexType() != null ? "USING " + index.getIndexType() : "";

        return String.format("CREATE %sINDEX IF NOT EXISTS %s ON %s.%s %s (%s);",
                unique, quote(indexName), quote(schemaName), quote(tableName), type, columns);
    }

    @Override
    public List<String> buildAllCreateIndexSql(String schemaName, String tableName, List<IndexDefinition> indexes) {
        if (indexes == null) return List.of();
        return indexes.stream()
                .map(index -> buildCreateIndexSql(schemaName, tableName, index))
                .toList();
    }

    @Override
    public String buildSelectQuerySql(String schemaName,
                                      String tableName,
                                      List<String> columns,
                                      Map<String, Object> filters,
                                      List<AggregationRequest> aggregations) {

        String fullTableName = quote(schemaName) + "." + quote(tableName);
        String selectClause = buildSelectClause(columns, aggregations);

        StringBuilder sql = new StringBuilder("SELECT ")
                .append(selectClause)
                .append(" FROM ")
                .append(fullTableName);

        appendWhereClause(sql, filters);
        appendGroupByClause(sql, columns, aggregations);

        return sql.toString();
    }

    private String buildSelectClause(List<String> columns, List<AggregationRequest> aggregations) {
        List<String> selectFields = new ArrayList<>();

        if (columns != null && !columns.isEmpty()) {
            selectFields.addAll(columns.stream().map(this::quote).toList());
        }

        if (aggregations != null) {
            for (AggregationRequest agg : aggregations) {
                String aggExpr = agg.getFunction() + "(" + quote(agg.getColumn()) + ")";
                if (agg.getAlias() != null && !agg.getAlias().isBlank()) {
                    aggExpr += " AS " + quote(agg.getAlias());
                }
                selectFields.add(aggExpr);
            }
        }

        return selectFields.isEmpty() ? "*" : String.join(", ", selectFields);
    }

    private void appendWhereClause(StringBuilder sql, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return;

        String whereClause = filters.entrySet().stream()
                .map(entry -> quote(entry.getKey()) + " = " + formatValue(entry.getValue()))
                .collect(Collectors.joining(" AND "));
        sql.append(" WHERE ").append(whereClause);
    }

    private void appendGroupByClause(StringBuilder sql, List<String> columns, List<AggregationRequest> aggregations) {
        if (aggregations == null || aggregations.isEmpty() || columns == null || columns.isEmpty()) return;

        String groupBy = columns.stream().map(this::quote).collect(Collectors.joining(", "));
        sql.append(" GROUP BY ").append(groupBy);
    }


    private String formatValue(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "'" + value.toString().replace("'", "''") + "'";
        }
    }

    private String quote(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    @Override
    public String buildFlushFromTempToActualSql(TableDefinitionRequest def, int batchSize) {
        String actualTable = quote(def.getSchemaName()) + "." + quote(def.getTableName());
        String tempTable = quote(def.getSchemaName()) + "." + quote("__tmp_write_" + def.getTableName());

        List<String> columnNames = def.getColumns().stream().map(ColumnDefinition::getName).toList();
        columnNames = new ArrayList<>(columnNames);
        columnNames.add(LAST_UPDATE_DATE);
        columnNames.add(IS_DELETED);

        String insertColumns = columnNames.stream().map(this::quote).collect(Collectors.joining(", "));

        String selectColumns = columnNames.stream()
                .map(col -> "CAST(" + quote(col) + " AS " + resolveColumnType(col, def) + ")")
                .collect(Collectors.joining(", "));

        return """
        INSERT INTO %s (%s)
        SELECT %s FROM %s
        WHERE isDeleted = 'false'
        LIMIT %d;
        """.formatted(actualTable, insertColumns, selectColumns, tempTable, batchSize);
    }


    private String resolveColumnType(String columnName, TableDefinitionRequest def) {
        if (LAST_UPDATE_DATE.equals(columnName)) return "TIMESTAMP";
        if (IS_DELETED.equals(columnName)) return "BOOLEAN";

        return def.getColumns().stream()
                .filter(col -> col.getName().equals(columnName))
                .map(ColumnDefinition::getType)
                .findFirst()
                .orElse("TEXT");
    }

    @Override
    public String buildInsertSql(String schema, String table, Map<String, String> row, boolean tempTable) {
        return buildBulkInsertSql(schema, table, List.of(row), tempTable);
    }

    @Override
    public String buildBulkInsertSql(String schema, String table, List<Map<String, String>> rows, boolean tempTable) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Insert rows cannot be empty");
        }

        String tableName = quote(schema) + "." + quote((tempTable ? "__tmp_write_" : "") + table);
        Set<String> allColumns = rows.get(0).keySet();

        String columnClause = allColumns.stream()
                .map(this::quote)
                .collect(Collectors.joining(", "));

        String valueClause = rows.stream()
                .map(row -> allColumns.stream()
                        .map(col -> "'" + row.getOrDefault(col, "").replace("'", "''") + "'")
                        .collect(Collectors.joining(", ", "(", ")"))
                ).collect(Collectors.joining(",\n"));

        return String.format("INSERT INTO %s (%s) VALUES%n%s;", tableName, columnClause, valueClause);
    }

    @Override
    public String buildTableExistsSql(String schema, String tableName) {
        return "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '" +
                schema + "' AND table_name = '" + tableName + "');";
    }

}
