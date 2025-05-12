package com.builder.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "temp_table_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "columns_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String columnsJson;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
