#!/bin/bash

DB_NAME="searcher"
DB_USER="searcher"
DB_PASSWORD="searcher"

psql -U postgres <<EOF
CREATE DATABASE $DB_NAME;

CREATE ROLE $DB_USER LOGIN PASSWORD '$DB_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO $DB_USER;
DO \$\$
DECLARE
    seq record;
BEGIN
    FOR seq IN SELECT sequence_schema, sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public'
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON SEQUENCE %I.%I TO $DB_USER;', seq.sequence_schema, seq.sequence_name);
    END LOOP;
END
\$\$;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO $DB_USER;

\c $DB_NAME

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE users OWNER TO $DB_USER;

CREATE TABLE user_roles (
    id SERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);

ALTER TABLE user_roles OWNER TO $DB_USER;
EOF

# Запуск Java-приложения
java -jar ./searcher-1.0.0.jar