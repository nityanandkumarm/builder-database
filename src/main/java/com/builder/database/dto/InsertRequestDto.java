package com.builder.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertRequestDto {

    @NotBlank
    private String schemaName;

    @NotBlank
    private String tableName;

    @NotEmpty
    private List<@NotEmpty Map<String, String>> rows;
}
