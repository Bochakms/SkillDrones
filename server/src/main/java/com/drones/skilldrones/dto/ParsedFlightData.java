package com.drones.skilldrones.dto;
import com.drones.skilldrones.model.RawTelegram;

import java.time.LocalDate;
import java.time.LocalTime;

public class ParsedFlightData {

        private String flightId;
        private String droneType;
        private LocalDate flightDate;
        private String coordinates;
        private RawTelegram rawTelegram;
        private LocalTime departureTime;
        private LocalTime arrivalTime;
        private Integer durationMinutes;
        private String departureCoords;
        private String arrivalCoords;

        public Integer getDurationMinutes() {
                return durationMinutes;
        }

        public String getDepartureCoords() {
                return departureCoords;
        }

        public void setDepartureCoords(String departureCoords) {
                this.departureCoords = departureCoords;
        }

        public String getArrivalCoords() {
                return arrivalCoords;
        }

        public void setArrivalCoords(String arrivalCoords) {
                this.arrivalCoords = arrivalCoords;
        }

        public void setDurationMinutes(Integer durationMinutes) {
                this.durationMinutes = durationMinutes;
        }

        public LocalTime getArrivalTime() {
                return arrivalTime;
        }

        public void setArrivalTime(LocalTime arrivalTime) {
                this.arrivalTime = arrivalTime;
        }

        public LocalTime getDepartureTime() {
                return departureTime;
        }

        public void setDepartureTime(LocalTime departureTime) {
                this.departureTime = departureTime;
        }

        public String getFlightId() {
                return flightId;
        }

        public void setFlightId(String flightId) {
                this.flightId = flightId;
        }

        public String getDroneType() { return droneType; }
        public void setDroneType(String droneType) { this.droneType = droneType; }

        public LocalDate getFlightDate() { return flightDate; }
        public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }

        public String getCoordinates() { return coordinates; }
        public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

        public RawTelegram getRawTelegram() { return rawTelegram; }
        public void setRawTelegram(RawTelegram rawTelegram) { this.rawTelegram = rawTelegram; }
}
