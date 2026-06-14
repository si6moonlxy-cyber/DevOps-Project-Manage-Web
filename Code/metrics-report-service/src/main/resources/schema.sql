DROP TABLE IF EXISTS report_record;
DROP TABLE IF EXISTS metric_snapshot;
DROP TABLE IF EXISTS metric_definition;

CREATE TABLE metric_definition (
    id BIGINT PRIMARY KEY,
    metric_code VARCHAR(50) NOT NULL UNIQUE,
    metric_name VARCHAR(80) NOT NULL,
    unit_name VARCHAR(20) NOT NULL,
    target_value DECIMAL(10, 2) NOT NULL
);

CREATE TABLE metric_snapshot (
    id BIGINT PRIMARY KEY,
    metric_code VARCHAR(50) NOT NULL,
    snapshot_month DATE NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    metric_value DECIMAL(10, 2) NOT NULL,
    trend_rate DECIMAL(10, 2) NOT NULL,
    warning_level VARCHAR(20) NOT NULL
);

CREATE TABLE report_record (
    id BIGINT PRIMARY KEY,
    report_name VARCHAR(120) NOT NULL,
    report_cycle VARCHAR(20) NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    scope_name VARCHAR(80) NOT NULL,
    owner_name VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    quality_score DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);
