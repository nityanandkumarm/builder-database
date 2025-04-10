package com.builder.database.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnDefinition {
    private String name;
    private String type;
    private boolean primaryKey;
    private boolean notNull;
    private String defaultValue;
}
