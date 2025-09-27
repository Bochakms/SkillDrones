package com.drones.skilldrones.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "report_log")
public class ReportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String reportType;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;

    @Column(columnDefinition = "JSONB")
    private String parameters;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private String filePath;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum ReportStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    @OneToMany(mappedBy = "reportLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReportFlight> reportFlights = new HashSet<>();

    public void addFlight(Flight flight) {
        ReportFlight reportFlight = new ReportFlight();
        reportFlight.setReportLog(this);
        reportFlight.setFlight(flight);
        this.reportFlights.add(reportFlight);
    }

    public ReportLog() {
        this.createdAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDate reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDate getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDate reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // Вспомогательные методы
    @PreUpdate
    public void onUpdate() {
        if (status == ReportStatus.COMPLETED || status == ReportStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
