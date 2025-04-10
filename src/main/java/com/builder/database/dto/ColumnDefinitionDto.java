package com.builder.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnDefinitionDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_]\\w*$")
    private String name;

    @NotBlank
    private String type;

    private boolean primaryKey;
    private boolean notNull;
    private String defaultValue;
}
