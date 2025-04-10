package com.builder.database.api.config;

import com.builder.database.service.TableService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableClientConfig {
    public enum Mode { HTTP, LOCAL }

    private Mode mode;
    private String baseUrl; // used for HTTP
    private TableService tableService; // used for LOCAL
}
