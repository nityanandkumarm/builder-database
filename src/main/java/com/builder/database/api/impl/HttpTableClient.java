package com.builder.database.api.impl;

import com.builder.database.api.TableClient;
import com.builder.database.dto.GenericResultRowDto;
import com.builder.database.dto.IndexDefinitionDto;
import com.builder.database.dto.SelectQueryRequestDto;
import com.builder.database.dto.TableCreateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

public class HttpTableClient implements TableClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpTableClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void createTable(TableCreateRequestDto requestDto) {
        restTemplate.postForEntity(baseUrl + "/tables", requestDto, Void.class);
    }

    @Override
    public void createIndex(String schemaName, String tableName, IndexDefinitionDto indexDto) {
        String url = String.format("%s/tables/%s/%s/indexes", baseUrl, schemaName, tableName);
        restTemplate.postForEntity(url, indexDto, Void.class);
    }

    @Override
    public List<GenericResultRowDto> selectQuery(SelectQueryRequestDto requestDto) {
        ResponseEntity<GenericResultRowDto[]> response = restTemplate.postForEntity(
                baseUrl + "/tables/select",
                requestDto,
                GenericResultRowDto[].class
        );
        return List.of(Objects.requireNonNull(response.getBody()));
    }
}
