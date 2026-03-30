package com.barterbay.barterbay.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "userreports")
public class Report {

    @Id
    @JsonProperty("_id")
    private String id;

    private String description;
    private String reason;

    @Field("reportedBy")
    private String reportedBy;

    @Field("reportedUser")
    private String reportedUser;

    private String status;

    public Report() {
        /* Default constructor for Spring Data mapping. */
    }

    public Report(String id, String description, String reason, String reportedBy, String reportedUser, String status) {
        this.id = id;
        this.description = description;
        this.reason = reason;
        this.reportedBy = reportedBy;
        this.reportedUser = reportedUser;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(String reportedUser) {
        this.reportedUser = reportedUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}