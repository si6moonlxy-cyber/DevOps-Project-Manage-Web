(function () {
    var createApp = Vue.createApp;
    var ref = Vue.ref;
    var computed = Vue.computed;
    var nextTick = Vue.nextTick;
    var onMounted = Vue.onMounted;
    var onBeforeUnmount = Vue.onBeforeUnmount;

    createApp({
        setup: function () {
            var authResolved = ref(false);
            var sessionUser = ref(null);
            var navigation = ref(null);
            var currentModuleCode = ref('');
            var currentPageCode = ref('');
            var pagePayloads = ref({});
            var loginForm = ref({
                username: 'sys_root',
                password: 'Sys@2026'
            });
            var submitting = ref(false);
            var pageLoading = ref(false);
            var message = ref('');
            var themeMode = ref(localStorage.getItem('themeMode') || 'dark');
            var globalSearch = ref('');
            var tableFilters = ref({});
            var pageState = ref({});
            var queryLoading = ref(false);
            var queryMessage = ref('');
            var queryForm = ref({
                sql: ''
            });
            var queryResult = ref({
                columns: [],
                rows: []
            });
            var editForms = ref({});

            var charts = {
                projectStatus: null,
                workItemStatus: null,
                sourceStatus: null,
                metricTrend: null,
                reportScore: null,
                alertLevel: null
            };

            var demoAccounts = [
                { roleName: '系统管理', username: 'sys_root', password: 'Sys@2026' },
                { roleName: '系统管理', username: 'sys_audit', password: 'Audit@2026' },
                { roleName: '企业管理', username: 'enterprise_admin', password: 'Corp@2026' },
                { roleName: '企业管理', username: 'enterprise_pm', password: 'Pm@2026' }
            ];

            var pageDescriptions = {
                'organization-overview': '组织与权限域负责承载登录鉴权、角色导航和答辩演示入口。',
                'permission-center': '系统管理可查看管理账号、角色信息与权限说明。',
                'project-delivery': '项目交付域展示项目看板、里程碑与交付动态。',
                'work-item-center': '工作项清单保留现有 UI 风格，并按业务域文案重组。',
                'devops-overview': 'DevOps 数据域展示 Jira、GitLab、Jenkins、Prometheus 的样例接入状态，当前定位为接入展示和预留对接能力。',
                'collection-pipeline': '系统管理可查看采集任务、执行计划与链路阶段，当前为演示态配置。',
                'metrics-overview': '指标与报告域承接核心效能指标趋势和口径展示。',
                'report-center': '展示报表历史、评分趋势和留痕状态，当前版本不提供生成或导出能力。',
                'audit-events': '审计与配置域集中展示告警恢复、审计动作和值班视角。',
                'config-console': '配置控制台承载目标库配置、服务注册与只读 SQL 巡检。'
            };

            function fillDemoAccount(demo) {
                loginForm.value.username = demo.username;
                loginForm.value.password = demo.password;
            }

            var metricColors = {
                REQ_DELIVERY_CYCLE: '#ffb255',
                DEPLOY_FREQUENCY: '#5c84f6',
                CHANGE_FAILURE_RATE: '#ff7b75',
                MTTR: '#33c48d'
            };

            var modules = computed(function () {
                return navigation.value && navigation.value.modules ? navigation.value.modules : [];
            });

            var currentModule = computed(function () {
                return findModule(currentModuleCode.value);
            });

            var currentPages = computed(function () {
                return currentModule.value && currentModule.value.pages ? currentModule.value.pages : [];
            });

            var currentPage = computed(function () {
                return findPage(currentPageCode.value);
            });

            var currentPayload = computed(function () {
                return pagePayloads.value[currentPageCode.value] || {};
            });

            var userInitial = computed(function () {
                if (!sessionUser.value || !sessionUser.value.displayName) {
                    return 'D';
                }
                return sessionUser.value.displayName.slice(0, 1);
            });

            var pageDescription = computed(function () {
                return pageDescriptions[currentPageCode.value] || '';
            });

            function applyTheme() {
                document.body.classList.toggle('theme-light', themeMode.value === 'light');
                document.body.classList.toggle('theme-dark', themeMode.value !== 'light');
                localStorage.setItem('themeMode', themeMode.value);
            }

            function toggleTheme() {
                themeMode.value = themeMode.value === 'dark' ? 'light' : 'dark';
                applyTheme();
                nextTick(renderCurrentCharts);
            }

            function tableKey(name) {
                return currentPageCode.value + ':' + name;
            }

            function filterText(name) {
                return tableFilters.value[tableKey(name)] || '';
            }

            function setFilterText(name, value) {
                tableFilters.value[tableKey(name)] = value || '';
                pageState.value[tableKey(name)] = 1;
            }

            function pageNo(name) {
                return pageState.value[tableKey(name)] || 1;
            }

            function setPageNo(name, value) {
                pageState.value[tableKey(name)] = Math.max(1, value);
            }

            function matchesRow(row, keyword) {
                if (!keyword) {
                    return true;
                }
                var words = keyword.toLowerCase().split(/\s+/).filter(Boolean);
                var text = Object.keys(row || {}).map(function (key) {
                    return row[key];
                }).join(' ').toLowerCase();
                return words.every(function (word) {
                    return text.indexOf(word) !== -1;
                });
            }

            function viewRows(name, rows) {
                var source = rows || [];
                var keyword = (globalSearch.value + ' ' + filterText(name)).trim();
                var filtered = source.filter(function (row) {
                    return matchesRow(row, keyword);
                });
                var size = 8;
                var page = pageNo(name);
                var maxPage = Math.max(1, Math.ceil(filtered.length / size));
                if (page > maxPage) {
                    page = maxPage;
                    setPageNo(name, page);
                }
                return filtered.slice((page - 1) * size, page * size);
            }

            function pageInfo(name, rows) {
                var source = rows || [];
                var keyword = (globalSearch.value + ' ' + filterText(name)).trim();
                var total = source.filter(function (row) {
                    return matchesRow(row, keyword);
                }).length;
                var size = 8;
                return {
                    total: total,
                    page: pageNo(name),
                    pages: Math.max(1, Math.ceil(total / size))
                };
            }

            function changePage(name, delta) {
                var info = pageInfo(name, currentTableRows(name));
                var next = Math.min(info.pages, Math.max(1, info.page + delta));
                setPageNo(name, next);
            }

            function currentTableRows(name) {
                var payload = currentPayload.value || {};
                if (name === 'projects') {
                    return payload.projects || [];
                }
                if (name === 'workItems') {
                    return payload.items || [];
                }
                if (name === 'sources') {
                    return payload.sources || [];
                }
                if (name === 'accounts') {
                    return payload.accounts || [];
                }
                if (name === 'members') {
                    return payload.members || [];
                }
                return [];
            }

            function requestJson(url, options) {
                var fetchOptions = options || {};
                fetchOptions.headers = fetchOptions.headers || {};
                if (!fetchOptions.headers['Content-Type'] && fetchOptions.method && fetchOptions.method !== 'GET') {
                    fetchOptions.headers['Content-Type'] = 'application/json';
                }
                return fetch(url, fetchOptions).then(function (response) {
                    return response.text().then(function (text) {
                        var data = text ? JSON.parse(text) : {};
                        if (!response.ok) {
                            if (response.status === 401) {
                                sessionUser.value = null;
                                navigation.value = null;
                                authResolved.value = true;
                            }
                            throw new Error(data.message || '请求失败');
                        }
                        return data;
                    });
                });
            }

            function savePayload(pageCode, payload) {
                pagePayloads.value[pageCode || currentPageCode.value] = payload;
                return nextTick().then(renderCurrentCharts);
            }

            function mutate(url, method, body, pageCode) {
                message.value = '';
                return requestJson(url, {
                    method: method,
                    body: body ? JSON.stringify(body) : undefined
                }).then(function (payload) {
                    return savePayload(pageCode, payload);
                }).catch(function (error) {
                    message.value = error.message;
                    return null;
                });
            }

            function canEditProjects() {
                return sessionUser.value && (sessionUser.value.username === 'sys_root' || sessionUser.value.roleCode === 'ENTERPRISE_ADMIN');
            }

            function startEdit(type, row) {
                editForms.value[type] = Object.assign({}, row || {});
            }

            function cancelEdit(type) {
                editForms.value[type] = null;
            }

            function formValue(type) {
                return editForms.value[type] || {};
            }

            function createProjectForm() {
                startEdit('project', {
                    projectCode: 'PRJ' + new Date().getTime(),
                    projectName: '',
                    businessDomain: '企业项目',
                    statusCode: 'PLANNING',
                    description: ''
                });
            }

            function saveProject() {
                var form = formValue('project');
                if (!form.projectCode || !form.projectName) {
                    message.value = '请填写项目编码和项目名称。';
                    return;
                }
                var method = form.projectId ? 'PUT' : 'POST';
                var url = form.projectId ? '/api/portal/projects/' + form.projectId : '/api/portal/projects';
                return mutate(url, method, form, 'project-delivery').then(function (result) {
                    if (result !== null) {
                        cancelEdit('project');
                    }
                });
            }

            function createWorkItemForm() {
                var projects = currentPayload.value.projects || [];
                startEdit('workItem', {
                    projectId: projects.length ? projects[0].projectId : '',
                    itemTitle: '',
                    itemTypeCode: 'TASK',
                    priority: 'MEDIUM',
                    statusCode: 'TODO',
                    ownerName: ''
                });
            }

            function deleteProject(project) {
                if (!project || !project.projectId || !window.confirm('确定归档该项目吗？归档后不再显示在看板中。')) {
                    return;
                }
                return mutate('/api/portal/projects/' + project.projectId, 'DELETE', null, 'project-delivery');
            }

            function saveWorkItem() {
                var form = formValue('workItem');
                if (!form.projectId || !form.itemTitle) {
                    message.value = '请选择项目并填写工作项标题。';
                    return;
                }
                var method = form.itemId ? 'PUT' : 'POST';
                var url = form.itemId ? '/api/portal/work-items/' + form.itemId : '/api/portal/work-items';
                return mutate(url, method, form, 'work-item-center').then(function (result) {
                    if (result !== null) {
                        cancelEdit('workItem');
                    }
                });
            }

            function deleteWorkItem(item) {
                if (!item || !item.itemId || !window.confirm('确定删除该工作项吗？')) {
                    return;
                }
                return mutate('/api/portal/work-items/' + item.itemId, 'DELETE', null, 'work-item-center');
            }

            function saveSource(source) {
                if (!source || !source.sourceId || !source.endpointUrl) {
                    message.value = '请填写数据源 API 地址。';
                    return;
                }
                var body = Object.assign({}, source);
                return mutate('/api/portal/sources/' + source.sourceId, 'PUT', body, 'devops-overview').then(function (result) {
                    if (result !== null) {
                        cancelEdit('source');
                    }
                });
            }

            function editSource(source) {
                startEdit('source', Object.assign({}, source, {
                    visible: source.sourceStatus === 'ACTIVE'
                }));
            }

            function saveAccount(account) {
                if (!account.username || !account.roleId) {
                    message.value = '请填写账号和角色。';
                    return;
                }
                return mutate('/api/portal/accounts/' + account.userId, 'PUT', account, 'permission-center').then(function (result) {
                    if (result !== null) {
                        cancelEdit('account');
                    }
                });
            }

            function saveMember() {
                var form = formValue('member');
                if (!form.teamId || !form.username || !form.displayName) {
                    message.value = '请填写成员姓名、账号并选择团队。';
                    return;
                }
                var method = form.userId ? 'PUT' : 'POST';
                var url = form.userId ? '/api/portal/members/' + form.userId : '/api/portal/members';
                return mutate(url, method, form, 'organization-overview').then(function (result) {
                    if (result !== null) {
                        cancelEdit('member');
                    }
                });
            }

            function deleteMember(member) {
                if (!member || !member.userId || !window.confirm('确定移除该成员吗？移除后成员账号将停用。')) {
                    return;
                }
                return mutate('/api/portal/members/' + member.userId, 'DELETE', null, 'organization-overview');
            }

            function createMemberForm() {
                var teams = currentPayload.value.teams || [];
                startEdit('member', {
                    username: '',
                    displayName: '',
                    password: 'User@2026',
                    teamId: teams.length ? teams[0].teamId : ''
                });
            }

            function bootstrap() {
                requestJson('/api/auth/me').then(function (payload) {
                    applyAuthPayload(payload);
                }).catch(function () {
                    authResolved.value = true;
                });
            }

            function applyAuthPayload(payload) {
                sessionUser.value = payload.user;
                navigation.value = payload.navigation;
                authResolved.value = true;
                restoreRoute();
            }

            function login() {
                submitting.value = true;
                message.value = '';
                requestJson('/api/auth/login', {
                    method: 'POST',
                    body: JSON.stringify(loginForm.value)
                }).then(function (payload) {
                    applyAuthPayload(payload);
                }).catch(function (error) {
                    message.value = error.message;
                }).finally(function () {
                    submitting.value = false;
                });
            }

            function logout() {
                requestJson('/api/auth/logout', {
                    method: 'POST'
                }).finally(function () {
                    sessionUser.value = null;
                    navigation.value = null;
                    authResolved.value = true;
                    currentModuleCode.value = '';
                    currentPageCode.value = '';
                    pagePayloads.value = {};
                    queryForm.value.sql = '';
                    queryResult.value = { columns: [], rows: [] };
                    window.location.hash = '';
                });
            }

            function findModule(code) {
                return modules.value.find(function (item) {
                    return item.code === code;
                }) || null;
            }

            function findPage(code) {
                var page = null;
                modules.value.some(function (module) {
                    return module.pages.some(function (item) {
                        if (item.code === code) {
                            page = item;
                            return true;
                        }
                        return false;
                    });
                });
                return page;
            }

            function restoreRoute() {
                var hash = (window.location.hash || '').replace('#', '');
                var segments = hash.split('/');
                var targetModule = findModule(segments[0]) || modules.value[0] || null;
                if (!targetModule) {
                    return;
                }
                var targetPage = targetModule.pages.find(function (page) {
                    return page.code === segments[1];
                }) || targetModule.pages[0];

                currentModuleCode.value = targetModule.code;
                currentPageCode.value = targetPage.code;
                syncHash();
                loadCurrentPage();
            }

            function syncHash() {
                if (currentModuleCode.value && currentPageCode.value) {
                    window.location.hash = currentModuleCode.value + '/' + currentPageCode.value;
                }
            }

            function openModule(moduleCode) {
                var module = findModule(moduleCode);
                if (!module || !module.pages.length) {
                    return;
                }
                currentModuleCode.value = module.code;
                openPage(module.pages[0].code);
            }

            function openPage(pageCode) {
                currentPageCode.value = pageCode;
                syncHash();
                loadCurrentPage();
            }

            function refreshCurrentPage() {
                loadCurrentPage(true);
            }

            function loadCurrentPage(forceReload) {
                if (!currentPage.value) {
                    return;
                }
                if (!forceReload && pagePayloads.value[currentPageCode.value]) {
                    nextTick(renderCurrentCharts);
                    return;
                }

                pageLoading.value = true;
                message.value = '';
                requestJson(currentPage.value.endpoint).then(function (payload) {
                    pagePayloads.value[currentPageCode.value] = payload;
                    if (currentPageCode.value === 'config-console' && !queryForm.value.sql) {
                        queryForm.value.sql = payload.sampleSqls && payload.sampleSqls.length ? payload.sampleSqls[0] : '';
                    }
                    return nextTick();
                }).then(function () {
                    renderCurrentCharts();
                }).catch(function (error) {
                    message.value = error.message;
                }).finally(function () {
                    pageLoading.value = false;
                });
            }

            function executeQuery() {
                if (!queryForm.value.sql) {
                    queryMessage.value = '请输入查询语句。';
                    return;
                }
                queryLoading.value = true;
                queryMessage.value = '';
                requestJson('/api/portal/page/config-console/query', {
                    method: 'POST',
                    body: JSON.stringify(queryForm.value)
                }).then(function (payload) {
                    queryResult.value = payload;
                    queryMessage.value = '查询成功，共返回 ' + payload.rowCount + ' 行。';
                }).catch(function (error) {
                    queryMessage.value = error.message;
                }).finally(function () {
                    queryLoading.value = false;
                });
            }

            function runSampleSql(sql) {
                queryForm.value.sql = sql;
                executeQuery();
            }

            function disposeCharts() {
                Object.keys(charts).forEach(function (key) {
                    if (charts[key]) {
                        charts[key].dispose();
                        charts[key] = null;
                    }
                });
            }

            function renderCurrentCharts() {
                disposeCharts();
                if (currentPageCode.value === 'project-delivery') {
                    renderDonutChart('projectStatusChart', currentPayload.value.statusDistribution || [], ['#5c84f6', '#33c48d', '#ffb255', '#ff7b75'], 'projectStatus');
                }
                if (currentPageCode.value === 'work-item-center') {
                    renderDonutChart('workItemStatusChart', currentPayload.value.statusDistribution || [], ['#5c84f6', '#ffb255', '#33c48d', '#ff7b75'], 'workItemStatus');
                }
                if (currentPageCode.value === 'devops-overview') {
                    renderDonutChart('sourceStatusChart', currentPayload.value.statusDistribution || [], ['#33c48d', '#ffb255', '#ff7b75'], 'sourceStatus');
                }
                if (currentPageCode.value === 'metrics-overview') {
                    renderMetricTrendChart();
                }
                if (currentPageCode.value === 'report-center') {
                    renderReportChart();
                }
                if (currentPageCode.value === 'audit-events') {
                    renderDonutChart('alertLevelChart', currentPayload.value.levelDistribution || [], ['#ff7b75', '#ffb255', '#5c84f6'], 'alertLevel');
                }
            }

            function renderDonutChart(elementId, data, palette, chartKey) {
                var chartEl = document.getElementById(elementId);
                if (!chartEl) {
                    return;
                }
                charts[chartKey] = echarts.init(chartEl);
                charts[chartKey].setOption({
                    tooltip: { trigger: 'item' },
                    legend: { bottom: 0, textStyle: { color: chartTextColor() } },
                    series: [{
                        type: 'pie',
                        radius: ['45%', '72%'],
                        data: data.map(function (item, index) {
                            return {
                                value: item.value,
                                name: item.name,
                                itemStyle: { color: palette[index % palette.length] }
                            };
                        })
                    }]
                });
            }

            function renderMetricTrendChart() {
                var chartEl = document.getElementById('metricTrendChart');
                if (!chartEl || !currentPayload.value.metricTrend) {
                    return;
                }
                charts.metricTrend = echarts.init(chartEl);
                charts.metricTrend.setOption({
                    tooltip: { trigger: 'axis' },
                    legend: { top: 0, textStyle: { color: chartTextColor() } },
                    grid: { left: 20, right: 20, bottom: 20, top: 54, containLabel: true },
                    xAxis: {
                        type: 'category',
                        data: currentPayload.value.metricTrend.labels || [],
                        axisLabel: { color: chartMutedColor() }
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: { color: chartMutedColor() },
                        splitLine: { lineStyle: { color: chartLineColor() } }
                    },
                    series: (currentPayload.value.metricTrend.series || []).map(function (item) {
                        return {
                            name: item.name,
                            type: 'line',
                            smooth: true,
                            symbolSize: 8,
                            data: item.data,
                            lineStyle: { width: 3, color: metricColors[item.code] || '#5c84f6' },
                            itemStyle: { color: metricColors[item.code] || '#5c84f6' },
                            areaStyle: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                    { offset: 0, color: (metricColors[item.code] || '#5c84f6') + '55' },
                                    { offset: 1, color: (metricColors[item.code] || '#5c84f6') + '08' }
                                ])
                            }
                        };
                    })
                });
            }

            function renderReportChart() {
                var chartEl = document.getElementById('reportScoreChart');
                if (!chartEl || !currentPayload.value.scoreTrend) {
                    return;
                }
                charts.reportScore = echarts.init(chartEl);
                charts.reportScore.setOption({
                    tooltip: { trigger: 'axis' },
                    grid: { left: 20, right: 20, bottom: 20, top: 24, containLabel: true },
                    xAxis: {
                        type: 'category',
                        data: currentPayload.value.scoreTrend.labels || [],
                        axisLabel: { color: chartMutedColor() }
                    },
                    yAxis: {
                        type: 'value',
                        min: 70,
                        axisLabel: { color: chartMutedColor() },
                        splitLine: { lineStyle: { color: chartLineColor() } }
                    },
                    series: [{
                        type: 'line',
                        smooth: true,
                        data: currentPayload.value.scoreTrend.values || [],
                        lineStyle: { width: 3, color: '#5c84f6' },
                        itemStyle: { color: '#5c84f6' },
                        areaStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: '#5c84f655' },
                                { offset: 1, color: '#5c84f608' }
                            ])
                        }
                    }]
                });
            }

            function chartTextColor() {
                return themeMode.value === 'dark' ? '#e6edf7' : '#223046';
            }

            function chartMutedColor() {
                return themeMode.value === 'dark' ? '#9aa8bb' : '#7c8a9f';
            }

            function chartLineColor() {
                return themeMode.value === 'dark' ? '#2a3548' : '#edf1f7';
            }

            function formatDelta(delta) {
                if (delta === null || delta === undefined) {
                    return '--';
                }
                var value = Number(delta);
                if (Number.isNaN(value)) {
                    return String(delta);
                }
                return (value > 0 ? '+' : '') + value.toFixed(1) + '%';
            }

            function warningClass(level) {
                if (level === 'NORMAL') {
                    return 'tag-success';
                }
                if (level === 'WARNING') {
                    return 'tag-warning';
                }
                return 'tag-danger';
            }

            function riskClass(level) {
                if (level === 'LOW') {
                    return 'tag-success';
                }
                if (level === 'MEDIUM') {
                    return 'tag-warning';
                }
                return 'tag-danger';
            }

            function priorityClass(level) {
                if (level === '高') {
                    return 'tag-danger';
                }
                if (level === '中') {
                    return 'tag-warning';
                }
                return 'tag-success';
            }

            function sourceClass(status) {
                if (status === 'ONLINE' || status === 'RUNNING') {
                    return 'tag-success';
                }
                if (status === 'WARNING') {
                    return 'tag-warning';
                }
                return 'tag-danger';
            }

            function alertClass(level) {
                if (level === 'P1') {
                    return 'tag-danger';
                }
                if (level === 'P2') {
                    return 'tag-warning';
                }
                return 'tag-brand';
            }

            function resizeCharts() {
                Object.keys(charts).forEach(function (key) {
                    if (charts[key]) {
                        charts[key].resize();
                    }
                });
            }

            function handleHashChange() {
                if (!sessionUser.value) {
                    return;
                }
                restoreRoute();
            }

            onMounted(function () {
                applyTheme();
                bootstrap();
                window.addEventListener('resize', resizeCharts);
                window.addEventListener('hashchange', handleHashChange);
            });

            onBeforeUnmount(function () {
                disposeCharts();
                window.removeEventListener('resize', resizeCharts);
                window.removeEventListener('hashchange', handleHashChange);
            });

            return {
                authResolved: authResolved,
                sessionUser: sessionUser,
                navigation: navigation,
                modules: modules,
                currentModule: currentModule,
                currentPages: currentPages,
                currentModuleCode: currentModuleCode,
                currentPage: currentPage,
                currentPageCode: currentPageCode,
                currentPayload: currentPayload,
                themeMode: themeMode,
                globalSearch: globalSearch,
                tableFilters: tableFilters,
                editForms: editForms,
                loginForm: loginForm,
                submitting: submitting,
                pageLoading: pageLoading,
                message: message,
                queryLoading: queryLoading,
                queryMessage: queryMessage,
                queryForm: queryForm,
                queryResult: queryResult,
                demoAccounts: demoAccounts,
                userInitial: userInitial,
                pageDescription: pageDescription,
                fillDemoAccount: fillDemoAccount,
                login: login,
                logout: logout,
                openModule: openModule,
                openPage: openPage,
                refreshCurrentPage: refreshCurrentPage,
                toggleTheme: toggleTheme,
                filterText: filterText,
                setFilterText: setFilterText,
                viewRows: viewRows,
                pageInfo: pageInfo,
                changePage: changePage,
                startEdit: startEdit,
                cancelEdit: cancelEdit,
                formValue: formValue,
                canEditProjects: canEditProjects,
                createProjectForm: createProjectForm,
                saveProject: saveProject,
                deleteProject: deleteProject,
                createWorkItemForm: createWorkItemForm,
                canEditAccounts: function () {
                    return sessionUser.value && sessionUser.value.roleCode === 'SYSTEM_ADMIN';
                },
                saveWorkItem: saveWorkItem,
                deleteWorkItem: deleteWorkItem,
                editSource: editSource,
                saveSource: saveSource,
                saveAccount: saveAccount,
                createMemberForm: createMemberForm,
                saveMember: saveMember,
                deleteMember: deleteMember,
                executeQuery: executeQuery,
                runSampleSql: runSampleSql,
                formatDelta: formatDelta,
                warningClass: warningClass,
                riskClass: riskClass,
                priorityClass: priorityClass,
                sourceClass: sourceClass,
                alertClass: alertClass
            };
        }
    }).mount('#app');
})();
