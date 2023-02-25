package com.group.projectAdmin.model;

@SuppressWarnings("ALL")
public class ModelGroups {

    String role, id, timestamp;

    public ModelGroups() {

    }

    public ModelGroups(String role, String id, String timestamp) {
        this.role = role;
        this.id = id;
        this.timestamp = timestamp;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
