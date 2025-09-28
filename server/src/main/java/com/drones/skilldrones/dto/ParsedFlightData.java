package com.drones.skilldrones.dto;
import com.drones.skilldrones.model.RawTelegram;
import java.time.LocalDate;

public class ParsedFlightData {

        private String flightId;
        private String droneType;
        private LocalDate flightDate;
        private String coordinates;
        private RawTelegram rawTelegram;


        // геттеры и сеттеры
        public String getFlightId() { return flightId; }
        public void setFlightId(String flightId) { this.flightId = flightId; }

        public String getDroneType() { return droneType; }
        public void setDroneType(String droneType) { this.droneType = droneType; }

        public LocalDate getFlightDate() { return flightDate; }
        public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }

        public String getCoordinates() { return coordinates; }
        public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

        public RawTelegram getRawTelegram() { return rawTelegram; }
        public void setRawTelegram(RawTelegram rawTelegram) { this.rawTelegram = rawTelegram; }
}
