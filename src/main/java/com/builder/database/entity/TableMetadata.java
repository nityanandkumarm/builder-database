package com.builder.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schema_name", nullable = false)
    private String schemaName;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "has_temp_table")
    private boolean hasTempTable;

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
