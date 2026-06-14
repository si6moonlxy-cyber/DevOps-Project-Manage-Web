package com.graduate.devops.service;

import com.graduate.devops.mapper.DevopsDataMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DevopsDataService {

    private final DevopsDataMapper mapper;

    public DevopsDataService(DevopsDataMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getDevopsOverviewPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildSourceCards());
        payload.put("sources", mapper.findSources());
        payload.put("statusDistribution", toDistribution(mapper.countSourceStatus()));
        payload.put("domainTips", buildDomainTips());
        return payload;
    }

    public Map<String, Object> getCollectionPipelinePage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildPipelineCards());
        payload.put("tasks", mapper.findCollectionTasks());
        payload.put("pipelineSteps", buildPipelineSteps());
        payload.put("qualityChecks", mapper.findQualityChecks());
        return payload;
    }

    public Map<String, Object> updateSource(Long sourceId, Map<String, Object> request) {
        mapper.updateSource(sourceId, request);
        return getDevopsOverviewPage();
    }

    private List<Map<String, Object>> buildSourceCards() {
        int total = valueOf(mapper.countSources());
        int online = valueOf(mapper.countOnlineSources());
        int issueCount = valueOf(mapper.countOpenQualityIssues());
        int ruleCount = valueOf(mapper.countEnabledQualityRules());
        BigDecimal rate = total == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(online * 100.0 / total).setScale(1, RoundingMode.HALF_UP);

        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("接入数据源", String.valueOf(total), "个", "用于展示 Jira、GitLab、Jenkins 与监控平台的预留接入能力。", "brand"));
        cards.add(createCard("在线数据源", String.valueOf(online), "个", "来自演示库 data_source 的样例接入状态。", "success"));
        cards.add(createCard("展示在线率", String.valueOf(rate), "%", "根据样例状态统计当前可用接入占比。", "accent"));
        cards.add(createCard("待处理质量问题", String.valueOf(issueCount), "条", "结合 data_quality_rule 与 data_quality_log 展示采集质量概念。", ruleCount > 0 ? "warning" : "danger"));
        return cards;
    }

    private List<Map<String, Object>> buildPipelineCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("采集任务", String.valueOf(valueOf(mapper.countCollectionTasks())), "个", "采集链路读取 collect_job 样例任务定义。", "brand"));
        cards.add(createCard("数据源总数", String.valueOf(valueOf(mapper.countSources())), "个", "用于说明五个业务域共享演示数据。", "accent"));
        cards.add(createCard("质检规则", String.valueOf(valueOf(mapper.countEnabledQualityRules())), "条", "用于答辩展示数据质量巡检与问题闭环概念。", "success"));
        return cards;
    }

    private List<Map<String, Object>> buildDomainTips() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createNote("接入概况", "当前登记 " + valueOf(mapper.countSources()) + " 个样例数据源，用于说明项目交付主链路的数据来源。"));
        rows.add(createNote("采集质量", "已配置 " + valueOf(mapper.countEnabledQualityRules()) + " 条数据质量规则，用于展示字段完整性与状态一致性校验思路。"));
        rows.add(createNote("演示口径", "本阶段不宣称完成真实 API 联调，仅展示接入结构、同步状态与预留能力。"));
        return rows;
    }

    private List<Map<String, Object>> buildPipelineSteps() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createNote("需求与缺陷采集", "展示需求系统到 work_item 的数据流向。"));
        rows.add(createNote("代码与流水线采集", "展示代码平台和持续集成平台的数据流向。"));
        rows.add(createNote("监控与告警回流", "展示监控事件进入 alert_event 并关联项目链路的设计。"));
        rows.add(createNote("质量巡检", "展示 data_quality_rule / data_quality_log 对采集结果的校验思路。"));
        return rows;
    }

    private List<Map<String, Object>> toDistribution(List<Map<String, Object>> rows) {
        List<Map<String, Object>> distribution = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", row.get("itemName"));
            item.put("value", row.get("totalCount"));
            distribution.add(item);
        }
        return distribution;
    }

    private Map<String, Object> createCard(String title, String value, String unit, String description, String tone) {
        Map<String, Object> card = new LinkedHashMap<String, Object>();
        card.put("title", title);
        card.put("value", value);
        card.put("unit", unit);
        card.put("description", description);
        card.put("tone", tone);
        return card;
    }

    private Map<String, Object> createNote(String title, String description) {
        Map<String, Object> note = new LinkedHashMap<String, Object>();
        note.put("title", title);
        note.put("description", description);
        return note;
    }

    private int valueOf(Integer value) {
        return value == null ? 0 : value.intValue();
    }
}
