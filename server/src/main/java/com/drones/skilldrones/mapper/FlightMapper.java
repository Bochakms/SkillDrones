package com.drones.skilldrones.mapper;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.dto.response.FlightResponse;
import com.drones.skilldrones.model.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    // 1. Основной маппинг из ParsedFlightData в Flight
    @Mapping(source = "flightId", target = "flightCode")
    @Mapping(source = "coordinates", target = "departureCoords")
    @Mapping(source = "coordinates", target = "arrivalCoords")
    @Mapping(target = "departurePoint", ignore = true)
    @Mapping(target = "arrivalPoint", ignore = true)
    @Mapping(target = "departureRegion", ignore = true)
    @Mapping(target = "arrivalRegion", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "droneId", ignore = true)
    Flight toFlight(ParsedFlightData parsedData);

    // 2. Отдельный метод для маппинга Flight в FlightResponse
    default FlightResponse toFlightResponse(Flight flight) {
        if (flight == null) {
            return null;
        }
        return new FlightResponse(
                flight.getFlightId(),
                flight.getDroneId(),
                flight.getRawTelegram() != null ? flight.getRawTelegram().getId() : null,
                flight.getFlightCode(),
                flight.getDroneType(),
                flight.getDroneRegistration(),
                flight.getFlightDate(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getDurationMinutes(),
                flight.getDepartureCoords(),
                flight.getArrivalCoords(),
                flight.getDepartureRegion() != null ? flight.getDepartureRegion().getRegionId() : null,
                flight.getArrivalRegion() != null ? flight.getArrivalRegion().getRegionId() : null,
                flight.getDepartureRegion() != null ? flight.getDepartureRegion().getName() : null,
                flight.getArrivalRegion() != null ? flight.getArrivalRegion().getName() : null,
                flight.getCreatedAt(),
                flight.getUpdatedAt()
        );
    }

    default List<FlightResponse> toFlightResponseList(List<Flight> flights) {
        if (flights == null) {
            return Collections.emptyList();
        }
        return flights.stream()
                .map(this::toFlightResponse)
                .collect(Collectors.toList());
    }

    @Named("coordinatesToPoint")
    default org.locationtech.jts.geom.Point coordinatesToPoint(String coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        try {
            String[] parts = coordinates.split(",");
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());

            org.locationtech.jts.geom.GeometryFactory geometryFactory =
                    new org.locationtech.jts.geom.GeometryFactory();
            return geometryFactory.createPoint(
                    new org.locationtech.jts.geom.Coordinate(lon, lat)
            );
        } catch (Exception e) {
            return null;
        }
    }

    default LocalTime extractTime(String timeText) {
        if (timeText == null) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
            return LocalTime.parse(timeText, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
