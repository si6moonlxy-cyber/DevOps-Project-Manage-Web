package com.graduate.delivery.entity;

import java.sql.Timestamp;

public class Project {

    private Long id;
    private String projectCode;
    private String projectName;
    private Long teamId;
    private String teamName;
    private String managerName;
    private String businessDomain;
    private String status;
    private String deliveryStage;
    private Integer progress;
    private String riskLevel;
    private String currentVersion;
    private Timestamp planGoLive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getBusinessDomain() {
        return businessDomain;
    }

    public void setBusinessDomain(String businessDomain) {
        this.businessDomain = businessDomain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryStage() {
        return deliveryStage;
    }

    public void setDeliveryStage(String deliveryStage) {
        this.deliveryStage = deliveryStage;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Timestamp getPlanGoLive() {
        return planGoLive;
    }

    public void setPlanGoLive(Timestamp planGoLive) {
        this.planGoLive = planGoLive;
    }
}
