package com.drones.skilldrones.controller;
import com.drones.skilldrones.dto.response.FlightResponse;
import com.drones.skilldrones.dto.response.ReportResponse;
import com.drones.skilldrones.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
@Tag(name = "Отчеты и аналитика", description = "API для генерации отчетов и аналитики по полетам БПЛА")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
            summary = "Региональный отчет",
            description = "Генерирует JSON отчет по распределению полетов по регионам за указанный период"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчет успешно сгенерирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка генерации отчета"
            )
    })
    @GetMapping("/regional")
    public ResponseEntity<String> getRegionalReport(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            String jsonReport = reportService.generateRegionalReport(startDate, endDate);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonReport);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Operation(
            summary = "Комплексный отчет",
            description = "Генерирует комплексный отчет с различными метриками и статистикой полетов"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчет успешно сгенерирован",
                    content = @Content(schema = @Schema(implementation = ComprehensiveReport.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка генерации отчета"
            )
    })
    @GetMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveReport(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> report = reportService.generateComprehensiveReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Отчет по топ регионам",
            description = "Генерирует отчет с топ-10 регионов по количеству полетов"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчет успешно сгенерирован",
                    content = @Content(schema = @Schema(implementation = TopRegionsReport.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка генерации отчета"
            )
    })
    @GetMapping("/top-regions")
    public ResponseEntity<Map<String, Object>> getTopRegionsReport(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> report = reportService.generateTopRegionsReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "График полетов",
            description = "Генерирует график в формате PNG с визуализацией статистики полетов"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "График успешно сгенерирован",
                    content = @Content(mediaType = "image/png")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка генерации графика"
            )
    })
    @GetMapping("/chart")
    public ResponseEntity<FileSystemResource> getChart(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Тип графика", required = false, example = "bar",
                    schema = @Schema(allowableValues = {"bar", "line", "pie"}))
            @RequestParam(defaultValue = "bar") String chartType) {

        try {
            File chartFile = reportService.generateFlightsChart(startDate, endDate, chartType);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"flight_chart.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new FileSystemResource(chartFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "История отчетов",
            description = "Возвращает историю всех сгенерированных отчетов"
    )
    @ApiResponse(
            responseCode = "200",
            description = "История отчетов получена",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportResponse.class)))
    )
    @GetMapping("/history")
    public ResponseEntity<List<ReportResponse>> getReportHistory() {
        try {
            List<ReportResponse> reports = reportService.getReportHistory();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @Operation(
            summary = "Отчет по ID",
            description = "Возвращает информацию о конкретном отчете по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчет найден",
                    content = @Content(schema = @Schema(implementation = ReportResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отчет не найден"
            )
    })
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(
            @Parameter(description = "ID отчета", required = true, example = "1")
            @PathVariable Long reportId) {

        try {
            Optional<ReportResponse> report = reportService.getReportById(reportId);
            return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Отчеты по типу",
            description = "Возвращает список отчетов определенного типа"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Отчеты получены",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportResponse.class)))
    )
    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<ReportResponse>> getReportsByType(
            @Parameter(description = "Тип отчета", required = true, example = "REGIONAL",
                    schema = @Schema(allowableValues = {"REGIONAL", "COMPREHENSIVE", "TOP_REGIONS", "CHART"}))
            @PathVariable String reportType) {

        try {
            List<ReportResponse> reports = reportService.getReportsByType(reportType);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @Operation(
            summary = "Статистика отчетов",
            description = "Возвращает общую статистику по сгенерированным отчетам"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика получена",
                    content = @Content(schema = @Schema(implementation = ReportStatistics.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка получения статистики"
            )
    })
    @GetMapping("/statistics")
    public ResponseEntity<?> getReportStatistics() {
        try {
            var stats = reportService.getReportStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка получения статистики: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Полеты отчета",
            description = "Возвращает список всех полетов, включенных в конкретный отчет"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Полеты отчета получены",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlightResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отчет не найден"
            )
    })
    @GetMapping("/{reportId}/flights")
    public ResponseEntity<List<FlightResponse>> getReportFlights(
            @Parameter(description = "ID отчета", required = true, example = "1")
            @PathVariable Long reportId) {

        try {
            List<FlightResponse> flights = reportService.getFlightsByReportId(reportId);
            return ResponseEntity.ok(flights);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Схемы для Swagger документации
    @Schema(description = "Комплексный отчет")
    public static class ComprehensiveReport {
        @Schema(description = "Тип отчета", example = "COMPREHENSIVE")
        public String reportType;

        @Schema(description = "Общее количество полетов", example = "145")
        public Integer totalFlights;

        @Schema(description = "Распределение по типам дронов")
        public Map<String, Long> droneTypeDistribution;

        @Schema(description = "Распределение по регионам")
        public Map<String, Long> regionalDistribution;

        @Schema(description = "Путь к файлу графика", example = "/tmp/chart_123.png")
        public String chartFilePath;
    }

    @Schema(description = "Отчет по топ регионам")
    public static class TopRegionsReport {
        @Schema(description = "Тип отчета", example = "TOP_REGIONS")
        public String reportType;

        @Schema(description = "Топ-10 регионов")
        public List<RegionStats> topRegions;

        @Schema(description = "Всего регионов с полетами", example = "45")
        public Long totalRegionsWithFlights;
    }

    @Schema(description = "Статистика региона")
    public static class RegionStats {
        @Schema(description = "Название региона", example = "Московская область")
        public String regionName;

        @Schema(description = "Количество полетов", example = "42")
        public Long flightCount;

        @Schema(description = "Плотность полетов (на 1000 км²)", example = "0.95")
        public Double flightDensity;
    }

    @Schema(description = "Статистика отчетов")
    public static class ReportStatistics {
        @Schema(description = "Всего отчетов", example = "100")
        public Long totalReports;

        @Schema(description = "Завершено успешно", example = "85")
        public Long completedReports;

        @Schema(description = "С ошибками", example = "10")
        public Long failedReports;

        @Schema(description = "В обработке", example = "5")
        public Long pendingReports;

        @Schema(description = "Процент успеха", example = "85.0")
        public Double successRate;
    }
}
