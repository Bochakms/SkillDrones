package com.drones.skilldrones.dto.response;

import java.time.LocalDateTime;

public record ReportFlightResponse(Long reportFlightId,
                                   Long reportId,
                                   Long flightId,
                                   LocalDateTime includedAt) {
}
