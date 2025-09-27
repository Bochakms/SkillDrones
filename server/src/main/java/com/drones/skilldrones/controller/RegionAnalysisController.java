package com.drones.skilldrones.controller;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.dto.response.RegionResponse;
import com.drones.skilldrones.service.FileParserService;
import com.drones.skilldrones.service.RegionAnalysisService;
import com.drones.skilldrones.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/analysis")
@Tag(name = "Анализ регионов", description = "API для анализа топ регионов по полетам БПЛА")
public class RegionAnalysisController {

    private final FileParserService fileParserService;
    private final RegionAnalysisService regionAnalysisService;
    private final ReportService reportService;

    public RegionAnalysisController(FileParserService fileParserService,
                                    RegionAnalysisService regionAnalysisService, ReportService reportService) {
        this.fileParserService = fileParserService;
        this.regionAnalysisService = regionAnalysisService;
        this.reportService = reportService;
    }

    @Operation(summary = "Получить топ регионов из базы данных",
            description = "Возвращает топ-10 регионов по количеству полетов за указанный период")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
            @ApiResponse(responseCode = "400", description = "Ошибка получения данных")
    })
    @GetMapping("/top-regions")  // Изменили с POST на GET
    public ResponseEntity<Map<String, Object>> getTopRegionsFromDatabase(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Используем существующий сервис для получения данных из БД
            Map<String, Object> result = reportService.generateTopRegionsReport(startDate, endDate);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка получения топ регионов: " + e.getMessage()));
        }
    }

    @Operation(summary = "Анализ за период",
            description = "Анализирует топ регионов за указанный временной период")
    @PostMapping("/top-regions/period")
    public ResponseEntity<Map<String, Object>> analyzeTopRegionsForPeriod(
            @Parameter(description = "Excel файл с данными полетов", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Дата начала периода (формат: YYYY-MM-DD)", required = true,
                    example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (формат: YYYY-MM-DD)", required = true,
                    example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<ParsedFlightData> flightData = fileParserService.parseFlightData(file);
            Map<String, Object> result = regionAnalysisService.analyzeTopRegionsByPeriod(
                    flightData, startDate, endDate);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка анализа: " + e.getMessage()));
        }
    }

    @Operation(summary = "Упрощенный анализ",
            description = "Возвращает упрощенный список топ регионов (только названия и количество)")
    @PostMapping("/top-regions/simple")
    public ResponseEntity<List<String>> getSimpleTopRegions(
            @Parameter(description = "Excel файл с данными полетов", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            List<ParsedFlightData> flightData = fileParserService.parseFlightData(file);
            List<String> result = regionAnalysisService.getSimpleTopRegions(flightData);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(List.of("Ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить все регионы", description = "Возвращает список всех регионов")
    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        try {
            List<RegionResponse> regions = regionAnalysisService.getAllRegions();
            return ResponseEntity.ok(regions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @Operation(summary = "Получить регион по ID", description = "Возвращает информацию о регионе по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Регион найден"),
            @ApiResponse(responseCode = "404", description = "Регион не найден")
    })
    @GetMapping("/regions/{regionId}")
    public ResponseEntity<RegionResponse> getRegionById(
            @Parameter(description = "ID региона", required = true, example = "1")
            @PathVariable Long regionId) {

        try {
            Optional<RegionResponse> region = regionAnalysisService.getRegionById(regionId);
            return region.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
