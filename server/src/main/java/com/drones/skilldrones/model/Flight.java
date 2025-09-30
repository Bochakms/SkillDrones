package com.drones.skilldrones.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "flights",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flight_composite_key",
                        columnNames = {
                                "departure_time",
                                "arrival_time",
                                "departure_coords",
                                "arrival_coords"
                        }
                )
        }
)
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id") // Явно указываем имя столбца
    private Long flightId;

    @Column(name = "drone_id")
    private Integer droneId;

    @ManyToOne
    @JoinColumn(name = "raw_id")
    private RawTelegram rawTelegram;

    @Column(name = "flight_code")
    private String flightCode;

    @Column(name = "drone_type")
    private String droneType;

    @Column(name = "drone_registration")
    private String droneRegistration;

    @Column(name = "flight_date")
    private LocalDate flightDate;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "departure_coords")
    private String departureCoords;

    @Column(name = "arrival_coords")
    private String arrivalCoords;

    @Column(name = "processing_status")
    private String processingStatus;



    @Column(columnDefinition = "geometry(Point,4326)")
    private Point departurePoint;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point arrivalPoint;

    @ManyToOne
    @JoinColumn(name = "departure_region_id")
    private Region departureRegion;

    @ManyToOne
    @JoinColumn(name = "arrival_region_id")
    private Region arrivalRegion;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReportFlight> reportFlights = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструкторы
    public Flight() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Integer getDroneId() {
        return droneId;
    }

    public void setDroneId(Integer droneId) {
        this.droneId = droneId;
    }

    public RawTelegram getRawTelegram() {
        return rawTelegram;
    }

    public void setRawTelegram(RawTelegram rawTelegram) {
        this.rawTelegram = rawTelegram;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getDroneType() {
        return droneType;
    }

    public void setDroneType(String droneType) {
        this.droneType = droneType;
    }

    public String getDroneRegistration() {
        return droneRegistration;
    }

    public void setDroneRegistration(String droneRegistration) {
        this.droneRegistration = droneRegistration;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(LocalDate flightDate) {
        this.flightDate = flightDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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

    public Point getDeparturePoint() {
        return departurePoint;
    }

    public void setDeparturePoint(Point departurePoint) {
        this.departurePoint = departurePoint;
    }

    public Point getArrivalPoint() {
        return arrivalPoint;
    }

    public void setArrivalPoint(Point arrivalPoint) {
        this.arrivalPoint = arrivalPoint;
    }

    public Region getDepartureRegion() {
        return departureRegion;
    }

    public void setDepartureRegion(Region departureRegion) {
        this.departureRegion = departureRegion;
    }

    public Region getArrivalRegion() {
        return arrivalRegion;
    }

    public void setArrivalRegion(Region arrivalRegion) {
        this.arrivalRegion = arrivalRegion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
}
