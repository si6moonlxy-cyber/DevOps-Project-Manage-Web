package com.graduate.project.service;

import com.graduate.project.mapper.ProjectDeliveryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectDeliveryService {

    private final ProjectDeliveryMapper mapper;

    public ProjectDeliveryService(ProjectDeliveryMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getProjectDeliveryPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildProjectCards());
        payload.put("projects", mapper.findProjectRows());
        payload.put("milestones", mapper.findMilestones());
        payload.put("recentActivities", mapper.findActivities());
        payload.put("statusDistribution", toDistribution(mapper.countProjectStatus()));
        return payload;
    }

    public Map<String, Object> getWorkItemPage() {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("summaryCards", buildWorkItemCards());
        payload.put("projects", mapper.findProjectRows());
        payload.put("items", mapper.findWorkItems());
        payload.put("statusDistribution", toDistribution(mapper.countWorkItemStatus()));
        return payload;
    }

    public Map<String, Object> createProject(Map<String, Object> request) {
        mapper.insertProject(request);
        return getProjectDeliveryPage();
    }

    public Map<String, Object> updateProject(Long projectId, Map<String, Object> request) {
        mapper.updateProject(projectId, request);
        return getProjectDeliveryPage();
    }

    public Map<String, Object> deleteProject(Long projectId) {
        mapper.deleteProject(projectId);
        return getProjectDeliveryPage();
    }

    public Map<String, Object> createWorkItem(Map<String, Object> request) {
        mapper.insertWorkItem(request);
        return getWorkItemPage();
    }

    public Map<String, Object> updateWorkItem(Long itemId, Map<String, Object> request) {
        mapper.updateWorkItem(itemId, request);
        return getWorkItemPage();
    }

    public Map<String, Object> deleteWorkItem(Long itemId) {
        mapper.deleteWorkItem(itemId);
        return getWorkItemPage();
    }

    private List<Map<String, Object>> buildProjectCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("项目总数", valueOf(mapper.countProjects()), "个", "项目交付域统一管理项目主数据", "brand"));
        cards.add(createCard("高风险项目", valueOf(mapper.countHighRiskProjects()), "个", "用于展示交付推进中的重点风险", "danger"));
        cards.add(createCard("已完成工作项", valueOf(mapper.countCompletedItems()), "条", "交付过程中的闭环事项数量", "success"));
        return cards;
    }

    private List<Map<String, Object>> buildWorkItemCards() {
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        cards.add(createCard("工作项总数", valueOf(mapper.countWorkItems()), "条", "用于补充说明项目执行过程", "brand"));
        cards.add(createCard("逾期待办", valueOf(mapper.countOverdueItems()), "条", "便于企业管理视角跟踪推进节奏", "warning"));
        cards.add(createCard("已完成", valueOf(mapper.countCompletedItems()), "条", "工作项清单用于展示执行闭环", "success"));
        return cards;
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

    private String valueOf(Integer value) {
        return value == null ? "0" : String.valueOf(value);
    }
}
