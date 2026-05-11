
[English](README.md) | [简体中文](README_CN.md)

# 基于 DevOps 的通讯企业 IT 项目交付效能度量平台

本系统面向大型企业级别管理，采用 Java 8 + Spring Boot 2.7 + Vue 3 + ECharts 作为核心技术栈进行开发。前端界面参考企业级管理系统的通用设计风格，采用顶栏、左侧导航区、二级菜单区与主内容区相结合的布局方式，能够较好地满足功能展示需求。后端基于领域划分思想进行模块化设计，将系统业务拆分为多个可独立运行的 Spring Boot 服务，以增强系统结构的清晰性，并为后续功能扩展和工程化演进预留空间。

本次版本已经从“各服务独立 H2 演示库”升级为“`5` 个服务共享 `MySQL 8` 总库 `db_projectmanage`”，并新增独立的数据库初始化模块。数据库初始化改为手动触发，只有在显式执行初始化脚本时才会执行统一建库脚本与演示数据脚本。

## 一、技术栈

- Java 1.8
- Spring Boot 2.7
- Vue 3
- ECharts
- JDBC
- MySQL 8.0
- Maven Wrapper

## 二、系统结构

### 1. 五个业务域微服务

- `organization-permission-service`，端口 `8080`
  负责登录认证、角色导航、组织与权限数据、前端页面承载、聚合其他四个业务域接口。
- `project-delivery-service`，端口 `8081`
  负责项目交付看板、工作项、交付动态、里程碑与版本数据。
- `devops-data-service`，端口 `8082`
  负责数据源接入、采集任务、同步状态、数据质量巡检。
- `metrics-report-service`，端口 `8083`
  负责核心指标总览、指标趋势、报告中心、指标预警。
- `audit-config-service`，端口 `8084`
  负责告警审计、登录审计、操作审计、配置台账、只读 SQL 控制台、服务注册表视图。

### 2. 数据库初始化模块

- `database-bootstrap`
  负责在需要时手动初始化共享 MySQL 数据库。

初始化逻辑如下：

1. 执行根目录 `sql/devops_delivery_metrics.sql`
2. 检查 `sys_user` 是否已有数据
3. 若为空，则执行 `sql/devops_delivery_metrics_seed.sql`
4. 同步演示账号密码为 `BCrypt` 哈希，避免明文比较

## 三、数据库说明

### 1. 统一总库

- 数据库名称：`db_projectmanage`
- 数据库类型：`MySQL 8.0`
- JDBC URL：`jdbc:mysql://localhost:3306/db_projectmanage?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Hong_Kong&useSSL=false&allowPublicKeyRetrieval=true`
- 用户名：`root`
- 密码：`123123`

### 2. 结构真源

以下两份脚本是数据库唯一真源：

- `../sql/devops_delivery_metrics.sql`
- `../sql/devops_delivery_metrics_seed.sql`

其中：

- `devops_delivery_metrics.sql` 负责创建 `39` 张核心业务表
- `devops_delivery_metrics_seed.sql` 负责注入通信企业 DevOps 场景演示数据

### 3. 演示数据规模

当前种子数据已覆盖全部 `39` 张业务表，且每张表不少于 `10` 条记录。

已实际核验通过的核心结果：

- `org_department` 到 `api_access_log` 共 `39` 张表均已存在
- 每张表 `COUNT(*) >= 10`
- `metric_definition` 因包含基础指标定义，当前为 `14` 条

## 四、演示账号

演示账号已经写入 `sys_user`、`sys_role`、`sys_user_role` 表中，登录时通过 `BCrypt` 校验密码。

### 1. 系统管理账号

- `sys_root / Sys@2026`
- `sys_audit / Audit@2026`
- `sys_ops / Ops@2026`
- `sys_config / Cfg@2026`

### 2. 企业管理账号

- `enterprise_admin / Corp@2026`
- `enterprise_pm / Pm@2026`
- `enterprise_quality / Qa@2026`
- `enterprise_arch / Arch@2026`
- `enterprise_delivery / Del@2026`
- `enterprise_test / Test@2026`

## 五、角色可见页面差异

- 系统管理账号：
  可见五个业务域，共 `10` 个页面，可访问权限中心、采集链路、报告中心、配置控制台等管理页面。
- 企业管理账号：
  可见五个业务域中的 `6` 个企业级页面，重点查看组织概况、项目交付、工作项、数据源总览、指标总览、告警审计。

## 六、页面组织

### 1. 组织与权限域

- 组织概况
- 权限中心

### 2. 项目交付域

- 项目交付
- 工作项列表

### 3. DevOps 数据域

- 数据源总览
- 采集链路

### 4. 指标与报告域

- 指标总览
- 报告中心

### 5. 审计与配置域

- 告警审计
- 配置控制台

## 七、前端说明

- 登录页已精简为纯账号密码登录 UI
- 登录卡片在桌面端与移动端均保持水平垂直居中
- 保留原有渐变背景、圆角、阴影与按钮色彩，不做大改色
- 左侧底部“刷新 / 退出”按钮固定在页面左下角，不随内容滚动
- 配置控制台为系统内自定义的精美只读数据库管理页，不使用默认 `/h2-console`

## 八、后端分层结构

为便于答辩讲解，各微服务均按分层结构组织：

- `entity`
  实体类，负责承接数据库表对应的数据结构。
- `mapper`
  数据访问层，使用 JDBC 查询共享 MySQL 总库。
- `service`
  业务层，负责页面聚合、统计口径转换、角色逻辑处理。
- `controller`
  接口层，负责提供 REST API。
- `config`
  配置层，负责登录拦截、密码编码器、Web 配置等基础设施。

## 九、目录说明

- `pom.xml`
  父工程，聚合六个 Maven 模块。
- `database-bootstrap`
  数据库初始化模块。
- `organization-permission-service`
  组织与权限域微服务，同时承载前端页面。
- `project-delivery-service`
  项目交付域微服务。
- `devops-data-service`
  DevOps 数据域微服务。
- `metrics-report-service`
  指标与报告域微服务。
- `audit-config-service`
  审计与配置域微服务。
- `../sql`
  统一数据库脚本目录。
- `scripts/set-java8.bat`
  统一切换本地 Java 8 环境。
- `init-database.bat`
  单独初始化 MySQL 数据库。
- `start-all-services.bat`
  一键启动全部五个微服务。
- `start-*.bat`
  分别启动单个业务域服务。

## 十、运行方式

### 1. 一键初始化数据库

在 `Code` 目录执行：

```bat
init-database.bat
```

该脚本会：

1. 自动切换到本机 Java 8
2. 启动 `database-bootstrap`
3. 执行统一 schema 脚本
4. 在空库场景下注入演示数据
5. 同步演示账号的 BCrypt 密码

### 2. 一键启动五个微服务

```bat
start-all-services.bat
```

该脚本默认不会执行数据库初始化，而是直接并行启动以下五个服务：

- `8080` 组织与权限域
- `8081` 项目交付域
- `8082` DevOps 数据域
- `8083` 指标与报告域
- `8084` 审计与配置域

如需在启动前重新执行建库与种子数据导入，请先执行：

```bat
set RUN_DB_INIT=1
```

然后再运行：

```bat
start-all-services.bat
```

### 3. 启动单个服务

```bat
start-organization-permission-service.bat
start-project-delivery-service.bat
start-devops-data-service.bat
start-metrics-report-service.bat
start-audit-config-service.bat
```

说明：

- 单服务脚本默认不会执行数据库初始化
- 仅当显式设置 `RUN_DB_INIT=1` 时，单服务脚本才会先执行初始化
- 若由 `start-all-services.bat` 调起，则通过 `SKIP_DB_INIT=1` 避免重复初始化

### 4. 手动打包

```bat
mvnw.cmd -o -DskipTests package
```

## 十一、访问入口

- 系统首页与管理端入口：
  [http://localhost:8080](http://localhost:8080)

## 十二、IDEA 运行配置

项目已经补充共享运行配置，建议在 IDEA 中直接使用：

- `DatabaseBootstrapApplication`
  用于单独初始化数据库
- `OrganizationPermissionApplication`
  用于启动主入口服务，配置了 Java 8；如需初始化数据库，请单独运行 `DatabaseBootstrapApplication`

如果需要完整演示五服务协同，仍建议优先使用 `start-all-services.bat`。

## 十三、当前实现口径

本项目已经完成以下关键落地：

- 五业务域微服务拆分
- 统一 MySQL 总库接入
- 自动执行 SQL 建库与注数
- 基于真实总库表的页面查询
- BCrypt 密码校验
- 系统管理与企业管理两类角色视图差异
- 精美化只读数据库控制台

对于需求文档中更偏生产化的能力，如：

- 实时数据采集
- 计算失败重试
- 报告自动导出 PDF/Excel
- 开放 API 签名验签与限流
- 更细粒度的数据权限隔离

当前版本仍以毕业设计演示原型为主，重点体现系统结构、业务域拆分、数据库设计、页面呈现和基础权限控制。

## 十四、答辩讲解建议顺序

1. 先演示系统管理账号与企业管理账号的页面差异
2. 再说明前端如何按五个业务域组织页面
3. 接着介绍五个 Spring Boot 微服务与共享 MySQL 总库
4. 再演示 `database-bootstrap` 如何自动执行 SQL
5. 最后展示配置控制台中的目标库信息与只读 SQL 查询
