package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.dto.response.RegionResponse;
import com.drones.skilldrones.mapper.RegionMapper;
import com.drones.skilldrones.model.Region;
import com.drones.skilldrones.repository.RegionRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionAnalysisServiceImpl implements RegionAnalysisService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;
    private final GeometryFactory geometryFactory;

    public RegionAnalysisServiceImpl(RegionRepository regionRepository,
                                     RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionMapper = regionMapper;
        this.geometryFactory = new GeometryFactory();
    }

    private static final Logger log = LoggerFactory.getLogger(RegionAnalysisServiceImpl.class);

    @Override
    public Optional<Region> findRegionForCoordinates(String coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return Optional.empty();
        }

        try {
            Point point = createPointFromCoordinates(coordinates);
            return regionRepository.findRegionByPoint(point);
        } catch (Exception e) {
            System.err.println("Ошибка определения региона для координат: " + coordinates);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, Object> analyzeTopRegions(List<ParsedFlightData> flightData) {
        Map<Region, Integer> regionStats = new HashMap<>();
        int totalProcessed = 0;
        int failedGeolocation = 0;

        for (ParsedFlightData flight : flightData) {
            Optional<Region> regionOpt = findRegionForCoordinates(flight.getCoordinates());

            if (regionOpt.isPresent()) {
                Region region = regionOpt.get();
                regionStats.put(region, regionStats.getOrDefault(region, 0) + 1);
                totalProcessed++;
            } else {
                failedGeolocation++;
            }
        }

        List<Map.Entry<Region, Integer>> sortedRegions = regionStats.entrySet()
                .stream()
                .sorted(Map.Entry.<Region, Integer>comparingByValue().reversed())
                .limit(10)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFlightsProcessed", totalProcessed);
        result.put("failedGeolocation", failedGeolocation);
        result.put("analysisDate", new Date());

        // Используем RegionMapper для преобразования регионов в DTO
        List<Map<String, Object>> topRegions = new ArrayList<>();
        int rank = 1;

        for (Map.Entry<Region, Integer> entry : sortedRegions) {
            Map<String, Object> regionInfo = new LinkedHashMap<>();
            regionInfo.put("rank", rank++);
            regionInfo.put("flightCount", entry.getValue());

            // Маппим регион в DTO используя RegionMapper
            RegionResponse regionResponse = regionMapper.toResponse(entry.getKey());
            regionInfo.put("region", regionResponse);

            // Добавляем плотность полетов
            if (entry.getKey().getAreaKm2() != null) {
                double density = (double) entry.getValue() / entry.getKey().getAreaKm2() * 1000;
                regionInfo.put("flightDensity", Math.round(density * 100) / 100.0);
            }

            topRegions.add(regionInfo);
        }

        result.put("topRegions", topRegions);
        return result;
    }

    @Override
    public Map<String, Object> analyzeTopRegionsByPeriod(List<ParsedFlightData> flightData,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        List<ParsedFlightData> filteredFlights = flightData.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .filter(flight -> !flight.getFlightDate().isBefore(startDate))
                .filter(flight -> !flight.getFlightDate().isAfter(endDate))
                .collect(Collectors.toList());

        Map<String, Object> result = analyzeTopRegions(filteredFlights);
        result.put("periodStart", startDate);
        result.put("periodEnd", endDate);
        result.put("periodFlightsCount", filteredFlights.size());

        return result;
    }

    @Override
    public List<String> getSimpleTopRegions(List<ParsedFlightData> flightData) {
        Map<String, Object> analysisResult = analyzeTopRegions(flightData);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topRegions =
                (List<Map<String, Object>>) analysisResult.get("topRegions");

        return topRegions.stream()
                .map(region -> {
                    @SuppressWarnings("unchecked")
                    RegionResponse regionResponse = (RegionResponse) region.get("region");
                    int flightCount = (Integer) region.get("flightCount");
                    return String.format("%d. %s (%d полетов)",
                            region.get("rank"), regionResponse.name(), flightCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RegionResponse> getAllRegions() {
        List<Region> regions = regionRepository.findAll();
        return regionMapper.toResponseList(regions);
    }

    @Override
    public Optional<RegionResponse> getRegionById(Long regionId) {
        return regionRepository.findById(regionId)
                .map(regionMapper::toResponse);
    }

    @Override
    public void saveRegions(List<Region> regions) {
        log.info("Начало сохранения {} регионов в базу данных", regions.size());

        try {
            if (regions.isEmpty()) {
                log.warn("Передан пустой список регионов для сохранения");
                return;
            }

            int successCount = 0;
            int mergedCount = 0;

            for (Region region : regions) {
                try {
                    // Используем merge вместо save/saveAll
                    Region mergedRegion = regionRepository.save(region);
                    mergedCount++;
                } catch (Exception e) {
                    log.warn("Ошибка при сохранении региона '{}': {}", region.getName(), e.getMessage());
                    // Пробуем сохранить через новый EntityManager
                    try {
                        Region savedRegion = regionRepository.save(region);
                        successCount++;
                    } catch (Exception ex) {
                        log.error("Не удалось сохранить регион '{}' даже после повторной попытки", region.getName());
                    }
                }
            }

            log.info("Сохранение завершено. Успешно: {}, Обновлено: {}", successCount, mergedCount);

        } catch (Exception e) {
            log.error("Ошибка при сохранении регионов в базу данных", e);
            throw new RuntimeException("Ошибка сохранения регионов: " + e.getMessage(), e);
        }
    }

    public long getRegionsCount() {
        return regionRepository.count();

    }

    private Point createPointFromCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());

        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

}
