package com.builder.database.builder;

import org.springframework.stereotype.Component;

@Component
public class SqlBuilderFactory {

    // In future, can be loaded from application.yml or env
    private static final String DEFAULT_DB = "postgres";

    public SqlBuilder getBuilder() {
        return switch (DEFAULT_DB.toLowerCase()) {
            case "postgres" -> new PostgresSqlBuilder();
            // case "mysql" -> new MySqlSqlBuilder();
            // case "oracle" -> new OracleSqlBuilder();
            default -> throw new UnsupportedOperationException("Unsupported DB type");
        };
    }
}
