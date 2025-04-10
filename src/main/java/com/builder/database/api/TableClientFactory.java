package com.builder.database.api;

import com.builder.database.api.impl.HttpTableClient;
import com.builder.database.api.impl.LocalTableClient;
import com.builder.database.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class TableClientFactory {

    private final TableService tableService;

    @Autowired
    public TableClientFactory(final TableService tableService){
        this.tableService = tableService;
    }

    public TableClient createLocalClient() {
        return new LocalTableClient(tableService);
    }

    public TableClient createHttpClient(String baseUrl) {
        RestTemplate restTemplate = createSafeRestTemplate();
        return new HttpTableClient(restTemplate, baseUrl);
    }

    private RestTemplate createSafeRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add error handler for REST call failures
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

        });

        return restTemplate;
    }
}
