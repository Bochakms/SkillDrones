package com.drones.skilldrones.controller;

import com.drones.skilldrones.dto.response.FlightResponse;
import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.model.RawTelegram;
import com.drones.skilldrones.service.FileParserService;
import com.drones.skilldrones.service.FlightProcessingService;
import com.drones.skilldrones.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/processing")
@Tag(name = "Обработка полетов", description = "API для обработки и преобразования данных о полетах БПЛА")
public class FlightProcessingController {
    private final FileParserService fileParserService;
    private final FlightProcessingService flightProcessingService;
    private final FlightService<Flight> flightService;

    public FlightProcessingController(FileParserService fileParserService,
                                      FlightProcessingService flightProcessingService, FlightService<Flight> flightService) {
        this.fileParserService = fileParserService;
        this.flightProcessingService = flightProcessingService;
        this.flightService = flightService;
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
            List<RawTelegram> telegrams = fileParserService.parseExcelFile(file);

            int processedCount = flightProcessingService.processBatch(telegrams);

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

   @Operation(
            summary = "Получить все рейсы",
            description = "Возвращает список всех обработанных рейсов с пагинацией"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рейсы успешно получены",
                    content = @Content(schema = @Schema(implementation = FlightResponse.class))
            )
    })
    @GetMapping("/flights")
    public ResponseEntity<Page<Flight>> getAllFlights(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле для сортировки", example = "flightDate")
            @RequestParam(defaultValue = "flightDate") String sortBy,

            @Parameter(description = "Направление сортировки", example = "desc")
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort sort = direction.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Flight> flights = flightService.getAllFlights(pageable);
            return ResponseEntity.ok(flights);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Получить рейс по ID",
            description = "Возвращает информацию о конкретном рейсе по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рейс найден",
                    content = @Content(schema = @Schema(implementation = Flight.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Рейс не найден"
            )
    })
    @GetMapping("/flights/{flightId}")
    public ResponseEntity<Flight> getFlightById(
            @Parameter(description = "ID рейса", required = true, example = "1")
            @PathVariable Long flightId) {

        try {
            Optional<Flight> flight = flightService.getFlightById(flightId);
            return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Поиск рейсов по дате",
            description = "Возвращает рейсы за указанный период"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рейсы успешно получены"
            )
    })
    @GetMapping("/flights/by-date")
    public ResponseEntity<Page<Flight>> getFlightsByDateRange(
            @Parameter(description = "Дата начала периода (YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Дата окончания периода (YYYY-MM-DD)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("flightDate").descending());
            Page<Flight> flights = flightService.getFlightsByDateRange(startDate, endDate, pageable);
            return ResponseEntity.ok(flights);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Поиск рейсов по типу дрона",
            description = "Возвращает рейсы для указанного типа дрона"
    )
    @GetMapping("/flights/by-drone-type")
    public ResponseEntity<Page<Flight>> getFlightsByDroneType(
            @Parameter(description = "Тип дрона", required = true, example = "BLA")
            @RequestParam String droneType,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("flightDate").descending());
            Page<Flight> flights = flightService.getFlightsByDroneType(droneType, pageable);
            return ResponseEntity.ok(flights);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Статистика по рейсам",
            description = "Возвращает общую статистику по всем рейсам"
    )
    @GetMapping("/flights/stats")
    public ResponseEntity<Map<String, Object>> getFlightsStats() {
        try {
            Map<String, Object> stats = flightService.getFlightsStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка получения статистики: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Удалить рейс",
            description = "Удаляет рейс по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рейс успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Рейс не найден"
            )
    })
    @DeleteMapping("/flights/{flightId}")
    public ResponseEntity<Map<String, String>> deleteFlight(
            @Parameter(description = "ID рейса", required = true, example = "1")
            @PathVariable Long flightId) {

        try {
            boolean deleted = flightService.deleteFlight(flightId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Рейс успешно удален"));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка удаления рейса: " + e.getMessage()));
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