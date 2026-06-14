package com.graduate.delivery.entity;

public class SessionUser {

    private Long id;
    private String username;
    private String displayName;
    private String roleCode;
    private String roleName;
    private Long teamId;

    public static SessionUser fromAccount(AdminAccount account) {
        SessionUser user = new SessionUser();
        user.setId(account.getId());
        user.setUsername(account.getUsername());
        user.setDisplayName(account.getDisplayName());
        user.setRoleCode(account.getRoleCode());
        user.setRoleName(account.getRoleName());
        user.setTeamId(account.getTeamId());
        return user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
