
[[README|English]] | [[README.CN|简体中文]]
# DevOps-Based IT Project Delivery Performance Measurement Platform for Telecommunications Enterprises
This system is designed for large-scale enterprise management and is developed using Java 8, Spring Boot 2.7, Vue 3, and ECharts as its core technology stack. The front-end interface references the common design style of enterprise-level management systems, employing a layout combining a top bar, left navigation area, secondary menu area, and main content area, which effectively meets the functional display requirements. The back-end is modularly designed based on domain-based partitioning, breaking down system business logic into multiple independently running Spring Boot services to enhance the clarity of the system structure and reserve space for future functional expansion and engineering evolution.

This version has upgraded from "each service having its own independent H2 demo library" to "5 services sharing a single `MySQL 8` master database `db_projectmanage`", and added a separate database initialization module. Database initialization is now manually triggered; the unified database creation script and demo data script are only executed when the initialization script is explicitly executed.

## I. Technology Stack

- Java 1.8

- Spring Boot 2.7

- Vue 3

- ECharts

- JDBC

- MySQL 8.0

- Maven Wrapper

## II. System Architecture

### 1. Five Business Domain Microservices

- `organization-permission-service`, port `8080`

Responsible for login authentication, role navigation, organization and permission data, front-end page hosting, and aggregating interfaces from the other four business domains.

- `project-delivery-service`, port `8081`

Responsible for project delivery dashboards, work items, delivery dynamics, milestones, and version data.

- `devops-data-service`, port `8082`

Responsible for data source access, collection tasks, synchronization status, and data quality inspection.

- `metrics-report-service`, port `8083`

Responsible for core metric overview, metric trends, report center, and metric alerts.

- `audit-config-service`, port `8084`

Responsible for alarm auditing, login auditing, operation auditing, configuration ledger, read-only SQL console, and service registry view.

### 2. Database Initialization Module

- `database-bootstrap`

Responsible for manually initializing the shared MySQL database when needed.

The initialization logic is as follows:

1. Execute `sql/devops_delivery_metrics.sql` in the root directory.

2. Check if `sys_user` already contains data.

3. If empty, execute `sql/devops_delivery_metrics_seed.sql`.

4. Synchronize the demo account password using a hash of `BCrypt` to avoid plaintext comparison.

## III. Database Description

### 1. Unified Master Database

- Database Name: `db_projectmanage`

- Database Type: `MySQL 8.0`

- JDBC URL: `jdbc:mysql://localhost:3306/db_projectmanage?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Hong_Kong&useSSL=false&allowPublicKeyRetrieval=true`

- Username: `root`

- Password: `123123`

### 2. True Source of Database Structure

The following two scripts are the sole true source of the database:

- `../sql/devops_delivery_metrics.sql`

- `../sql/devops_delivery_metrics_seed.sql`

Where:

- `devops_delivery_metrics.sql` is responsible for creating `39` core business tables.

- `devops_delivery_metrics_seed.sql` is responsible for injecting demonstration data from a telecommunications enterprise DevOps scenario.

### 3. Demonstration Data Scale

The current seed data covers all `39` business tables, and each table has at least `10` records.

Core results that have been verified:

- All 39 tables from `org_department` to `api_access_log` exist.

- Each table has `COUNT(*) >= 10` records.

- `metric_definition` currently contains 14 records due to its inclusion of basic metric definitions.

## IV. Demo Account

The demo account has been written to the `sys_user`, `sys_role`, and `sys_user_role` tables. Password verification is performed via `BCrypt` upon login.

### 1. System Management Accounts

- `sys_root / Sys@2026`

- `sys_audit / Audit@2026`

- `sys_ops / Ops@2026`

- `sys_config / Cfg@2026`

### 2. Enterprise Management Accounts

- `enterprise_admin / Corp@2026`

- `enterprise_pm / Pm@2026`

- `enterprise_quality / Qa@2026`

- `enterprise_arch / Arch@2026`

- `enterprise_delivery / Del@2026`

- `enterprise_test / Test@2026`

## V. Differences in Visible Pages by Role

- System Management Accounts:

Visible across five business domains, totaling `10` pages, with access to management pages such as the Access Center, Data Acquisition Link, Report Center, and Configuration Console.

- Enterprise Management Account:

Viewable are 6 enterprise-level pages across five business domains. Key pages to view include organization overview, project deliverables, work items, data source overview, metric overview, and alarm auditing.

## VI. Page Organization

### 1. Organization and Permission Domains

- Organization Overview
- Permission Center

### 2. Project Delivery Domain

- Project Delivery
- Work Item List

### 3. DevOps Data Domain

- Data Source Overview
- Collection Path

### 4. Metrics and Reports Domain

- Metrics Overview
- Reports Center

### 5. Audit and Configuration Domain

- Alarm Audit
- Configuration Console

## VII. Front-end Description

- The login page has been simplified to a pure username and password login UI.

- The login card remains horizontally and vertically centered on both desktop and mobile devices.

- The original gradient background, rounded corners, shadows, and button colors are retained without major color changes.

- The "Refresh / Exit" button at the bottom left is fixed to the lower left corner of the page and does not scroll with the content.

- The configuration console is a custom, beautifully designed read-only database management page within the system; the default is not used. `/h2-console`

## VIII. Backend Layered Structure

For ease of presentation, each microservice is organized in a layered structure:

- `entity`
Entity class, responsible for the data structure corresponding to the database tables.

- `mapper`
Data access layer, uses JDBC to query the shared MySQL master database.

- `service`
Business layer, responsible for page aggregation, statistical caliber conversion, and role logic processing.

- `controller`
Interface layer, responsible for providing REST APIs.

- `config`
Configuration layer, responsible for login interception, password encoder, web configuration, and other infrastructure.

## IX. Directory Description

- `pom.xml`
Parent project, aggregating six Maven modules.

- `database-bootstrap`
Database initialization module.

- `organization-permission-service`
Organization and permission domain microservice, also hosting the frontend pages.

- `project-delivery-service`
Project delivery domain microservice.

- `devops-data-service`
DevOps data domain microservice.

- `metrics-report-service`
Metrics and reporting domain microservice.

- `audit-config-service`
Audit and configuration domain microservice.

- `../sql`
Unified database script directory.

- `scripts/set-java8.bat`
Unified switching to local Java 8 environment.

- `init-database.bat`
Initializes the MySQL database separately.

- `start-all-services.bat`
Starts all five microservices with a single click.

- `start-*.bat`
Starts individual business domain services separately.

## X. Operation Method

### 1. One-Click Database Initialization

Execute the following in the `Code` directory:

``bat init-database.bat

```
This script will:

1. Automatically switch to local Java 8

2. Start `database-bootstrap`

3. Execute the unified schema script

4. Inject demo data in an empty database scenario

5. Synchronize the BCrypt password of the demo account

### 2. One-Click Start of Five Microservices

```bat start-all-services.bat

```
This script will not perform database initialization by default, but will directly start the following five services in parallel:

- `8080` Organization and Permissions Domain

- `8081` Project Delivery Domain

- `8082` DevOps Data Domain

- `8083` Metrics and Reporting Domain

- `8084` Auditing and Configuration Domain

To re-execute database creation and seed data import before startup, please execute the following first:

``bat` set RUN_DB_INIT=1

```
Then run:

``bat` start-all-services.bat

```
### 3. Starting a Single Service

```bat` start-organization-permission-service.bat start-project-delivery-service.bat start-devops-data-service.bat start-metrics-report-service.bat start-audit-config-service.bat

```
Note:

- Single-service scripts do not perform database initialization by default.

- Initialization will only be performed when `RUN_DB_INIT=1` is explicitly set.

- If invoked by `start-all-services.bat`, `SKIP_DB_INIT=1` is used to avoid duplicate initialization.

### 4. Manual Packaging

``bat mvnw.cmd -o -DskipTests package

```
## XI. Access Entry Point

- System Homepage and Management Terminal Entry Point:

[http://localhost:8080](http://localhost:8080)

## XII. IDEA Runtime Configuration

The project has been supplemented with shared runtime configuration, which is recommended for direct use in IDEA:

- `DatabaseBootstrapApplication`

Used for initializing the database separately

- `OrganizationPermissionApplication`

Used to start the main entry service, configured with Java 8; to initialize the database, please run `DatabaseBootstrapApplication` separately

If you need a complete demonstration of five-service collaboration, it is still recommended to use `start-all-services.bat` first.

## XIII. Current Implementation Overview

This project has completed the following key implementations:

- Microservice decomposition across five business domains

- Unified MySQL central database access

- Automatic SQL database creation and injection

- Page queries based on the actual central database tables

- BCrypt password verification

- Differences in view between system management and enterprise management roles

- Refined read-only database console

For more production-oriented capabilities outlined in the requirements document, such as:

- Real-time data acquisition

- Retrying on calculation failures

- Automatic report export to PDF/Excel

- Open API signature verification and rate limiting

- Fine-grained data access control

The current version primarily serves as a graduation project demonstration prototype, focusing on system architecture, business domain decomposition, database design, page presentation, and basic access control.

## XIV. Suggested Order for Presentation during the Defense

1. First, demonstrate the differences between the system management account and the enterprise management account pages.

2. Then, explain how the front-end organizes the pages according to the five business domains.

3. Next, introduce the five Spring Boot microservices and the shared MySQL master database.

4. Then, demonstrate how `database-bootstrap` automatically executes SQL.

5. Finally, show the target database information and read-only SQL queries in the configuration console.