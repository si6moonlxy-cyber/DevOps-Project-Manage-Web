DROP TABLE IF EXISTS delivery_activity;
DROP TABLE IF EXISTS work_item;
DROP TABLE IF EXISTS alert_event;
DROP TABLE IF EXISTS report_record;
DROP TABLE IF EXISTS metric_snapshot;
DROP TABLE IF EXISTS metric_definition;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS admin_account;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS team;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id BIGINT PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL
);

CREATE TABLE team (
    id BIGINT PRIMARY KEY,
    dept_id BIGINT NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    focus_area VARCHAR(100) NOT NULL,
    FOREIGN KEY (dept_id) REFERENCES department(id)
);

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE admin_account (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    login_password VARCHAR(50) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(30) NOT NULL,
    team_id BIGINT,
    account_status VARCHAR(20) NOT NULL,
    last_login_at TIMESTAMP,
    description VARCHAR(255) NOT NULL,
    FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE project (
    id BIGINT PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    project_name VARCHAR(120) NOT NULL,
    team_id BIGINT NOT NULL,
    manager_name VARCHAR(50) NOT NULL,
    business_domain VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    delivery_stage VARCHAR(30) NOT NULL,
    progress INT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    current_version VARCHAR(30) NOT NULL,
    plan_go_live TIMESTAMP NOT NULL,
    FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE work_item (
    id BIGINT PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL,
    project_name VARCHAR(120) NOT NULL,
    item_title VARCHAR(120) NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    item_status VARCHAR(30) NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    owner_name VARCHAR(50) NOT NULL,
    sprint_name VARCHAR(40) NOT NULL,
    progress INT NOT NULL,
    due_date DATE NOT NULL,
    FOREIGN KEY (project_code) REFERENCES project(project_code)
);

CREATE TABLE data_source (
    id BIGINT PRIMARY KEY,
    source_name VARCHAR(80) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    sync_owner VARCHAR(50) NOT NULL,
    last_sync_time TIMESTAMP NOT NULL,
    api_health INT NOT NULL,
    today_increment INT NOT NULL,
    remark VARCHAR(255) NOT NULL
);

CREATE TABLE metric_definition (
    id BIGINT PRIMARY KEY,
    metric_code VARCHAR(50) NOT NULL UNIQUE,
    metric_name VARCHAR(80) NOT NULL,
    category_name VARCHAR(40) NOT NULL,
    unit_name VARCHAR(20) NOT NULL,
    target_value DECIMAL(10, 2) NOT NULL
);

CREATE TABLE metric_snapshot (
    id BIGINT PRIMARY KEY,
    metric_code VARCHAR(50) NOT NULL,
    snapshot_month DATE NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    scope_name VARCHAR(80) NOT NULL,
    metric_value DECIMAL(10, 2) NOT NULL,
    trend_rate DECIMAL(10, 2) NOT NULL,
    warning_level VARCHAR(20) NOT NULL,
    FOREIGN KEY (metric_code) REFERENCES metric_definition(metric_code)
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

CREATE TABLE alert_event (
    id BIGINT PRIMARY KEY,
    project_name VARCHAR(120) NOT NULL,
    alert_name VARCHAR(120) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    impact_scope VARCHAR(80) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    recovered_at TIMESTAMP,
    owner_name VARCHAR(50) NOT NULL
);

CREATE TABLE delivery_activity (
    id BIGINT PRIMARY KEY,
    activity_title VARCHAR(120) NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    project_name VARCHAR(120) NOT NULL,
    owner_name VARCHAR(50) NOT NULL,
    activity_status VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    detail_text VARCHAR(255) NOT NULL
);
