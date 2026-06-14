INSERT INTO alert_event (id, domain_name, alert_name, alert_level, event_status, impact_scope, triggered_at, recovered_at, owner_name, source_service) VALUES
(1, '项目交付域', '客户画像项目需求交付周期超阈值', 'P1', '处理中', 'CRM-009 版本交付', TIMESTAMP '2026-04-27 09:15:00', NULL, '刘子航', 'project-delivery-service'),
(2, 'DevOps 数据域', 'Jenkins 构建节点采集延迟', 'P2', '已恢复', '流水线采集链路', TIMESTAMP '2026-04-27 08:20:00', TIMESTAMP '2026-04-27 08:52:00', '宋佳音', 'devops-data-service'),
(3, '指标与报告域', '报表历史入库延迟', 'P2', '已恢复', '报表历史留痕', TIMESTAMP '2026-04-26 16:40:00', TIMESTAMP '2026-04-26 17:05:00', '系统管理负责人', 'metrics-report-service'),
(4, '审计与配置域', '配置台账变更待复核', 'P3', '已恢复', '配置控制台', TIMESTAMP '2026-04-26 11:10:00', TIMESTAMP '2026-04-26 11:30:00', '平台审计管理员', 'audit-config-service'),
(5, '项目交付域', '营销活动项目联调进度落后', 'P2', '处理中', '营销活动编排中心', TIMESTAMP '2026-04-26 10:05:00', NULL, '宋佳音', 'project-delivery-service'),
(6, 'DevOps 数据域', 'GitHub 镜像同步失败', 'P3', '已恢复', '代码镜像仓', TIMESTAMP '2026-04-25 18:15:00', TIMESTAMP '2026-04-25 18:45:00', '赵廷轩', 'devops-data-service'),
(7, '指标与报告域', '代码评审周期超阈值预警', 'P2', '处理中', '企业级指标', TIMESTAMP '2026-04-25 14:40:00', NULL, '系统管理负责人', 'metrics-report-service'),
(8, '审计与配置域', '只读查询触发写关键字拦截', 'P3', '已恢复', '配置控制台', TIMESTAMP '2026-04-25 09:20:00', TIMESTAMP '2026-04-25 09:21:00', '平台审计管理员', 'audit-config-service'),
(9, '项目交付域', '对账平台缺陷逃逸率升高', 'P1', '处理中', '融合账单对账平台', TIMESTAMP '2026-04-24 17:05:00', NULL, '顾明轩', 'project-delivery-service'),
(10, 'DevOps 数据域', 'Zabbix 主机数据延迟采集', 'P2', '已恢复', '稳定性平台', TIMESTAMP '2026-04-24 08:55:00', TIMESTAMP '2026-04-24 09:22:00', '何知远', 'devops-data-service');

INSERT INTO service_endpoint (id, service_name, service_code, base_url, run_status, port_no, responsibility) VALUES
(1, '组织与权限域服务-主节点', 'organization-permission-service-primary', 'http://127.0.0.1:8080', 'RUNNING', 8080, '登录鉴权、导航聚合与前端承载'),
(2, '项目交付域服务-主节点', 'project-delivery-service-primary', 'http://127.0.0.1:8081', 'RUNNING', 8081, '项目、工作项与交付活动'),
(3, 'DevOps 数据域服务-主节点', 'devops-data-service-primary', 'http://127.0.0.1:8082', 'RUNNING', 8082, '数据源接入、采集任务与链路状态'),
(4, '指标与报告域服务-主节点', 'metrics-report-service-primary', 'http://127.0.0.1:8083', 'RUNNING', 8083, '指标趋势、报表历史与评分展示'),
(5, '审计与配置域服务-主节点', 'audit-config-service-primary', 'http://127.0.0.1:8084', 'RUNNING', 8084, '告警审计、配置台账与只读控制台'),
(6, '组织与权限域服务-备节点', 'organization-permission-service-standby', 'http://127.0.0.1:18080', 'RUNNING', 18080, '组织域容灾节点与登录页静态资源备援'),
(7, '项目交付域服务-备节点', 'project-delivery-service-standby', 'http://127.0.0.1:18081', 'RUNNING', 18081, '项目域容灾节点与交付看板备援'),
(8, 'DevOps 数据域服务-备节点', 'devops-data-service-standby', 'http://127.0.0.1:18082', 'RUNNING', 18082, '数据域容灾节点与采集链路备援'),
(9, '指标与报告域服务-备节点', 'metrics-report-service-standby', 'http://127.0.0.1:18083', 'RUNNING', 18083, '指标域容灾节点与趋势分析备援'),
(10, '审计与配置域服务-备节点', 'audit-config-service-standby', 'http://127.0.0.1:18084', 'RUNNING', 18084, '审计域容灾节点与配置控制台备援');

INSERT INTO config_item (id, config_group, config_key, config_value, config_state, description, updated_at) VALUES
(1, 'TARGET_DB', 'mysql.jdbc-url', 'jdbc:mysql://localhost:3306/devops_delivery_metrics', 'ACTIVE', '需求 v1.0.1 指定的目标生产数据库地址', TIMESTAMP '2026-04-27 09:05:00'),
(2, 'TARGET_DB', 'mysql.username', 'root', 'ACTIVE', '目标生产数据库账号', TIMESTAMP '2026-04-27 09:05:00'),
(3, 'COLLECTION', 'pipeline.retry-policy', '3 次指数退避', 'ACTIVE', '采集任务失败后的重试策略', TIMESTAMP '2026-04-27 08:30:00'),
(4, 'FRONTEND', 'login.guide.default', 'hidden', 'ACTIVE', '登录提示页默认隐藏，由用户手动展开', TIMESTAMP '2026-04-27 08:55:00'),
(5, 'FRONTEND', 'portal.bottom-actions', 'sticky-left-bottom', 'ACTIVE', '刷新与退出按钮固定在左下角', TIMESTAMP '2026-04-27 08:58:00'),
(6, 'TARGET_TSDB', 'influxdb.url', 'http://127.0.0.1:8086', 'PLANNED', '时序指标数据目标地址，当前演示版未启用', TIMESTAMP '2026-04-27 08:35:00'),
(7, 'DEPLOYMENT', 'docker.compose.profile', 'graduate-demo-v101', 'ACTIVE', '本地演示环境的容器化配置名称', TIMESTAMP '2026-04-27 08:32:00'),
(8, 'REPORT', 'history.display-mode', 'ONLINE_ONLY', 'ACTIVE', '报表历史仅在线展示配置', TIMESTAMP '2026-04-27 08:28:00'),
(9, 'AUDIT', 'operation.retention.days', '180', 'ACTIVE', '操作审计日志保留天数', TIMESTAMP '2026-04-27 08:22:00'),
(10, 'METRIC', 'batch.calculate.cron', '0 0/30 * * * ?', 'ACTIVE', '指标批量计算计划任务表达式', TIMESTAMP '2026-04-27 08:18:00');

INSERT INTO operation_audit_log (id, operator_name, operator_role, operation_type, target_name, operation_result, operation_detail, occurred_at) VALUES
(1, '系统管理负责人', '系统管理', '更新配置', 'mysql.jdbc-url', 'SUCCESS', '同步需求 v1.0.1 中的 MySQL 目标地址到配置台账。', TIMESTAMP '2026-04-27 09:06:00'),
(2, '平台审计管理员', '系统管理', '校验服务', 'service-endpoint', 'SUCCESS', '核验五个业务域微服务端口和启动脚本。', TIMESTAMP '2026-04-27 08:45:00'),
(3, '企业交付管理员', '企业管理', '查看审计', 'audit-events', 'SUCCESS', '通过企业管理视角查看与自身相关的告警恢复记录。', TIMESTAMP '2026-04-27 08:10:00'),
(4, '系统管理负责人', '系统管理', '执行查询', 'config-console', 'SUCCESS', '在只读控制台执行服务注册表巡检查询。', TIMESTAMP '2026-04-27 09:12:00'),
(5, '平台运维管理员', '系统管理', '更新配置', 'docker.compose.profile', 'SUCCESS', '补充演示版部署配置名称用于答辩展示。', TIMESTAMP '2026-04-27 08:58:00'),
(6, '平台配置管理员', '系统管理', '新增配置', 'influxdb.url', 'SUCCESS', '登记需求文档中的时序数据库目标地址。', TIMESTAMP '2026-04-27 08:41:00'),
(7, '企业质量经理', '企业管理', '查看告警', 'defect-escape-alert', 'SUCCESS', '查看缺陷逃逸率升高告警处理情况。', TIMESTAMP '2026-04-27 08:26:00'),
(8, '企业交付主管', '企业管理', '查看服务', 'service-registry', 'SUCCESS', '核对项目交付域与指标域节点状态。', TIMESTAMP '2026-04-27 08:16:00'),
(9, '系统审计管理员', '系统管理', '拦截查询', 'config-console', 'SUCCESS', '阻止包含写关键字的 SQL 在只读控制台执行。', TIMESTAMP '2026-04-25 09:21:00'),
(10, '系统管理负责人', '系统管理', '审计查看', 'operation-log', 'SUCCESS', '查看演示版操作审计列表用于答辩资料整理。', TIMESTAMP '2026-04-24 18:05:00');
