package com.builder.database.repository;

import com.builder.database.entity.TempTableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempTableMetadataRepository extends JpaRepository<TempTableMetadata, Long> {
}
