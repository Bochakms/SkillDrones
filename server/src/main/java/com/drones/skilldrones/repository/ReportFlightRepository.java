package com.drones.skilldrones.repository;

import com.drones.skilldrones.model.ReportFlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportFlightRepository extends JpaRepository<ReportFlight, Long> {
    List<ReportFlight> findByReportLog_ReportId(Long reportId);

    List<ReportFlight> findByFlight_FlightId(Long flightId);

    @Query("SELECT rf FROM ReportFlight rf WHERE rf.reportLog.reportId = :reportId")
    List<ReportFlight> findFlightsByReportId(@Param("reportId") Long reportId);

    @Query("SELECT COUNT(rf) FROM ReportFlight rf WHERE rf.reportLog.reportId = :reportId")
    Long countFlightsInReport(@Param("reportId") Long reportId);
}
