package com.builder.database.repository;

import com.builder.database.entity.TempTableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TempTableMetadataRepository extends JpaRepository<TempTableMetadata, Long> {
    Optional<TempTableMetadata> findBySchemaNameAndOriginalTableName(String schema, String table);
}
