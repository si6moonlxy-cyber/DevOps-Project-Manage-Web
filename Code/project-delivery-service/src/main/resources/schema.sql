DROP TABLE IF EXISTS delivery_activity;
DROP TABLE IF EXISTS work_item;
DROP TABLE IF EXISTS project;

CREATE TABLE project (
    id BIGINT PRIMARY KEY,
    project_name VARCHAR(120) NOT NULL,
    manager_name VARCHAR(50) NOT NULL,
    business_domain VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    delivery_stage VARCHAR(30) NOT NULL,
    progress INT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    current_version VARCHAR(30) NOT NULL,
    plan_go_live TIMESTAMP NOT NULL
);

CREATE TABLE work_item (
    id BIGINT PRIMARY KEY,
    project_name VARCHAR(120) NOT NULL,
    item_title VARCHAR(120) NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    item_status VARCHAR(30) NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    owner_name VARCHAR(50) NOT NULL,
    sprint_name VARCHAR(40) NOT NULL,
    progress INT NOT NULL,
    due_date DATE NOT NULL
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
