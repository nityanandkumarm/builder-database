package com.builder.database.repository;

import com.builder.database.entity.TableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableMetadataRepository extends JpaRepository<TableMetadata, Long> {
    Optional<TableMetadata> findBySchemaNameAndTableName(String schema, String table);
}
