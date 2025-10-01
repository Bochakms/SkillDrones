package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.dto.ProcessingStats;
import com.drones.skilldrones.mapper.FlightMapper;
import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.model.RawTelegram;
import com.drones.skilldrones.model.Region;
import com.drones.skilldrones.repository.FlightRepository;
import com.drones.skilldrones.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlightProcessingServiceImpl implements FlightProcessingService {

    private final FileParserService fileParserService;
    private final RegionRepository regionRepository;
    private final FlightRepository flightRepository;
    private final FlightMapper flightProcessingMapper;

    public FlightProcessingServiceImpl(FileParserService fileParserService,
                                       RegionRepository regionRepository,
                                       FlightRepository flightRepository,
                                       FlightMapper flightProcessingMapper) {
        this.fileParserService = fileParserService;
        this.regionRepository = regionRepository;
        this.flightRepository = flightRepository;
        this.flightProcessingMapper = flightProcessingMapper;
    }

    @Override
    @Transactional
    public void processRawTelegrams(List<RawTelegram> rawTelegrams) {
        for (RawTelegram telegram : rawTelegrams) {
            try {
                Flight flight = convertToFlight(telegram);
                flightRepository.save(flight);
                telegram.setProcessingStatus("PROCESSED");
            } catch (Exception e) {
                telegram.setProcessingStatus("FAILED");
            }
        }
    }

    @Override
    public int processBatch(List<RawTelegram> telegrams) {
        int successful = 0;
        System.out.println("Начало обработки " + telegrams.size() + " телеграмм");

        for (RawTelegram telegram : telegrams) {
            try {
                Flight flight = convertToFlight(telegram);
                Flight savedFlight = flightRepository.save(flight);
                telegram.setProcessingStatus("PROCESSED");
                successful++;
                System.out.println("Создан Flight с ID: " + savedFlight.getFlightId());
            } catch (Exception e) {
                telegram.setProcessingStatus("FAILED");
                System.err.println("Ошибка обработки телеграммы " + telegram.getId() + ": " + e.getMessage());
                e.printStackTrace(); // Добавьте для деталей ошибки
            }
        }

        System.out.println("Успешно обработано: " + successful + " из " + telegrams.size());
        return successful;
    }

    @Transactional
    @Override
    public Flight convertToFlight(RawTelegram telegram) {
        // Парсим данные из телеграммы
        ParsedFlightData parsedData = fileParserService.extractFlightDataFromTelegram(telegram);

        // Используем маппер для основного преобразования
        Flight flight = flightProcessingMapper.toFlight(parsedData);

        // Дополнительная бизнес-логика, которая не входит в маппер
        setFlightTimes(flight, parsedData, telegram);
        performGeolocation(flight);
        calculateDuration(flight);

        return flight;
    }

    private void setFlightTimes(Flight flight, ParsedFlightData parsedData, RawTelegram telegram) {
        // Парсим время из телеграммы (если есть соответствующая логика в FileParserService)
        LocalTime departureTime = parseDepartureTime(telegram);
        LocalTime arrivalTime = parseArrivalTime(telegram);

        if (departureTime != null) {
            flight.setDepartureTime(departureTime);
        } else {
            flight.setDepartureTime(LocalTime.now()); // временное значение
        }

        if (arrivalTime != null) {
            flight.setArrivalTime(arrivalTime);
        } else if (departureTime != null) {
            flight.setArrivalTime(departureTime.plusMinutes(30)); // временная логика
        } else {
            flight.setArrivalTime(LocalTime.now().plusMinutes(30));
        }
    }

    /**
     * Выполняет геопривязку к регионам
     */
    private void performGeolocation(Flight flight) {
        if (flight.getDeparturePoint() != null) {
            Optional<Region> departureRegion = regionRepository.findRegionByPoint(flight.getDeparturePoint());
            departureRegion.ifPresent(flight::setDepartureRegion);

            // Для простоты считаем, что вылет и прилет в одном месте
            flight.setArrivalPoint(flight.getDeparturePoint());
            departureRegion.ifPresent(flight::setArrivalRegion);
        }
    }

    /**
     * Рассчитывает продолжительность полета
     */
    private void calculateDuration(Flight flight) {
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            long durationMinutes = java.time.Duration.between(
                    flight.getDepartureTime(), flight.getArrivalTime()
            ).toMinutes();
            flight.setDurationMinutes((int) Math.max(0, durationMinutes));
        }
    }

    /**
     * Парсит время вылета из телеграммы
     */
    private LocalTime parseDepartureTime(RawTelegram telegram) {
        // Реализация парсинга времени из DEP-телеграммы
        // Пример: поиск паттерна времени в dep_raw_text
        if (telegram.getDepRawText() != null) {
            // Паттерн для времени: ATD 0705
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("ATD (\\d{4})");
            java.util.regex.Matcher matcher = pattern.matcher(telegram.getDepRawText());
            if (matcher.find()) {
                return flightProcessingMapper.extractTime(matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Парсит время прилета из телеграммы
     */
    private LocalTime parseArrivalTime(RawTelegram telegram) {
        // Реализация парсинга времени из ARR-телеграммы
        if (telegram.getArrRawText() != null) {
            // Паттерн для времени: ATA 1250
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("ATA (\\d{4})");
            java.util.regex.Matcher matcher = pattern.matcher(telegram.getArrRawText());
            if (matcher.find()) {
                return flightProcessingMapper.extractTime(matcher.group(1));
            }
        }
        return null;
    }

    @Override
    public ProcessingStats getProcessingStats() {
        // Реализация сбора статистики из базы данных
        long totalProcessed = flightRepository.count();
        long successful = flightRepository.countByProcessingStatus("PROCESSED");
        long failed = flightRepository.countByProcessingStatus("FAILED");

        return new ProcessingStats(
                (int) totalProcessed,
                (int) successful,
                (int) failed
        );
    }
}
