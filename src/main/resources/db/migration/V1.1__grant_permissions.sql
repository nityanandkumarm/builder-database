-- Grant permissions on tables and sequences
GRANT ALL PRIVILEGES ON table_metadata TO root;
GRANT ALL PRIVILEGES ON temp_table_metadata TO root;
GRANT USAGE, SELECT ON SEQUENCE table_metadata_id_seq TO root;
GRANT USAGE, SELECT ON SEQUENCE temp_table_metadata_id_seq TO root;

-- Grant schema usage and set ownership
GRANT USAGE ON SCHEMA public TO root;
ALTER SEQUENCE table_metadata_id_seq OWNER TO root;
ALTER SEQUENCE temp_table_metadata_id_seq OWNER TO root;
ALTER TABLE table_metadata OWNER TO root;
ALTER TABLE temp_table_metadata OWNER TO root;

-- Set default privileges
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
GRANT ALL PRIVILEGES ON TABLES TO root;

ALTER DEFAULT PRIVILEGES IN SCHEMA public 
GRANT USAGE, SELECT ON SEQUENCES TO root;
