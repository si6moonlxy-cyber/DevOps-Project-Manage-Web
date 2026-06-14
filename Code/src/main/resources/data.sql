INSERT INTO department (id, dept_name) VALUES
(1, '交付研发中心'),
(2, '云网运营中心');

INSERT INTO team (id, dept_id, team_name, focus_area) VALUES
(1, 1, 'BSS交付团队', '计费与结算'),
(2, 1, '5G平台团队', '流程调度'),
(3, 2, '云网运维团队', '监控与稳定性');

INSERT INTO sys_user (id, team_id, real_name, role_name) VALUES
(1, 1, '陈文博', '项目经理'),
(2, 1, '刘子航', '技术负责人'),
(3, 2, '周一凡', '项目经理'),
(4, 2, '宋佳音', '测试负责人'),
(5, 3, '王泽远', '运维负责人');

INSERT INTO admin_account (id, username, login_password, display_name, role_code, role_name, team_id, account_status, last_login_at, description) VALUES
(1, 'sys_root', 'Sys@2026', '系统管理负责人', 'SYSTEM_ADMIN', '系统管理', NULL, 'ACTIVE', TIMESTAMP '2026-04-17 18:20:00', '可查看全部页面、账号中心与数据库控制台'),
(2, 'sys_audit', 'Audit@2026', '平台审计管理员', 'SYSTEM_ADMIN', '系统管理', NULL, 'ACTIVE', TIMESTAMP '2026-04-16 09:10:00', '用于答辩演示系统级管理账号'),
(3, 'enterprise_admin', 'Corp@2026', '企业交付管理员', 'ENTERPRISE_ADMIN', '企业管理', 1, 'ACTIVE', TIMESTAMP '2026-04-17 17:35:00', '可查看企业交付、仪表盘与工作量页面'),
(4, 'enterprise_pm', 'Pm@2026', '企业项目负责人', 'ENTERPRISE_ADMIN', '企业管理', 2, 'ACTIVE', TIMESTAMP '2026-04-15 14:25:00', '用于演示企业管理员登录入口');

INSERT INTO project (id, project_code, project_name, team_id, manager_name, business_domain, status, delivery_stage, progress, risk_level, current_version, plan_go_live) VALUES
(1, 'BSS-001', 'BSS计费中台重构', 1, '陈文博', '核心计费', '开发中', '联调阶段', 82, 'MEDIUM', 'v2.4.0', TIMESTAMP '2026-05-18 00:00:00'),
(2, 'OPS-014', '5G工单调度平台', 2, '周一凡', '流程自动化', '测试中', '验收阶段', 91, 'LOW', 'v1.9.2', TIMESTAMP '2026-04-28 00:00:00'),
(3, 'NOC-021', '云网运维门户', 3, '王泽远', '可观测平台', '稳定运行', '运营阶段', 97, 'LOW', 'v3.2.1', TIMESTAMP '2026-04-20 00:00:00'),
(4, 'CRM-009', '客户画像分析平台', 1, '刘子航', '数据分析', '开发中', '开发阶段', 68, 'HIGH', 'v1.5.3', TIMESTAMP '2026-06-12 00:00:00');

INSERT INTO work_item (id, project_code, project_name, item_title, item_type, item_status, priority_level, owner_name, sprint_name, progress, due_date) VALUES
(1, 'BSS-001', 'BSS计费中台重构', '统一计费规则引擎联调', '需求', '进行中', '高', '陈文博', 'Sprint 23', 76, DATE '2026-04-21'),
(2, 'BSS-001', 'BSS计费中台重构', '账务校验接口压测', '测试', '待处理', '中', '刘子航', 'Sprint 23', 24, DATE '2026-04-23'),
(3, 'OPS-014', '5G工单调度平台', '工单回退场景验收', '任务', '已完成', '中', '周一凡', 'Sprint 31', 100, DATE '2026-04-16'),
(4, 'NOC-021', '云网运维门户', '告警归档规则上线', '任务', '进行中', '低', '王泽远', 'Sprint 19', 82, DATE '2026-04-19'),
(5, 'CRM-009', '客户画像分析平台', '画像标签链路修复', '缺陷', '风险中', '高', '刘子航', 'Sprint 12', 52, DATE '2026-04-20'),
(6, 'CRM-009', '客户画像分析平台', '数据补采作业优化', '任务', '待处理', '高', '宋佳音', 'Sprint 12', 18, DATE '2026-04-24'),
(7, 'OPS-014', '5G工单调度平台', '自动派单策略复盘', '评审', '进行中', '中', '宋佳音', 'Sprint 31', 63, DATE '2026-04-22');

INSERT INTO data_source (id, source_name, source_type, sync_status, sync_owner, last_sync_time, api_health, today_increment, remark) VALUES
(1, '需求中心 Jira', 'JIRA', 'ONLINE', '陈文博', TIMESTAMP '2026-04-17 20:35:00', 98, 126, '需求、缺陷与任务同步正常'),
(2, '代码仓 GitLab', 'GITLAB', 'ONLINE', '刘子航', TIMESTAMP '2026-04-17 20:32:00', 96, 84, '提交记录与 MR 数据已对齐'),
(3, '流水线 Jenkins', 'JENKINS', 'WARNING', '宋佳音', TIMESTAMP '2026-04-17 19:58:00', 82, 37, '个别作业节点响应偏慢'),
(4, '监控 Prometheus', 'PROMETHEUS', 'ONLINE', '王泽远', TIMESTAMP '2026-04-17 20:37:00', 94, 58, '告警与恢复事件采集正常');

INSERT INTO metric_definition (id, metric_code, metric_name, category_name, unit_name, target_value) VALUES
(1, 'REQ_DELIVERY_CYCLE', '需求交付周期', '过程指标', '天', 14.00),
(2, 'DEPLOY_FREQUENCY', '部署频率', '效率指标', '次/周', 8.00),
(3, 'CHANGE_FAILURE_RATE', '变更失败率', '质量指标', '%', 12.00),
(4, 'MTTR', '平均恢复时间', '稳定性指标', '分钟', 60.00);

INSERT INTO metric_snapshot (id, metric_code, snapshot_month, scope_type, scope_name, metric_value, trend_rate, warning_level) VALUES
(1, 'REQ_DELIVERY_CYCLE', DATE '2025-11-01', 'ENTERPRISE', '企业级', 18.50, -4.00, 'WARNING'),
(2, 'REQ_DELIVERY_CYCLE', DATE '2025-12-01', 'ENTERPRISE', '企业级', 17.20, -7.00, 'WARNING'),
(3, 'REQ_DELIVERY_CYCLE', DATE '2026-01-01', 'ENTERPRISE', '企业级', 16.40, -4.70, 'WARNING'),
(4, 'REQ_DELIVERY_CYCLE', DATE '2026-02-01', 'ENTERPRISE', '企业级', 15.10, -7.90, 'WARNING'),
(5, 'REQ_DELIVERY_CYCLE', DATE '2026-03-01', 'ENTERPRISE', '企业级', 14.60, -3.30, 'WARNING'),
(6, 'REQ_DELIVERY_CYCLE', DATE '2026-04-01', 'ENTERPRISE', '企业级', 13.80, -5.50, 'NORMAL'),

(7, 'DEPLOY_FREQUENCY', DATE '2025-11-01', 'ENTERPRISE', '企业级', 4.00, 0.00, 'WARNING'),
(8, 'DEPLOY_FREQUENCY', DATE '2025-12-01', 'ENTERPRISE', '企业级', 5.00, 25.00, 'WARNING'),
(9, 'DEPLOY_FREQUENCY', DATE '2026-01-01', 'ENTERPRISE', '企业级', 5.80, 16.00, 'WARNING'),
(10, 'DEPLOY_FREQUENCY', DATE '2026-02-01', 'ENTERPRISE', '企业级', 6.30, 8.60, 'WARNING'),
(11, 'DEPLOY_FREQUENCY', DATE '2026-03-01', 'ENTERPRISE', '企业级', 6.90, 9.50, 'NORMAL'),
(12, 'DEPLOY_FREQUENCY', DATE '2026-04-01', 'ENTERPRISE', '企业级', 7.40, 7.20, 'NORMAL'),

(13, 'CHANGE_FAILURE_RATE', DATE '2025-11-01', 'ENTERPRISE', '企业级', 19.00, 0.00, 'CRITICAL'),
(14, 'CHANGE_FAILURE_RATE', DATE '2025-12-01', 'ENTERPRISE', '企业级', 17.50, -7.90, 'WARNING'),
(15, 'CHANGE_FAILURE_RATE', DATE '2026-01-01', 'ENTERPRISE', '企业级', 15.60, -10.90, 'WARNING'),
(16, 'CHANGE_FAILURE_RATE', DATE '2026-02-01', 'ENTERPRISE', '企业级', 14.80, -5.10, 'WARNING'),
(17, 'CHANGE_FAILURE_RATE', DATE '2026-03-01', 'ENTERPRISE', '企业级', 13.20, -10.80, 'WARNING'),
(18, 'CHANGE_FAILURE_RATE', DATE '2026-04-01', 'ENTERPRISE', '企业级', 11.60, -12.10, 'NORMAL'),

(19, 'MTTR', DATE '2025-11-01', 'ENTERPRISE', '企业级', 120.00, 0.00, 'CRITICAL'),
(20, 'MTTR', DATE '2025-12-01', 'ENTERPRISE', '企业级', 106.00, -11.70, 'WARNING'),
(21, 'MTTR', DATE '2026-01-01', 'ENTERPRISE', '企业级', 95.00, -10.40, 'WARNING'),
(22, 'MTTR', DATE '2026-02-01', 'ENTERPRISE', '企业级', 82.00, -13.70, 'WARNING'),
(23, 'MTTR', DATE '2026-03-01', 'ENTERPRISE', '企业级', 72.00, -12.20, 'WARNING'),
(24, 'MTTR', DATE '2026-04-01', 'ENTERPRISE', '企业级', 56.00, -22.20, 'NORMAL'),

(101, 'REQ_DELIVERY_CYCLE', DATE '2026-04-01', 'PROJECT', 'BSS-001', 14.50, -3.00, 'WARNING'),
(102, 'DEPLOY_FREQUENCY', DATE '2026-04-01', 'PROJECT', 'BSS-001', 6.20, 8.00, 'NORMAL'),
(103, 'CHANGE_FAILURE_RATE', DATE '2026-04-01', 'PROJECT', 'BSS-001', 11.80, -6.00, 'NORMAL'),
(104, 'MTTR', DATE '2026-04-01', 'PROJECT', 'BSS-001', 64.00, -9.00, 'WARNING'),

(105, 'REQ_DELIVERY_CYCLE', DATE '2026-04-01', 'PROJECT', 'OPS-014', 11.80, -6.00, 'NORMAL'),
(106, 'DEPLOY_FREQUENCY', DATE '2026-04-01', 'PROJECT', 'OPS-014', 8.40, 11.00, 'NORMAL'),
(107, 'CHANGE_FAILURE_RATE', DATE '2026-04-01', 'PROJECT', 'OPS-014', 9.10, -8.00, 'NORMAL'),
(108, 'MTTR', DATE '2026-04-01', 'PROJECT', 'OPS-014', 41.00, -18.00, 'NORMAL'),

(109, 'REQ_DELIVERY_CYCLE', DATE '2026-04-01', 'PROJECT', 'NOC-021', 10.30, -4.00, 'NORMAL'),
(110, 'DEPLOY_FREQUENCY', DATE '2026-04-01', 'PROJECT', 'NOC-021', 9.10, 6.00, 'NORMAL'),
(111, 'CHANGE_FAILURE_RATE', DATE '2026-04-01', 'PROJECT', 'NOC-021', 6.80, -12.00, 'NORMAL'),
(112, 'MTTR', DATE '2026-04-01', 'PROJECT', 'NOC-021', 34.00, -15.00, 'NORMAL'),

(113, 'REQ_DELIVERY_CYCLE', DATE '2026-04-01', 'PROJECT', 'CRM-009', 18.20, 4.00, 'CRITICAL'),
(114, 'DEPLOY_FREQUENCY', DATE '2026-04-01', 'PROJECT', 'CRM-009', 4.60, -5.00, 'WARNING'),
(115, 'CHANGE_FAILURE_RATE', DATE '2026-04-01', 'PROJECT', 'CRM-009', 16.40, 9.00, 'CRITICAL'),
(116, 'MTTR', DATE '2026-04-01', 'PROJECT', 'CRM-009', 88.00, 12.00, 'CRITICAL');

INSERT INTO report_record (id, report_name, report_cycle, report_type, scope_name, owner_name, generated_at, quality_score, status) VALUES
(1, '2026年4月企业级研发效能周报', '周报', '管理报表', '企业级', '系统管理负责人', TIMESTAMP '2026-04-17 18:30:00', 89.50, '已生成'),
(2, 'BSS计费中台月度交付复盘', '月报', '项目复盘', 'BSS计费中台重构', '陈文博', TIMESTAMP '2026-04-15 10:15:00', 86.00, '已生成'),
(3, '5G工单调度平台版本验收报告', '专项报告', '版本验收', '5G工单调度平台', '周一凡', TIMESTAMP '2026-04-13 16:20:00', 92.00, '已留档'),
(4, '云网运维门户稳定性趋势分析', '月报', '质量分析', '云网运维门户', '王泽远', TIMESTAMP '2026-04-11 14:05:00', 94.50, '已生成');

INSERT INTO alert_event (id, project_name, alert_name, alert_level, status, impact_scope, triggered_at, recovered_at, owner_name) VALUES
(1, '客户画像分析平台', '生产构建失败率升高', 'P1', '处理中', '生产流水线', TIMESTAMP '2026-04-17 18:42:00', NULL, '刘子航'),
(2, 'BSS计费中台重构', '需求交付周期超阈值', 'P2', '已恢复', '需求交付链路', TIMESTAMP '2026-04-16 14:20:00', TIMESTAMP '2026-04-16 17:30:00', '陈文博'),
(3, '云网运维门户', '告警恢复超时', 'P2', '已恢复', '监控治理', TIMESTAMP '2026-04-15 11:05:00', TIMESTAMP '2026-04-15 12:00:00', '王泽远'),
(4, '5G工单调度平台', '测试环境部署波动', 'P3', '已恢复', '测试环境', TIMESTAMP '2026-04-14 09:15:00', TIMESTAMP '2026-04-14 10:00:00', '周一凡');

INSERT INTO delivery_activity (id, activity_title, activity_type, project_name, owner_name, activity_status, occurred_at, detail_text) VALUES
(1, 'BSS 联调冒烟通过', '发布动态', 'BSS计费中台重构', '陈文博', '已完成', TIMESTAMP '2026-04-17 19:15:00', '核心计费链路联调完成，进入回归验证。'),
(2, 'Jenkins 构建节点扩容', '运维动作', '5G工单调度平台', '宋佳音', '处理中', TIMESTAMP '2026-04-17 18:10:00', '已新增两个构建节点，等待流水线重新分配。'),
(3, '客户画像标签链路修复评审', '评审记录', '客户画像分析平台', '刘子航', '待跟进', TIMESTAMP '2026-04-17 16:45:00', '需要补充离线标签回灌方案。'),
(4, 'Prometheus 告警模板更新', '平台治理', '云网运维门户', '王泽远', '已完成', TIMESTAMP '2026-04-17 15:20:00', '新增 MTTR 与失败率双阈值模板。'),
(5, '企业月报历史记录入库', '报表留痕', '企业级', '系统管理负责人', '已完成', TIMESTAMP '2026-04-17 14:10:00', '四类核心指标已形成管理端历史记录。'),
(6, '工单回退验收项关闭', '测试进展', '5G工单调度平台', '周一凡', '已完成', TIMESTAMP '2026-04-16 18:00:00', '本轮回退场景关闭 6 项遗留问题。');
