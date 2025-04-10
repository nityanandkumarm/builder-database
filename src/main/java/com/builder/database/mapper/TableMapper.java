package com.builder.database.mapper;

import com.builder.database.dto.*;
import com.builder.database.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TableMapper {

    public TableDefinitionRequest toModel(TableCreateRequestDto dto) {
        return TableDefinitionRequest.builder()
                .schemaName(dto.getSchemaName())
                .tableName(dto.getTableName())
                .temporaryWriteTable(dto.isTemporaryWriteTable())
                .columns(toModelColumns(dto.getColumns()))
                .indexes(toModelIndexes(dto.getIndexes()))
                .build();
    }

    public List<ColumnDefinition> toModelColumns(List<ColumnDefinitionDto> columnDtos) {
        if (columnDtos == null) return null;
        return columnDtos.stream().map(this::toModel).collect(Collectors.toList());
    }

    public ColumnDefinition toModel(ColumnDefinitionDto dto) {
        return ColumnDefinition.builder()
                .name(dto.getName())
                .type(dto.getType())
                .primaryKey(dto.isPrimaryKey())
                .notNull(dto.isNotNull())
                .defaultValue(dto.getDefaultValue())
                .build();
    }

    public List<IndexDefinition> toModelIndexes(List<IndexDefinitionDto> indexDtos) {
        if (indexDtos == null) return null;
        return indexDtos.stream().map(this::toModel).collect(Collectors.toList());
    }

    public IndexDefinition toModel(IndexDefinitionDto dto) {
        return IndexDefinition.builder()
                .columnNames(dto.getColumnNames())
                .indexType(dto.getIndexType())
                .unique(dto.isUnique())
                .build();
    }

    public SelectQueryRequest toModel(SelectQueryRequestDto dto) {
        return SelectQueryRequest.builder()
                .schemaName(dto.getSchemaName())
                .tableName(dto.getTableName())
                .columns(dto.getColumns())
                .filters(dto.getFilters())
                .aggregations(toModelAggregations(dto.getAggregations()))
                .build();
    }

    public List<AggregationRequest> toModelAggregations(List<AggregationRequestDto> aggregationDtos) {
        if (aggregationDtos == null) return null;
        return aggregationDtos.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public AggregationRequest toModel(AggregationRequestDto dto) {
        return AggregationRequest.builder()
                .column(dto.getColumn())
                .function(dto.getFunction())
                .alias(dto.getAlias())
                .build();
    }


    public TableCreateRequestDto toDto(TableDefinitionRequest model) {
        return TableCreateRequestDto.builder()
                .schemaName(model.getSchemaName())
                .tableName(model.getTableName())
                .temporaryWriteTable(model.isTemporaryWriteTable())
                .columns(toDtoColumns(model.getColumns()))
                .indexes(toDtoIndexes(model.getIndexes()))
                .build();
    }

    public List<ColumnDefinitionDto> toDtoColumns(List<ColumnDefinition> columns) {
        if (columns == null) return null;
        return columns.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ColumnDefinitionDto toDto(ColumnDefinition model) {
        return ColumnDefinitionDto.builder()
                .name(model.getName())
                .type(model.getType())
                .primaryKey(model.isPrimaryKey())
                .notNull(model.isNotNull())
                .defaultValue(model.getDefaultValue())
                .build();
    }

    public List<IndexDefinitionDto> toDtoIndexes(List<IndexDefinition> indexes) {
        if (indexes == null) return null;
        return indexes.stream().map(this::toDto).collect(Collectors.toList());
    }

    public IndexDefinitionDto toDto(IndexDefinition model) {
        return IndexDefinitionDto.builder()
                .columnNames(model.getColumnNames())
                .indexType(model.getIndexType())
                .unique(model.isUnique())
                .build();
    }

    public List<AggregationRequestDto> toDtoAggregations(List<AggregationRequest> models) {
        if (models == null) return null;
        return models.stream().map(this::toDto).collect(Collectors.toList());
    }

    public AggregationRequestDto toDto(AggregationRequest model) {
        return AggregationRequestDto.builder()
                .column(model.getColumn())
                .function(model.getFunction())
                .alias(model.getAlias())
                .build();
    }

        public GenericResultRowDto fromMap(Map<String, Object> map) {
        return GenericResultRowDto.builder()
                .fields(map)
                .build();
    }

}
