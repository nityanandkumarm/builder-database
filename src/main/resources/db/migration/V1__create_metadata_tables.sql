-- Create sequences first
CREATE SEQUENCE IF NOT EXISTS table_metadata_id_seq;
CREATE SEQUENCE IF NOT EXISTS temp_table_metadata_id_seq;

-- Create table_metadata table
CREATE TABLE IF NOT EXISTS table_metadata (
    id BIGINT DEFAULT nextval('table_metadata_id_seq') PRIMARY KEY,
    schema_name VARCHAR(255) NOT NULL,
    table_name VARCHAR(255) NOT NULL,
    has_temp_table BOOLEAN DEFAULT FALSE,
    columns_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(schema_name, table_name)
);

-- Create temp_table_metadata table
CREATE TABLE IF NOT EXISTS temp_table_metadata (
    id BIGINT DEFAULT nextval('temp_table_metadata_id_seq') PRIMARY KEY,
    schema_name VARCHAR(255) NOT NULL,
    temp_table_name VARCHAR(255) NOT NULL,
    original_table_name VARCHAR(255) NOT NULL,
    columns_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(schema_name, temp_table_name)
);
