package com.builder.database.config.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private Instant timestamp;
    private int status;
    private String message;
    private List<FieldViolation> errors;
}