package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.response.FlightResponse;
import com.drones.skilldrones.dto.response.ReportResponse;
import com.drones.skilldrones.mapper.FlightMapper;
import com.drones.skilldrones.mapper.ReportFlightMapper;
import com.drones.skilldrones.mapper.ReportMapper;
import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.model.Region;
import com.drones.skilldrones.model.ReportFlight;
import com.drones.skilldrones.model.ReportLog;
import com.drones.skilldrones.repository.FlightRepository;
import com.drones.skilldrones.repository.ReportFlightRepository;
import com.drones.skilldrones.repository.ReportLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;


@Service
public class ReportServiceImpl implements ReportService {
    private final FlightRepository flightRepository;
    private final ReportLogRepository reportLogRepository;
    private final ReportFlightRepository reportFlightRepository;
    private final ReportMapper reportMapper;
    private final ReportFlightMapper reportFlightMapper;
    private final FlightMapper flightMapper;
    private final ObjectMapper objectMapper;

    public ReportServiceImpl(FlightRepository flightRepository,
                             ReportLogRepository reportLogRepository,
                             ReportFlightRepository reportFlightRepository,
                             ReportMapper reportMapper,
                             ReportFlightMapper flightProcessingMapper, FlightMapper flightMapper, // Существующий маппер
                             ObjectMapper objectMapper) {
        this.flightRepository = flightRepository;
        this.reportLogRepository = reportLogRepository;
        this.reportFlightRepository = reportFlightRepository;
        this.reportMapper = reportMapper;
        this.reportFlightMapper = flightProcessingMapper;
        this.flightMapper = flightMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public File generateFlightsChart(LocalDate startDate, LocalDate endDate, String chartType) {
        ReportLog reportLog = createReportLog("CHART", startDate, endDate,
                Map.of("chartType", chartType));

        try {
            List<Flight> flights = flightRepository.findByFlightDateBetween(startDate, endDate);

            DefaultCategoryDataset dataset = createDataset(flights);
            JFreeChart chart = createChart(dataset, chartType, flights);

            File chartFile = File.createTempFile("chart", ".png");
            BufferedImage image = chart.createBufferedImage(800, 600);
            ImageIO.write(image, "png", chartFile);

            // Обновляем отчет
            reportLog.setFilePath(chartFile.getAbsolutePath());
            reportLog.setStatus(ReportLog.ReportStatus.COMPLETED);
            reportLogRepository.save(reportLog);

            return chartFile;
        } catch (Exception e) {
            // Сохраняем ошибку
            reportLog.setStatus(ReportLog.ReportStatus.FAILED);
            reportLog.setErrorMessage("Ошибка генерации графика: " + e.getMessage());
            reportLogRepository.save(reportLog);
            throw new RuntimeException("Ошибка генерации графика", e);
        }
    }

    @Override
    public String generateRegionalReport(LocalDate startDate, LocalDate endDate) {
        ReportLog reportLog = createReportLog("REGIONAL", startDate, endDate, null);

        try {
            List<Flight> flights = flightRepository.findByFlightDateBetween(startDate, endDate);

            Map<String, Long> regionalStats = flights.stream()
                    .filter(flight -> flight.getDepartureRegion() != null)
                    .collect(Collectors.groupingBy(
                            flight -> flight.getDepartureRegion().getName(),
                            Collectors.counting()
                    ));

            Map<String, Object> reportData = new LinkedHashMap<>();
            reportData.put("periodStart", startDate.toString());
            reportData.put("periodEnd", endDate.toString());
            reportData.put("totalFlights", flights.size());
            reportData.put("regionalDistribution", regionalStats);

            List<Map<String, Object>> topRegions = regionalStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> regionInfo = new HashMap<>();
                        regionInfo.put("region", entry.getKey());
                        regionInfo.put("flightCount", entry.getValue());
                        return regionInfo;
                    })
                    .collect(Collectors.toList());

            reportData.put("top10Regions", topRegions);

            String jsonReport = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportData);

            // Сохраняем параметры отчета
            reportLog.setParameters(jsonReport);
            reportLog.setStatus(ReportLog.ReportStatus.COMPLETED);
            reportLogRepository.save(reportLog);

            return jsonReport;

        } catch (Exception e) {
            reportLog.setStatus(ReportLog.ReportStatus.FAILED);
            reportLog.setErrorMessage("Ошибка генерации регионального отчета: " + e.getMessage());
            reportLogRepository.save(reportLog);
            throw new RuntimeException("Ошибка генерации регионального отчета", e);
        }
    }

    @Override
    public Map<String, Object> generateComprehensiveReport(LocalDate startDate, LocalDate endDate) {
        ReportLog reportLog = createReportLog("COMPREHENSIVE", startDate, endDate, null);
        Map<String, Object> comprehensiveReport = new LinkedHashMap<>();

        try {
            List<Flight> flights = flightRepository.findByFlightDateBetween(startDate, endDate);

            comprehensiveReport.put("reportType", "COMPREHENSIVE");
            comprehensiveReport.put("periodStart", startDate.toString());
            comprehensiveReport.put("periodEnd", endDate.toString());
            comprehensiveReport.put("totalFlights", flights.size());

            // Статистика по типам дронов
            Map<String, Long> droneTypeStats = flights.stream()
                    .filter(flight -> flight.getDroneType() != null)
                    .collect(Collectors.groupingBy(
                            Flight::getDroneType,
                            Collectors.counting()
                    ));
            comprehensiveReport.put("droneTypeDistribution", droneTypeStats);

            // Региональная статистика
            Map<String, Long> regionalStats = flights.stream()
                    .filter(flight -> flight.getDepartureRegion() != null)
                    .collect(Collectors.groupingBy(
                            flight -> flight.getDepartureRegion().getName(),
                            Collectors.counting()
                    ));
            comprehensiveReport.put("regionalDistribution", regionalStats);

            // Ежедневная статистика
            Map<String, Long> dailyStats = flights.stream()
                    .collect(Collectors.groupingBy(
                            flight -> flight.getFlightDate().toString(),
                            Collectors.counting()
                    ));
            comprehensiveReport.put("dailyFlights", dailyStats);

            // Генерация графика
            File chartFile = generateFlightsChart(startDate, endDate, "bar");
            comprehensiveReport.put("chartFilePath", chartFile.getAbsolutePath());

            // Сохраняем параметры отчета
            reportLog.setParameters(objectMapper.writeValueAsString(comprehensiveReport));
            reportLog.setStatus(ReportLog.ReportStatus.COMPLETED);
            reportLogRepository.save(reportLog);

        } catch (Exception e) {
            comprehensiveReport.put("error", "Ошибка генерации отчета: " + e.getMessage());
            reportLog.setStatus(ReportLog.ReportStatus.FAILED);
            reportLog.setErrorMessage(e.getMessage());
            reportLogRepository.save(reportLog);
        }

        return comprehensiveReport;
    }

    @Override
    public List<ReportResponse> getReportHistory() {
        List<ReportLog> reports = reportLogRepository.findAllByOrderByCreatedAtDesc();
        return reportMapper.toResponseList(reports);
    }

    @Override
    public Optional<ReportResponse> getReportById(Long reportId) {
        return reportLogRepository.findById(reportId)
                .map(reportMapper::toResponse);
    }

    @Override
    public List<ReportResponse> getReportsByType(String reportType) {
        List<ReportLog> reports = reportLogRepository.findByReportTypeOrderByCreatedAtDesc(reportType);
        return reportMapper.toResponseList(reports);
    }

    @Override
    public List<ReportResponse> getCompletedReports() {
        List<ReportLog> reports = reportLogRepository.findCompletedReports();
        return reportMapper.toResponseList(reports);
    }

    @Override
    public ReportStatistics getReportStatistics() {
        long total = reportLogRepository.count();
        long completed = reportLogRepository.countByStatus(ReportLog.ReportStatus.COMPLETED);
        long failed = reportLogRepository.countByStatus(ReportLog.ReportStatus.FAILED);
        long pending = reportLogRepository.countByStatus(ReportLog.ReportStatus.PENDING);

        return new ReportStatistics(total, completed, failed, pending);
    }

    @Override
    public List<FlightResponse> getFlightsByReportId(Long reportId) {
        List<ReportFlight> reportFlights = reportFlightRepository.findByReportLog_ReportId(reportId);

        List<Flight> flights = reportFlights.stream()
                .map(ReportFlight::getFlight)
                .collect(Collectors.toList());

        return flightMapper.toFlightResponseList(flights);
    }

    @Override
    public Map<String, Object> generateTopRegionsReport(LocalDate startDate, LocalDate endDate) {
        ReportLog reportLog = createReportLog("TOP_REGIONS", startDate, endDate, null);
        Map<String, Object> report = new LinkedHashMap<>();

        try {
            List<Flight> flights = flightRepository.findByFlightDateBetween(startDate, endDate);

            // Группируем полеты по регионам и считаем плотность полетов
            List<Map<String, Object>> topRegions = flights.stream()
                    .filter(flight -> flight.getDepartureRegion() != null)
                    .collect(Collectors.groupingBy(
                            Flight::getDepartureRegion,
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .sorted(Map.Entry.<Region, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Region region = entry.getKey();
                        long flightCount = entry.getValue();
                        double density = region.getAreaKm2() != null ?
                                (double) flightCount / region.getAreaKm2() * 1000 : 0;

                        Map<String, Object> regionInfo = new HashMap<>();
                        regionInfo.put("regionName", region.getName());
                        regionInfo.put("flightCount", flightCount);
                        regionInfo.put("areaKm2", region.getAreaKm2());
                        regionInfo.put("flightDensity", Math.round(density * 100) / 100.0);
                        regionInfo.put("regionId", region.getRegionId());

                        return regionInfo;
                    })
                    .collect(Collectors.toList());

            report.put("reportType", "TOP_REGIONS");
            report.put("periodStart", startDate.toString());
            report.put("periodEnd", endDate.toString());
            report.put("totalFlightsAnalyzed", flights.size());
            report.put("topRegions", topRegions);
            report.put("totalRegionsWithFlights",
                    flights.stream().filter(f -> f.getDepartureRegion() != null)
                            .map(f -> f.getDepartureRegion().getRegionId())
                            .distinct().count());

            // Сохраняем отчет
            reportLog.setParameters(objectMapper.writeValueAsString(report));
            reportLog.setStatus(ReportLog.ReportStatus.COMPLETED);
            reportLogRepository.save(reportLog);

        } catch (Exception e) {
            report.put("error", "Ошибка генерации отчета по топ регионам: " + e.getMessage());
            reportLog.setStatus(ReportLog.ReportStatus.FAILED);
            reportLog.setErrorMessage(e.getMessage());
            reportLogRepository.save(reportLog);
        }

        return report;
    }

    // Вспомогательные методы
    private ReportLog createReportLog(String reportType, LocalDate startDate, LocalDate endDate,
                                      Map<String, Object> parameters) {
        ReportLog reportLog = new ReportLog();
        reportLog.setReportType(reportType);
        reportLog.setReportPeriodStart(startDate);
        reportLog.setReportPeriodEnd(endDate);
        reportLog.setStatus(ReportLog.ReportStatus.PROCESSING);

        try {
            if (parameters != null) {
                reportLog.setParameters(objectMapper.writeValueAsString(parameters));
            }
        } catch (Exception e) {
            // Игнорируем ошибки сериализации параметров
        }

        return reportLogRepository.save(reportLog);
    }

    // Методы для создания графиков (без изменений)
    private DefaultCategoryDataset createDataset(List<Flight> flights) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        flights.stream()
                .collect(Collectors.groupingBy(
                        Flight::getFlightDate,
                        Collectors.counting()
                ))
                .forEach((date, count) -> dataset.addValue(count, "Полеты", date.toString()));
        return dataset;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset, String chartType, List<Flight> flights) {
        return switch (chartType.toLowerCase()) {
            case "line" -> ChartFactory.createLineChart(
                    "Статистика полетов по дням", "Дата", "Количество полетов",
                    dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, true, false);
            case "pie" -> ChartFactory.createPieChart(
                    "Распределение полетов по регионам", createPieDataset(flights),
                    true, true, false);
            default -> ChartFactory.createBarChart(
                    "Статистика полетов по дням", "Дата", "Количество полетов",
                    dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, true, false);
        };
    }

    private DefaultPieDataset createPieDataset(List<Flight> flights) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        flights.stream()
                .filter(flight -> flight.getDepartureRegion() != null)
                .collect(Collectors.groupingBy(
                        flight -> flight.getDepartureRegion().getName(),
                        Collectors.counting()
                ))
                .forEach(dataset::setValue);
        return dataset;
    }

    // DTO для статистики отчетов
    public static class ReportStatistics {
        private final long totalReports;
        private final long completedReports;
        private final long failedReports;
        private final long pendingReports;
        private final double successRate;

        public ReportStatistics(long total, long completed, long failed, long pending) {
            this.totalReports = total;
            this.completedReports = completed;
            this.failedReports = failed;
            this.pendingReports = pending;
            this.successRate = total > 0 ? (double) completed / total * 100 : 0;
        }

        public long getTotalReports() { return totalReports; }
        public long getCompletedReports() { return completedReports; }
        public long getFailedReports() { return failedReports; }
        public long getPendingReports() { return pendingReports; }
        public double getSuccessRate() { return successRate; }
    }
}
