package com.builder.database.config.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericErrorResponse {
    private Instant timestamp;
    private int status;
    private String message;
}