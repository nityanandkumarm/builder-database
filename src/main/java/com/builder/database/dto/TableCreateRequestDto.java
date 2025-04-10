package com.builder.database.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableCreateRequestDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_]\\w*$")
    private String schemaName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_]\\w*$")
    private String tableName;

    @Size(min = 1)
    @NotNull
    private List<@Valid ColumnDefinitionDto> columns;

    private List<IndexDefinitionDto> indexes; // Add this field to support indexing

    private boolean temporaryWriteTable;


}
