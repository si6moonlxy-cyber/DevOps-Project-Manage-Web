INSERT INTO department (id, dept_name) VALUES
(1, '组织与权限治理中心'),
(2, '企业交付管理中心'),
(3, '数据治理中心'),
(4, 'DevOps 工具接入中心'),
(5, '指标分析中心'),
(6, '质量保障中心'),
(7, '运维稳定性中心'),
(8, '平台架构中心'),
(9, 'PMO 管理中心'),
(10, '系统审计中心');

INSERT INTO team (id, dept_id, team_name, focus_area, member_count) VALUES
(1, 1, '统一身份管理组', '用户与权限治理', 6),
(2, 1, '访问控制治理组', '菜单与角色授权', 5),
(3, 2, '企业交付 PMO', '项目推进与经营分析', 8),
(4, 3, '数据标准化组', '主数据与字段治理', 7),
(5, 4, '工具接入研发组', 'Jira 与 GitLab 接入', 6),
(6, 5, '指标分析组', '效能度量与趋势分析', 5),
(7, 6, '测试质量组', '测试通过率与缺陷治理', 7),
(8, 7, '稳定性保障组', '告警与故障恢复', 6),
(9, 8, '微服务架构组', 'Spring Boot 与网关治理', 5),
(10, 10, '平台审计组', '审计巡检与配置复核', 4);

INSERT INTO admin_account (id, username, login_password, display_name, role_code, role_name, account_status, last_login_at, description) VALUES
(1, 'sys_root', 'Sys@2026', '系统管理负责人', 'SYSTEM_ADMIN', '系统管理', 'ACTIVE', TIMESTAMP '2026-04-27 08:36:00', '可查看五个业务域的全部系统级页面'),
(2, 'sys_audit', 'Audit@2026', '系统审计管理员', 'SYSTEM_ADMIN', '系统管理', 'ACTIVE', TIMESTAMP '2026-04-26 15:20:00', '负责系统审计、配置和查询控制台'),
(3, 'sys_ops', 'Ops@2026', '平台运维管理员', 'SYSTEM_ADMIN', '系统管理', 'ACTIVE', TIMESTAMP '2026-04-27 07:58:00', '负责运行环境巡检和服务节点维护'),
(4, 'sys_config', 'Cfg@2026', '平台配置管理员', 'SYSTEM_ADMIN', '系统管理', 'ACTIVE', TIMESTAMP '2026-04-26 17:40:00', '负责目标数据库、采集策略和配置台账'),
(5, 'enterprise_admin', 'Corp@2026', '企业交付管理员', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-27 08:10:00', '聚焦项目交付、DevOps 数据和指标页面'),
(6, 'enterprise_pm', 'Pm@2026', '企业项目负责人', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-25 14:15:00', '用于演示企业管理端登录入口'),
(7, 'enterprise_quality', 'Qa@2026', '企业质量经理', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-26 13:05:00', '关注测试质量、缺陷和发布稳定性'),
(8, 'enterprise_arch', 'Arch@2026', '企业架构负责人', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-24 18:12:00', '关注平台架构、服务治理和技术演进'),
(9, 'enterprise_delivery', 'Del@2026', '企业交付主管', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-27 08:02:00', '负责跨团队交付节奏与风险协同'),
(10, 'enterprise_test', 'Test@2026', '企业测试主管', 'ENTERPRISE_ADMIN', '企业管理', 'ACTIVE', TIMESTAMP '2026-04-26 11:35:00', '负责测试排期、回归验证和质量复盘');
