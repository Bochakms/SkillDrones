package com.drones.skilldrones.mapper;

import com.drones.skilldrones.dto.response.ReportFlightResponse;
import com.drones.skilldrones.model.ReportFlight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportFlightMapper {
    @Mapping(source = "reportFlightId", target = "reportFlightId")
    @Mapping(source = "reportLog.reportId", target = "reportId")
    @Mapping(source = "flight.flightId", target = "flightId")
    @Mapping(source = "includedAt", target = "includedAt")
    ReportFlightResponse toResponse(ReportFlight reportFlight);

    List<ReportFlightResponse> toResponseList(List<ReportFlight> reportFlights);
}
