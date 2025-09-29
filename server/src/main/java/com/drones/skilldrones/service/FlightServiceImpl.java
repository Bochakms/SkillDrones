package com.drones.skilldrones.service;

import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.repository.FlightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class FlightServiceImpl implements FlightService<Flight> {

    private final FlightRepository flightRepository;

    public FlightServiceImpl (FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public Page<Flight> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }

    @Override
    public Optional<Flight> getFlightById(Long flightId) {
        return flightRepository.findById(flightId);
    }

    @Override
    public Page<Flight> getFlightsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return flightRepository.findByFlightDateBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Flight> getFlightsByDroneType(String droneType, Pageable pageable) {
        return flightRepository.findByDroneType(droneType, pageable);
    }

    @Override
    public Map<String, Object> getFlightsStatistics() {
        long totalFlights = flightRepository.count();
        long todayFlights = flightRepository.countByFlightDate(LocalDate.now());
        long uniqueDroneTypes = flightRepository.countDistinctDroneTypes();

        return Map.of(
                "totalFlights", totalFlights,
                "todayFlights", todayFlights,
                "uniqueDroneTypes", uniqueDroneTypes,
                "lastUpdated", LocalDate.now()
        );
    }

    @Override
    public boolean deleteFlight(Long flightId) {
        if (flightRepository.existsById(flightId)) {
            flightRepository.deleteById(flightId);
            return true;
        }
        return false;
    }
}
