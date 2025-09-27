package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.response.FlightResponse;
import com.drones.skilldrones.dto.response.ReportResponse;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReportService {
    File generateFlightsChart(LocalDate startDate, LocalDate endDate, String chartType);
    String generateRegionalReport(LocalDate startDate, LocalDate endDate);
    Map<String, Object> generateComprehensiveReport(LocalDate startDate, LocalDate endDate);

    // Новые методы для работы с историей отчетов
    List<ReportResponse> getReportHistory();
    Optional<ReportResponse> getReportById(Long reportId);
    List<ReportResponse> getReportsByType(String reportType);
    List<ReportResponse> getCompletedReports();
    ReportServiceImpl.ReportStatistics getReportStatistics();
    Map<String, Object> generateTopRegionsReport(LocalDate startDate, LocalDate endDate);
    List<FlightResponse> getFlightsByReportId(Long reportId);
}
