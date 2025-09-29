package com.drones.skilldrones.service;

import com.drones.skilldrones.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public interface FlightService<T> {
    Page<T> getAllFlights(Pageable pageable);
    Optional<T> getFlightById(Long flightId);
    Page<T> getFlightsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<T> getFlightsByDroneType(String droneType, Pageable pageable);
    Map<String, Object> getFlightsStatistics();
    boolean deleteFlight(Long flightId);
}
