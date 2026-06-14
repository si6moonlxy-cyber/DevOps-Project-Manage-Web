DROP TABLE IF EXISTS admin_account;
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
    member_count INT NOT NULL,
    FOREIGN KEY (dept_id) REFERENCES department(id)
);

CREATE TABLE admin_account (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    login_password VARCHAR(50) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(30) NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    last_login_at TIMESTAMP,
    description VARCHAR(255) NOT NULL
);
