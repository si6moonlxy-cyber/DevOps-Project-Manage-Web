DROP TABLE IF EXISTS operation_audit_log;
DROP TABLE IF EXISTS config_item;
DROP TABLE IF EXISTS service_endpoint;
DROP TABLE IF EXISTS alert_event;

CREATE TABLE alert_event (
    id BIGINT PRIMARY KEY,
    domain_name VARCHAR(60) NOT NULL,
    alert_name VARCHAR(120) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    event_status VARCHAR(20) NOT NULL,
    impact_scope VARCHAR(80) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    recovered_at TIMESTAMP,
    owner_name VARCHAR(50) NOT NULL,
    source_service VARCHAR(60) NOT NULL
);

CREATE TABLE service_endpoint (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(80) NOT NULL,
    service_code VARCHAR(80) NOT NULL,
    base_url VARCHAR(120) NOT NULL,
    run_status VARCHAR(20) NOT NULL,
    port_no INT NOT NULL,
    responsibility VARCHAR(120) NOT NULL
);

CREATE TABLE config_item (
    id BIGINT PRIMARY KEY,
    config_group VARCHAR(50) NOT NULL,
    config_key VARCHAR(80) NOT NULL,
    config_value VARCHAR(160) NOT NULL,
    config_state VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE operation_audit_log (
    id BIGINT PRIMARY KEY,
    operator_name VARCHAR(50) NOT NULL,
    operator_role VARCHAR(30) NOT NULL,
    operation_type VARCHAR(40) NOT NULL,
    target_name VARCHAR(80) NOT NULL,
    operation_result VARCHAR(20) NOT NULL,
    operation_detail VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP NOT NULL
);
