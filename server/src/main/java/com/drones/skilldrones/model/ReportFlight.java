package com.drones.skilldrones.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_flights")
public class ReportFlight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportFlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private ReportLog reportLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Column(name = "included_at")
    private LocalDateTime includedAt = LocalDateTime.now();

    public Long getReportFlightId() {
        return reportFlightId;
    }

    public void setReportFlightId(Long reportFlightId) {
        this.reportFlightId = reportFlightId;
    }

    public ReportLog getReportLog() {
        return reportLog;
    }

    public void setReportLog(ReportLog reportLog) {
        this.reportLog = reportLog;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public LocalDateTime getIncludedAt() {
        return includedAt;
    }

    public void setIncludedAt(LocalDateTime includedAt) {
        this.includedAt = includedAt;
    }
}
