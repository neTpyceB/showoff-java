package com.showoff.incidentops.springboot.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incident_tickets")
public class IncidentTicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 32)
    private String ticketId;

    @Column(name = "service_id", nullable = false, length = 64)
    private String serviceId;

    @Column(name = "severity", nullable = false)
    private int severity;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    protected IncidentTicketEntity() {}

    public IncidentTicketEntity(String ticketId, String serviceId, int severity, String summary, String status) {
        this.ticketId = ticketId;
        this.serviceId = serviceId;
        this.severity = severity;
        this.summary = summary;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
