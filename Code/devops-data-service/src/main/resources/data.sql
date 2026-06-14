INSERT INTO data_source (id, source_name, source_type, sync_status, sync_owner, last_sync_time, api_health, today_increment, remark) VALUES
(1, '需求中心 Jira', 'JIRA', 'ONLINE', '陈文博', TIMESTAMP '2026-04-27 08:30:00', 98, 126, '需求与缺陷数据已按业务域完成映射'),
(2, '禅道项目库', 'ZENTAO', 'ONLINE', '李清妍', TIMESTAMP '2026-04-27 08:18:00', 95, 74, '遗留项目需求与缺陷已同步到标准结构'),
(3, '代码仓 GitLab', 'GITLAB', 'ONLINE', '刘子航', TIMESTAMP '2026-04-27 08:28:00', 96, 84, '提交记录、MR 和分支数据同步正常'),
(4, '代码镜像 GitHub', 'GITHUB', 'WARNING', '赵廷轩', TIMESTAMP '2026-04-27 07:56:00', 83, 26, '镜像仓同步延迟较高，需要继续观察'),
(5, '流水线 Jenkins', 'JENKINS', 'WARNING', '宋佳音', TIMESTAMP '2026-04-27 08:15:00', 82, 37, '构建节点波动已记录到采集链路页面'),
(6, 'GitLab CI', 'GITLAB_CI', 'ONLINE', '李哲宇', TIMESTAMP '2026-04-27 08:12:00', 91, 42, '容器构建结果已纳入部署频率指标'),
(7, '质量平台 SonarQube', 'SONARQUBE', 'ONLINE', '顾明轩', TIMESTAMP '2026-04-27 08:06:00', 93, 19, '代码质量告警已与缺陷治理联动'),
(8, '监控 Prometheus', 'PROMETHEUS', 'ONLINE', '王泽远', TIMESTAMP '2026-04-27 08:32:00', 94, 58, '恢复事件将回流到审计与配置域'),
(9, '稳定性平台 Zabbix', 'ZABBIX', 'ONLINE', '何知远', TIMESTAMP '2026-04-27 08:02:00', 90, 17, '网络与主机告警已进入统一审计视图'),
(10, '制品仓 Harbor', 'HARBOR', 'ONLINE', '邵安琪', TIMESTAMP '2026-04-27 08:09:00', 96, 33, '镜像制品信息已支持版本交付看板展示');

INSERT INTO collection_task (id, task_name, source_name, schedule_expr, task_status, last_run_time, next_run_time) VALUES
(1, 'Jira 增量同步任务', '需求中心 Jira', '0 */10 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:30:00', TIMESTAMP '2026-04-27 08:40:00'),
(2, '禅道 历史缺陷补采任务', '禅道项目库', '0 */20 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:18:00', TIMESTAMP '2026-04-27 08:38:00'),
(3, 'GitLab 提交采集任务', '代码仓 GitLab', '0 */15 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:28:00', TIMESTAMP '2026-04-27 08:43:00'),
(4, 'GitHub 镜像一致性校验', '代码镜像 GitHub', '0 */30 * * * ?', 'WARNING', TIMESTAMP '2026-04-27 07:56:00', TIMESTAMP '2026-04-27 08:26:00'),
(5, 'Jenkins 构建分析任务', '流水线 Jenkins', '0 */20 * * * ?', 'WARNING', TIMESTAMP '2026-04-27 08:15:00', TIMESTAMP '2026-04-27 08:35:00'),
(6, 'GitLab CI 结果回流任务', 'GitLab CI', '0 */15 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:12:00', TIMESTAMP '2026-04-27 08:27:00'),
(7, 'SonarQube 质量告警采集', '质量平台 SonarQube', '0 */30 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:06:00', TIMESTAMP '2026-04-27 08:36:00'),
(8, 'Prometheus 告警回流任务', '监控 Prometheus', '0 */10 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:32:00', TIMESTAMP '2026-04-27 08:42:00'),
(9, 'Zabbix 主机健康采集任务', '稳定性平台 Zabbix', '0 */20 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:02:00', TIMESTAMP '2026-04-27 08:22:00'),
(10, 'Harbor 制品版本同步任务', '制品仓 Harbor', '0 */25 * * * ?', 'RUNNING', TIMESTAMP '2026-04-27 08:09:00', TIMESTAMP '2026-04-27 08:34:00');
