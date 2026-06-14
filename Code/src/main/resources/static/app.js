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
            var queryLoading = ref(false);
            var queryMessage = ref('');
            var queryForm = ref({
                sql: ''
            });
            var queryResult = ref({
                columns: [],
                rows: []
            });

            var charts = {
                metricTrend: null,
                metricStatus: null,
                projectStatus: null,
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
                overview: '以首页形式汇总平台角色、项目规模、近期动态和里程碑，作为答辩演示入口。',
                activity: '按时间线查看交付动作、平台治理和版本推进记录。',
                metrics: '展示需求周期、部署频率、变更失败率和 MTTR 等核心指标。',
                sources: '展示 Jira、GitLab、Jenkins、Prometheus 等工具的样例接入状态，当前定位为接入展示和预留对接能力。',
                workitems: '展示任务、需求、缺陷和评审项，用于补充说明项目执行过程。',
                projects: '按风险等级与交付阶段展示项目看板和交付指标。',
                reports: '仅展示报表历史、评分趋势和留痕状态，当前版本不提供生成或导出能力。',
                alerts: '集中查看当前异常告警与恢复记录。',
                accounts: '系统管理员查看管理端账号、角色和页面范围，密码字段已脱敏。',
                database: '系统管理员使用只读数据库控制台查询 H2 中的数据。'
            };

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

            function fillDemoAccount(demo) {
                loginForm.value.username = demo.username;
                loginForm.value.password = demo.password;
            }

            function requestJson(url, options) {
                var fetchOptions = options || {};
                fetchOptions.headers = fetchOptions.headers || {};
                if (!fetchOptions.headers['Content-Type'] && fetchOptions.method && fetchOptions.method !== 'GET') {
                    fetchOptions.headers['Content-Type'] = 'application/json';
                }
                return fetch(url, fetchOptions).then(function (response) {
                    return response.text().then(function (text) {
                        var data = {};
                        if (text) {
                            data = JSON.parse(text);
                        }
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

            function bootstrap() {
                requestJson('/api/auth/me').then(function (payload) {
                    applyAuthPayload(payload);
                }).catch(function () {
                    authResolved.value = true;
                });
            }

            function findModule(code) {
                return modules.value.find(function (module) {
                    return module.code === code;
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
                requestJson(currentPage.value.endpoint).then(function (payload) {
                    pagePayloads.value[currentPageCode.value] = payload;
                    if (currentPageCode.value === 'database' && !queryForm.value.sql) {
                        queryForm.value.sql = payload.sampleSqls && payload.sampleSqls.length ? payload.sampleSqls[0] : '';
                    }
                    if (currentPageCode.value !== 'database') {
                        queryMessage.value = '';
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
                if (currentPageCode.value === 'metrics') {
                    renderMetricCharts();
                }
                if (currentPageCode.value === 'projects') {
                    renderProjectChart();
                }
                if (currentPageCode.value === 'reports') {
                    renderReportChart();
                }
                if (currentPageCode.value === 'alerts') {
                    renderAlertChart();
                }
            }

            function renderMetricCharts() {
                var trendEl = document.getElementById('metricTrendChart');
                var statusEl = document.getElementById('metricStatusChart');
                if (!trendEl || !statusEl) {
                    return;
                }
                charts.metricTrend = echarts.init(trendEl);
                charts.metricStatus = echarts.init(statusEl);

                charts.metricTrend.setOption({
                    tooltip: { trigger: 'axis' },
                    legend: { top: 0 },
                    grid: { left: 20, right: 20, bottom: 20, top: 50, containLabel: true },
                    xAxis: {
                        type: 'category',
                        data: currentPayload.value.metricTrend.labels,
                        axisLabel: { color: '#7c8a9f' }
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: { color: '#7c8a9f' },
                        splitLine: { lineStyle: { color: '#edf1f7' } }
                    },
                    series: currentPayload.value.metricTrend.series.map(function (item) {
                        return {
                            name: item.name,
                            type: 'line',
                            smooth: true,
                            symbolSize: 8,
                            data: item.data,
                            lineStyle: { width: 3, color: metricColors[item.code] },
                            itemStyle: { color: metricColors[item.code] },
                            areaStyle: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                    { offset: 0, color: metricColors[item.code] + '55' },
                                    { offset: 1, color: metricColors[item.code] + '05' }
                                ])
                            }
                        };
                    })
                });

                charts.metricStatus.setOption({
                    tooltip: { trigger: 'item' },
                    legend: { bottom: 0 },
                    series: [{
                        type: 'pie',
                        radius: ['48%', '72%'],
                        data: (currentPayload.value.projectStatus || []).map(function (item, index) {
                            var palette = ['#5c84f6', '#ffb255', '#33c48d', '#ff7b75'];
                            return {
                                value: item.value,
                                name: item.name,
                                itemStyle: { color: palette[index % palette.length] }
                            };
                        })
                    }]
                });
            }

            function renderProjectChart() {
                var chartEl = document.getElementById('projectStatusChart');
                if (!chartEl) {
                    return;
                }
                charts.projectStatus = echarts.init(chartEl);
                charts.projectStatus.setOption({
                    tooltip: { trigger: 'item' },
                    legend: { bottom: 0 },
                    series: [{
                        type: 'pie',
                        radius: ['44%', '70%'],
                        data: (currentPayload.value.projectStatus || []).map(function (item, index) {
                            var palette = ['#5c84f6', '#33c48d', '#ffb255', '#ff7b75'];
                            return {
                                value: item.value,
                                name: item.name,
                                itemStyle: { color: palette[index % palette.length] }
                            };
                        })
                    }]
                });
            }

            function renderReportChart() {
                var chartEl = document.getElementById('reportScoreChart');
                if (!chartEl) {
                    return;
                }
                charts.reportScore = echarts.init(chartEl);
                charts.reportScore.setOption({
                    tooltip: { trigger: 'axis' },
                    grid: { left: 20, right: 20, bottom: 20, top: 24, containLabel: true },
                    xAxis: {
                        type: 'category',
                        data: currentPayload.value.scoreTrend.labels,
                        axisLabel: { color: '#7c8a9f' }
                    },
                    yAxis: {
                        type: 'value',
                        min: 70,
                        axisLabel: { color: '#7c8a9f' },
                        splitLine: { lineStyle: { color: '#edf1f7' } }
                    },
                    series: [{
                        type: 'line',
                        smooth: true,
                        data: currentPayload.value.scoreTrend.values,
                        lineStyle: { width: 3, color: '#5c84f6' },
                        areaStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: '#5c84f655' },
                                { offset: 1, color: '#5c84f608' }
                            ])
                        }
                    }]
                });
            }

            function renderAlertChart() {
                var chartEl = document.getElementById('alertLevelChart');
                if (!chartEl) {
                    return;
                }
                charts.alertLevel = echarts.init(chartEl);
                charts.alertLevel.setOption({
                    tooltip: { trigger: 'item' },
                    legend: { bottom: 0 },
                    series: [{
                        type: 'pie',
                        radius: ['42%', '70%'],
                        data: (currentPayload.value.levelDistribution || []).map(function (item, index) {
                            var palette = ['#ff7b75', '#ffb255', '#5c84f6', '#33c48d'];
                            return {
                                value: item.value,
                                name: item.name,
                                itemStyle: { color: palette[index % palette.length] }
                            };
                        })
                    }]
                });
            }

            function executeQuery() {
                if (!queryForm.value.sql) {
                    queryMessage.value = '请输入查询语句。';
                    return;
                }
                queryLoading.value = true;
                queryMessage.value = '';
                requestJson('/api/management/database/query', {
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

            function statusClass(status) {
                if (status === '已完成' || status === '已恢复') {
                    return 'tag-success';
                }
                if (status === '处理中' || status === '风险中') {
                    return 'tag-danger';
                }
                return 'tag-warning';
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
                if (status === 'ONLINE') {
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
                currentModuleCode: currentModuleCode,
                currentPages: currentPages,
                currentPage: currentPage,
                currentPageCode: currentPageCode,
                currentPayload: currentPayload,
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
                executeQuery: executeQuery,
                runSampleSql: runSampleSql,
                formatDelta: formatDelta,
                statusClass: statusClass,
                warningClass: warningClass,
                riskClass: riskClass,
                priorityClass: priorityClass,
                sourceClass: sourceClass,
                alertClass: alertClass
            };
        }
    }).mount('#app');
})();
