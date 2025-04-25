package com.builder.database.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "temp_table_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempTableMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schema_name", nullable = false)
    private String schemaName;

    @Column(name = "temp_table_name", nullable = false)
    private String tempTableName;

    @Column(name = "original_table_name", nullable = false)
    private String originalTableName;

    @Lob
    @Column(name = "columns_json", columnDefinition = "TEXT")
    private String columnsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
