package com.drones.skilldrones.controller;

import com.drones.skilldrones.model.RawTelegram;
import com.drones.skilldrones.service.FileParserService;
import com.drones.skilldrones.service.FlightProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/processing")
@Tag(name = "Обработка полетов", description = "API для обработки и преобразования данных о полетах БПЛА")
public class FlightProcessingController {
    private final FileParserService fileParserService;
    private final FlightProcessingService flightProcessingService;

    public FlightProcessingController(FileParserService fileParserService,
                                      FlightProcessingService flightProcessingService) {
        this.fileParserService = fileParserService;
        this.flightProcessingService = flightProcessingService;
    }

    @Operation(
            summary = "Обработка файла с полетами",
            description = "Загружает Excel файл с телеграммами полетов, парсит данные и сохраняет в базу данных"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Файл успешно обработан",
                    content = @Content(schema = @Schema(implementation = ProcessingResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка обработки файла",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(value = "/process-file", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> processFlightFile(
            @Parameter(
                    description = "Excel файл с данными полетов в формате телеграмм",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. Парсим Excel файл
            List<RawTelegram> telegrams = fileParserService.parseExcelFile(file);

            // 2. Обрабатываем телеграммы в полеты
            int processedCount = flightProcessingService.processBatch(telegrams);

            // 3. Возвращаем результат
            return ResponseEntity.ok(Map.of(
                    "message", "Файл успешно обработан",
                    "totalRecords", telegrams.size(),
                    "processedSuccessfully", processedCount,
                    "failed", telegrams.size() - processedCount,
                    "successRate", String.format("%.2f%%", (double) processedCount / telegrams.size() * 100)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Ошибка обработки файла",
                            "details", e.getMessage()
                    ));
        }
    }

    @Operation(
            summary = "Статистика обработки",
            description = "Возвращает статистику по обработанным полетам и телеграммам"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика получена",
                    content = @Content(schema = @Schema(implementation = ProcessingStats.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка получения статистики"
            )
    })
    @GetMapping("/stats")
    public ResponseEntity<?> getProcessingStats() {
        try {
            var stats = flightProcessingService.getProcessingStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка получения статистики: " + e.getMessage()));
        }
    }

    // Схемы для Swagger документации
    @Schema(description = "Результат обработки файла")
    public static class ProcessingResult {
        @Schema(description = "Сообщение о результате", example = "Файл успешно обработан")
        public String message;

        @Schema(description = "Общее количество записей", example = "150")
        public Integer totalRecords;

        @Schema(description = "Успешно обработано", example = "145")
        public Integer processedSuccessfully;

        @Schema(description = "Не удалось обработать", example = "5")
        public Integer failed;

        @Schema(description = "Процент успешной обработки", example = "96.67%")
        public String successRate;
    }

    @Schema(description = "Статистика обработки")
    public static class ProcessingStats {
        @Schema(description = "Всего обработано", example = "1000")
        public Integer totalProcessed;

        @Schema(description = "Успешно обработано", example = "950")
        public Integer successful;

        @Schema(description = "Ошибок обработки", example = "50")
        public Integer failed;

        @Schema(description = "Процент успеха", example = "95.0")
        public Double successRate;
    }

    @Schema(description = "Ответ с ошибкой")
    public static class ErrorResponse {
        @Schema(description = "Текст ошибки", example = "Ошибка обработки файла")
        public String error;

        @Schema(description = "Детали ошибки", example = "Неверный формат файла")
        public String details;
    }
}