package com.drones.skilldrones.service;

import com.drones.skilldrones.model.Region;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Service
public class GeoJsonService {
    private final RegionAnalysisService regionAnalysisService;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(GeoJsonService.class);

    public GeoJsonService(RegionAnalysisService regionAnalysisService) {
        this.regionAnalysisService = regionAnalysisService;
        this.objectMapper = new ObjectMapper();
    }

    public int loadRegionsFromGeoJson(MultipartFile geoJsonFile) {
        try {
            log.info("Начало загрузки GeoJSON файла: {}", geoJsonFile.getOriginalFilename());

            List<Region> regions = parseGeoJsonFile(geoJsonFile);
            log.info("Загружено {} регионов из GeoJSON", regions.size());

            regionAnalysisService.saveRegions(regions);

            return regions.size();

        } catch (Exception e) {
            log.error("Ошибка загрузки GeoJSON файла", e);
            throw new RuntimeException("Ошибка загрузки GeoJSON файла: " + e.getMessage(), e);
        }
    }

    public List<Region> parseGeoJsonFile(MultipartFile geoJsonFile) {
        List<Region> regions = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(geoJsonFile.getInputStream());

            // Проверяем, что это FeatureCollection
            if (!rootNode.has("type") || !"FeatureCollection".equals(rootNode.get("type").asText())) {
                throw new IllegalArgumentException("Файл должен быть GeoJSON FeatureCollection");
            }

            JsonNode features = rootNode.get("features");
            if (features == null || !features.isArray()) {
                throw new IllegalArgumentException("GeoJSON не содержит features array");
            }

            log.info("Найдено {} features в GeoJSON", features.size());

            GeoJsonReader geoJsonReader = new GeoJsonReader();
            WKTReader wktReader = new WKTReader();

            int processed = 0;
            int skipped = 0;

            for (JsonNode feature : features) {
                try {
                    Region region = parseFeature(feature, geoJsonReader, wktReader);
                    if (region != null) {
                        regions.add(region);
                        processed++;
                    } else {
                        skipped++;
                    }

                    // Логируем прогресс
                    if (processed % 100 == 0) {
                        log.info("Обработано {} регионов...", processed);
                    }

                } catch (Exception e) {
                    log.warn("Ошибка парсинга feature {}: {}", processed, e.getMessage());
                    skipped++;
                }
            }

            log.info("Парсинг GeoJSON завершен. Успешно: {}, Пропущено: {}", processed, skipped);

        } catch (Exception e) {
            log.error("Ошибка чтения GeoJSON файла", e);
            throw new RuntimeException("Ошибка парсинга GeoJSON: " + e.getMessage(), e);
        }

        return regions;
    }

    private Region parseFeature(JsonNode feature, GeoJsonReader geoJsonReader, WKTReader wktReader) {
        try {
            Region region = new Region();

            // Парсим свойства
            JsonNode properties = feature.get("properties");
            if (properties != null) {
                String name = extractRegionName(properties);
                region.setName(name);
            } else {
                region.setName("Unknown");
            }

            // Парсим геометрию
            JsonNode geometryNode = feature.get("geometry");
            if (geometryNode != null) {
                String geoJsonString = geometryNode.toString();
                Geometry geometry = geoJsonReader.read(geoJsonString);

                if (geometry != null) {
                    // Конвертируем в WKT для единообразия
                    String wkt = geometry.toText();
                    Geometry normalizedGeometry = wktReader.read(wkt);
                    region.setGeometry(normalizedGeometry);

                    // Рассчитываем площадь
                    double area = calculateAreaKm2(normalizedGeometry);
                    region.setAreaKm2(area);
                }
            }

            return region;

        } catch (Exception e) {
            log.warn("Ошибка парсинга feature: {}", e.getMessage());
            return null;
        }
    }

    private String extractRegionName(JsonNode properties) {
        // Пробуем разные возможные названия полей для имени региона
        String[] possibleNameFields = {"name", "NAME", "region", "REGION", "subject", "SUBJECT"};

        for (String field : possibleNameFields) {
            if (properties.has(field)) {
                String name = properties.get(field).asText();
                if (name != null && !name.trim().isEmpty()) {
                    return name.trim();
                }
            }
        }

        return "Unknown";
    }

    private double calculateAreaKm2(Geometry geometry) {
        try {
            // Для расчета площади в км² из координат WGS84
            // Это упрощенный расчет - для продакшена используйте более точные методы
            double area = geometry.getArea() * 111.32 * 111.32;
            return Math.abs(Math.round(area * 100) / 100.0);
        } catch (Exception e) {
            log.warn("Ошибка расчета площади: {}", e.getMessage());
            return 0.0;
        }
    }

    public void validateGeoJsonFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("GeoJSON файл не должен быть пустым");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".json") &&
                !filename.toLowerCase().endsWith(".geojson"))) {
            throw new IllegalArgumentException("Файл должен иметь расширение .json или .geojson");
        }

        // Быстрая проверка структуры
        try {
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());
            if (!rootNode.has("type") || !"FeatureCollection".equals(rootNode.get("type").asText())) {
                throw new IllegalArgumentException("Неверный формат GeoJSON: должен быть FeatureCollection");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный JSON формат: " + e.getMessage());
        }
    }
}

