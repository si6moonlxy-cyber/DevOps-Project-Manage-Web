package com.graduate.metrics.service;

import com.graduate.metrics.mapper.MetricsReportMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MetricsReportService {

    private final MetricsReportMapper mapper;

    public MetricsReportService(MetricsReportMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getMetricsOverviewPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildMetricCards());
        payload.put("metricTrend", buildMetricTrend());
        payload.put("metricCatalog", mapper.findMetricCards());
        payload.put("domainTips", buildDomainTips());
        return payload;
    }

    public Map<String, Object> getReportCenterPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildReportCards());
        payload.put("reports", mapper.findReports());
        payload.put("scoreTrend", buildScoreTrend());
        payload.put("metricAlerts", mapper.findMetricAlerts());
        return payload;
    }

    private List<Map<String, Object>> buildMetricCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : mapper.findMetricCards()) {
            Map<String, Object> card = new LinkedHashMap<String, Object>();
            card.put("title", row.get("metricName"));
            card.put("value", row.get("metricValue"));
            card.put("unit", row.get("unitName"));
            card.put("target", row.get("targetValue"));
            card.put("delta", row.get("trendRate"));
            card.put("tone", toneOf(String.valueOf(row.get("warningLevel"))));
            cards.add(card);
        }
        return cards;
    }

    private Map<String, Object> buildMetricTrend() {
        List<Map<String, Object>> rows = mapper.findMetricTrend();
        Set<String> labels = new LinkedHashSet<String>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("snapshotMonth")));
        }

        Map<String, List<Object>> grouped = new LinkedHashMap<String, List<Object>>();
        List<Map<String, Object>> cards = mapper.findMetricCards();
        for (Map<String, Object> row : cards) {
            grouped.put(String.valueOf(row.get("metricCode")), new ArrayList<Object>());
        }

        for (Map<String, Object> row : cards) {
            String code = String.valueOf(row.get("metricCode"));
            for (String label : labels) {
                grouped.get(code).add(findMetricValue(rows, code, label));
            }
        }

        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> definition : cards) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("code", definition.get("metricCode"));
            item.put("name", definition.get("metricName"));
            item.put("unit", definition.get("unitName"));
            item.put("data", grouped.get(String.valueOf(definition.get("metricCode"))));
            series.add(item);
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("labels", new ArrayList<String>(labels));
        payload.put("series", series);
        return payload;
    }

    private List<Map<String, Object>> buildReportCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("历史记录", String.valueOf(countReports()), "份", "报表历史直接读取 report_record 与 report_template。", "brand"));
        cards.add(createCard("核心指标", String.valueOf(mapper.findMetricCards().size()), "项", "围绕需求交付周期、部署频率、变更失败率和 MTTR 展示。", "accent"));
        cards.add(createCard("指标预警", String.valueOf(valueOf(mapper.countMetricAlerts())), "条", "预警记录用于辅助展示风险识别能力。", "warning"));
        return cards;
    }

    private Map<String, Object> buildScoreTrend() {
        List<Map<String, Object>> rows = mapper.findReportScoreTrend();
        List<String> labels = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("reportDay")));
            values.add(row.get("qualityScore"));
        }
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("labels", labels);
        payload.put("values", values);
        return payload;
    }

    private List<Map<String, Object>> buildDomainTips() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(createNote("指标口径", "核心指标定义来自 metric_definition，页面展示统一口径。"));
        rows.add(createNote("趋势结果", "趋势图读取 metric_result 的企业级统计结果，适合在答辩时解释计算结果与时间粒度。"));
        rows.add(createNote("报表口径", "报表页面仅展示历史留痕、评分趋势和关联预警，不讲成自动生成或导出系统。"));
        return rows;
    }

    private Object findMetricValue(List<Map<String, Object>> rows, String code, String label) {
        for (Map<String, Object> row : rows) {
            if (code.equals(String.valueOf(row.get("metricCode"))) && label.equals(String.valueOf(row.get("snapshotMonth")))) {
                return row.get("metricValue");
            }
        }
        return 0;
    }

    private int countReports() {
        Integer value = mapper.countReports();
        return value == null ? 0 : value.intValue();
    }

    private int valueOf(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private String toneOf(String level) {
        if ("CRITICAL".equalsIgnoreCase(level)) {
            return "danger";
        }
        if ("WARNING".equalsIgnoreCase(level)) {
            return "warning";
        }
        return "success";
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
}
