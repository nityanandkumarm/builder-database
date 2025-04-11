package com.builder.database.controller;

import com.builder.database.dto.*;
import com.builder.database.mapper.TableMapper;
import com.builder.database.service.TableMetadataService;
import com.builder.database.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;
    private final TableMetadataService tableMetadataService;
    private final TableMapper tableMapper;

    @PostMapping("/create")
    public ResponseEntity<String> createTable(@RequestBody @Valid TableCreateRequestDto dto) {
        tableService.createTable(dto);
        return ResponseEntity.ok("Table created successfully.");
    }

    @PostMapping("/select")
    public ResponseEntity<List<GenericResultRowDto>> executeSelect(@RequestBody @Valid SelectQueryRequestDto request) {
        List<GenericResultRowDto> results = tableService.executeSelectQuery(request);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{schema}/{table}/indexes")
    public ResponseEntity<String> createIndex(
            @PathVariable String schema,
            @PathVariable String table,
            @RequestBody @Valid IndexDefinitionDto indexDto
    ) {
        tableService.createIndex(schema, table, indexDto);
        return ResponseEntity.ok("Index created successfully.");
    }

    @PostMapping("/{schema}/{table}/flush")
    public ResponseEntity<String> flushTempTable(
            @PathVariable String schema,
            @PathVariable String table) {
        tableService.flushTempToActual(schema, table);
        return ResponseEntity.ok("Flush completed.");
    }

    @GetMapping("/{schema}/{table}")
    public ResponseEntity<TableCreateRequestDto> getTableMetadata(
            @PathVariable String schema,
            @PathVariable String table,
            @RequestParam(name = "includeIndexes", defaultValue = "true") boolean includeIndexes
    ) {
        var model = tableMetadataService.getTableDefinition(schema, table, includeIndexes);
        var dto = tableMapper.toDto(model);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/insert")
    public ResponseEntity<String> insertRows(@RequestBody @Valid InsertRequestDto request) {
        tableService.insertRows(request.getSchemaName(), request.getTableName(), request.getRows());
        return ResponseEntity.ok("Rows inserted successfully.");
    }

}
