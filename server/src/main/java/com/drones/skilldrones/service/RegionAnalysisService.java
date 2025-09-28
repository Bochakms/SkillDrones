package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.dto.response.RegionResponse;
import com.drones.skilldrones.model.Region;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public interface RegionAnalysisService {
    /**
     * Определяет регион для полета по координатам
     */
    Optional<Region> findRegionForCoordinates(String coordinates);

    /**
     * Анализирует список полетов и возвращает топ-10 регионов
     */
    Map<String, Object> analyzeTopRegions(List<ParsedFlightData> flightData);

    /**
     * Анализирует топ регионов за определенный период
     */
    Map<String, Object> analyzeTopRegionsByPeriod(List<ParsedFlightData> flightData,
                                                  LocalDate startDate,
                                                  LocalDate endDate);

    /**
     * Возвращает простой список топ регионов (только названия и количество)
     */
    List<String> getSimpleTopRegions(List<ParsedFlightData> flightData);

    List<RegionResponse> getAllRegions();

    Optional<RegionResponse> getRegionById(Long regionId);

    void saveRegions(List<Region> regions);
}
