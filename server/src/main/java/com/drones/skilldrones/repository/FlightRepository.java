package com.drones.skilldrones.repository;

import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.model.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByFlightDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Flight> findByFlightDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT f FROM Flight f WHERE f.departureRegion = :region OR f.arrivalRegion = :region")
    List<Flight> findByRegion(@Param("region") Region region);

    @Query("SELECT COUNT(f) FROM Flight f WHERE f.flightDate BETWEEN :startDate AND :endDate")
    Long countFlightsInPeriod(@Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    @Query("SELECT f.droneType, COUNT(f) FROM Flight f GROUP BY f.droneType")
    List<Object[]> countFlightsByDroneType();

    // Метод для подсчета по статусу обработки
    long countByProcessingStatus(String status);

    // Дополнительные методы для статистики
    @Query("SELECT COUNT(f) FROM Flight f WHERE f.flightDate BETWEEN :startDate AND :endDate")
    long countByFlightDateBetween(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(f) FROM Flight f WHERE f.departureRegion.regionId = :regionId")
    long countByDepartureRegion(@Param("regionId") Long regionId);

    Page<Flight> findByDroneType(String droneType, Pageable pageable);

    long countByFlightDate(LocalDate flightDate);

    @Query("SELECT COUNT(DISTINCT f.droneType) FROM Flight f")
    long countDistinctDroneTypes();
}


