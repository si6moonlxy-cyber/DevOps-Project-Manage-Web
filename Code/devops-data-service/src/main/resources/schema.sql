DROP TABLE IF EXISTS collection_task;
DROP TABLE IF EXISTS data_source;

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

CREATE TABLE collection_task (
    id BIGINT PRIMARY KEY,
    task_name VARCHAR(120) NOT NULL,
    source_name VARCHAR(80) NOT NULL,
    schedule_expr VARCHAR(80) NOT NULL,
    task_status VARCHAR(20) NOT NULL,
    last_run_time TIMESTAMP NOT NULL,
    next_run_time TIMESTAMP NOT NULL
);
