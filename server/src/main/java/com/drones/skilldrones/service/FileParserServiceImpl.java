package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.model.Flight;
import com.drones.skilldrones.model.RawTelegram;
import com.drones.skilldrones.repository.FlightRepository;
import com.drones.skilldrones.repository.RawTelegramRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileParserServiceImpl implements FileParserService {
    private final RawTelegramRepository rawTelegramRepository;
    private final FlightRepository flightRepository;
    private final GeometryFactory geometryFactory;

    public FileParserServiceImpl(RawTelegramRepository rawTelegramRepository,
                                 FlightRepository flightRepository) {
        this.rawTelegramRepository = rawTelegramRepository;
        this.flightRepository = flightRepository;
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public List<RawTelegram> parseExcelFile(MultipartFile file) {
        List<RawTelegram> telegrams = new ArrayList<>();
        System.out.println("Начало парсинга Excel файла: " + file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("Всего строк в файле: " + (sheet.getLastRowNum() + 1));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    System.out.println("Строка " + i + " пустая - пропускаем");
                    continue;
                }

                RawTelegram telegram = parseRow(row);
                if (telegram != null) {
                    telegram.setFileName(file.getOriginalFilename());
                    rawTelegramRepository.save(telegram);
                    telegrams.add(telegram);
                    System.out.println("Сохранен RawTelegram: " + telegram.getShrRawText());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга Excel файла: " + e.getMessage(), e);
        }

        System.out.println("Всего распарсено телеграмм: " + telegrams.size());
        return telegrams;
    }

    @Override
    public List<ParsedFlightData> parseFlightData(MultipartFile file) {
        List<ParsedFlightData> flightDataList = new ArrayList<>();
        List<RawTelegram> telegrams = parseExcelFile(file);

        System.out.println("Начало обработки " + telegrams.size() + " телеграмм в Flight данные");

        for (RawTelegram telegram : telegrams) {
            try {
                System.out.println("Обрабатываем телеграмму ID: " + telegram.getId());
                ParsedFlightData flightData = extractFlightDataFromTelegram(telegram);
                if (flightData != null) {
                    flightDataList.add(flightData);
                    // Сохраняем данные в Flight
                    saveParsedDataAsFlight(flightData);
                    System.out.println("Успешно обработан Flight для телеграммы " + telegram.getId());
                } else {
                    System.out.println("Не удалось извлечь данные из телеграммы " + telegram.getId());
                }
            } catch (Exception e) {
                System.err.println("Ошибка парсинга телеграммы " + telegram.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Всего создано Flight данных: " + flightDataList.size());
        return flightDataList;
    }

    @Transactional
    public void saveParsedDataAsFlight(ParsedFlightData flightData) {
        try {
            Flight flight = new Flight();

            // Устанавливаем базовые данные
            flight.setRawTelegram(flightData.getRawTelegram());
            flight.setFlightCode(flightData.getFlightId());
            flight.setDroneType(flightData.getDroneType());
            flight.setFlightDate(flightData.getFlightDate());

            // Устанавливаем время
            flight.setDepartureTime(flightData.getDepartureTime());
            flight.setArrivalTime(flightData.getArrivalTime());

            // Устанавливаем координаты в строковом формате
            String coordinates = flightData.getCoordinates();
            System.out.println("Сохраняем координаты в Flight: " + coordinates);

            if (coordinates != null) {
                flight.setDepartureCoords(coordinates);
                flight.setArrivalCoords(coordinates);

                // Преобразуем координаты в Point geometry
                createGeometryPoints(flight, coordinates);
            }

            // Рассчитываем продолжительность
            if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
                int durationMinutes = calculateDurationMinutes(
                        flight.getDepartureTime(),
                        flight.getArrivalTime()
                );
                flight.setDurationMinutes(durationMinutes);
                System.out.println("Продолжительность полета: " + durationMinutes + " минут");
            }

            flight.setProcessingStatus("PARSED");
            flight.setCreatedAt(LocalDateTime.now());
            flight.setUpdatedAt(LocalDateTime.now());

            // Сохраняем Flight
            Flight savedFlight = flightRepository.save(flight);
            System.out.println("Сохранен Flight ID: " + savedFlight.getFlightId() +
                    " с координатами: " + coordinates);

        } catch (Exception e) {
            System.err.println("Ошибка сохранения Flight: " + e.getMessage());
            e.printStackTrace();
            throw e; // Пробрасываем исключение дальше для отладки
        }
    }

    /**
     * Создает Point geometry из координат и устанавливает в departurePoint и arrivalPoint
     */
    private void createGeometryPoints(Flight flight, String coordinates) {
        try {
            if (coordinates == null || coordinates.trim().isEmpty()) {
                System.out.println("Координаты пустые - пропускаем создание Point");
                return;
            }

            System.out.println("Создаем Point из координат: " + coordinates);

            // Парсим координаты из строкового формата "59.950000,29.083333"
            String[] coordParts = coordinates.split(",");
            if (coordParts.length == 2) {
                double latitude = Double.parseDouble(coordParts[0].trim());
                double longitude = Double.parseDouble(coordParts[1].trim());

                System.out.println("Преобразовано в числа: lat=" + latitude + ", lon=" + longitude);

                // Создаем Point для departure (Coordinate: x=longitude, y=latitude)
                Point departurePoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                departurePoint.setSRID(4326); // Устанавливаем SRID для WGS84
                flight.setDeparturePoint(departurePoint);

                // Создаем Point для arrival (используем те же координаты)
                Point arrivalPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                arrivalPoint.setSRID(4326); // Устанавливаем SRID для WGS84
                flight.setArrivalPoint(arrivalPoint);

                System.out.println("Успешно созданы Point geometry с SRID 4326");
            } else {
                System.out.println("Неверный формат координат: " + coordinates);
            }
        } catch (Exception e) {
            System.err.println("Ошибка создания Point geometry из '" + coordinates + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public LocalTime extractTimeFromTelegram(String text, String timeType) {
        if (text == null) return null;

        try {
            Pattern pattern = Pattern.compile("-(\\d{4})");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String timeStr = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
                LocalTime time = LocalTime.parse(timeStr, formatter);
                System.out.println("Извлечено время " + timeType + ": " + time);
                return time;
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга времени: " + e.getMessage());
        }

        return null;
    }

    @Override
    public ParsedFlightData extractFlightDataFromTelegram(RawTelegram telegram) {
        ParsedFlightData data = new ParsedFlightData();
        System.out.println("Извлекаем данные из телеграммы: " + telegram.getId());

        if (telegram.getShrRawText() != null) {
            System.out.println("SHR текст: " + telegram.getShrRawText());

            // Извлекаем координаты в формате "5957N02905E"
            String coords = extractCoordinates(telegram.getShrRawText());
            if (coords != null) {
                data.setCoordinates(coords);
                data.setDepartureCoords(coords);
                data.setArrivalCoords(coords);
                System.out.println("Извлечены координаты: " + coords);
            } else {
                System.out.println("Координаты не найдены в тексте");
            }

            LocalDate flightDate = extractFlightDate(telegram.getShrRawText());
            data.setFlightDate(flightDate);
            System.out.println("Дата полета: " + flightDate);

            String droneType = extractDroneType(telegram.getShrRawText());
            data.setDroneType(droneType);
            System.out.println("Тип дрона: " + droneType);

            String flightId = extractFlightId(telegram.getShrRawText());
            data.setFlightId(flightId);
            System.out.println("ID полета: " + flightId);

            // Извлекаем время
            LocalTime departureTime = extractTimeFromTelegram(telegram.getShrRawText(), "departure");
            LocalTime arrivalTime = extractTimeFromTelegram(telegram.getShrRawText(), "arrival");
            data.setDepartureTime(departureTime);
            data.setArrivalTime(arrivalTime);
        } else {
            System.out.println("SHR текст пустой");
        }

        data.setRawTelegram(telegram);
        return data;
    }

    private int calculateDurationMinutes(LocalTime departure, LocalTime arrival) {
        if (departure == null || arrival == null) {
            System.out.println("Не удалось рассчитать продолжительность - время отсутствует");
            return 0;
        }

        int departureMinutes = departure.getHour() * 60 + departure.getMinute();
        int arrivalMinutes = arrival.getHour() * 60 + arrival.getMinute();

        if (arrivalMinutes < departureMinutes) {
            arrivalMinutes += 24 * 60; // полет через полночь
        }

        int duration = arrivalMinutes - departureMinutes;
        System.out.println("Продолжительность: " + departure + " -> " + arrival + " = " + duration + " мин");
        return duration;
    }

    private RawTelegram parseRow(Row row) {
        if (row.getPhysicalNumberOfCells() < 4) {
            System.out.println("Строка содержит меньше 4 ячеек - пропускаем");
            return null;
        }

        RawTelegram telegram = new RawTelegram();
        telegram.setCenter(getCellStringValue(row.getCell(0)));
        telegram.setShrRawText(getCellStringValue(row.getCell(1)));
        telegram.setDepRawText(getCellStringValue(row.getCell(2)));
        telegram.setArrRawText(getCellStringValue(row.getCell(3)));
        telegram.setProcessingStatus("PENDING");

        System.out.println("Создан RawTelegram: " + telegram.getShrRawText());
        return telegram;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String extractCoordinates(String text) {
        if (text == null) {
            System.out.println("Текст для поиска координат пустой");
            return null;
        }

        // Паттерн для координат типа "5957N02905E" или "5548N03730E"
        Pattern pattern = Pattern.compile("(\\d{4}[NS]\\d{5}[EW])");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String foundCoords = matcher.group(1);
            System.out.println("Найдены координаты в тексте: " + foundCoords);
            String normalized = normalizeCoordinates(foundCoords);
            System.out.println("Нормализованные координаты: " + normalized);
            return normalized;
        } else {
            System.out.println("Координаты не найдены в тексте: " + text);
            return null;
        }
    }

    private String normalizeCoordinates(String coords) {
        try {
            // Пример: "6057N07345E" -> "60.950000,73.750000"
            // Формат: DDMMNDDDMME (4 цифры широта + направление + 5 цифр долгота + направление)
            String latStr = coords.substring(0, 4);  // "6057"
            String latDir = coords.substring(4, 5);  // "N"
            String lonStr = coords.substring(5, 10); // "07345" - берем 5 цифр!
            String lonDir = coords.substring(10, 11); // "E"

            System.out.println("Парсим координаты: lat=" + latStr + latDir + " lon=" + lonStr + lonDir);

            // Преобразуем широту: 6057 -> 60 градусов 57 минут
            double latDegrees = Double.parseDouble(latStr.substring(0, 2)); // 60
            double latMinutes = Double.parseDouble(latStr.substring(2, 4)); // 57
            double lat = latDegrees + (latMinutes / 60.0);
            if ("S".equals(latDir)) lat = -lat;

            // Преобразуем долготу: 07345 -> 073 градуса 45 минут
            double lonDegrees = Double.parseDouble(lonStr.substring(0, 3)); // 073
            double lonMinutes = Double.parseDouble(lonStr.substring(3, 5)); // 45
            double lon = lonDegrees + (lonMinutes / 60.0);
            if ("W".equals(lonDir)) lon = -lon;

            String result = String.format("%.6f,%.6f", lat, lon);
            System.out.println("Результат нормализации: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("Ошибка нормализации координат '" + coords + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private LocalDate extractFlightDate(String text) {
        if (text == null) return LocalDate.now();

        Pattern pattern = Pattern.compile("DOF/(\\d{6})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String dateStr = matcher.group(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
            return LocalDate.parse(dateStr, formatter);
        }
        return LocalDate.now();
    }

    private String extractDroneType(String text) {
        if (text == null) return "UNKNOWN";

        Pattern pattern = Pattern.compile("TYP/([A-Z]{3})");
        Matcher matcher = pattern.matcher(text);

        return matcher.find() ? matcher.group(1) : "UNKNOWN";
    }

    private String extractFlightId(String text) {
        if (text == null) return null;

        String[] lines = text.split("\n");
        if (lines.length > 0 && lines[0].startsWith("SHR-")) {
            return lines[0].substring(4).trim();
        }
        return null;
    }

}
//package com.drones.skilldrones.service;
//
//import com.drones.skilldrones.dto.ParsedFlightData;
//import com.drones.skilldrones.model.Flight;
//import com.drones.skilldrones.model.RawTelegram;
//import com.drones.skilldrones.repository.FlightRepository;
//import com.drones.skilldrones.repository.RawTelegramRepository;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.locationtech.jts.geom.Coordinate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.Point;
//
//import java.io.InputStream;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//@Service
//public class FileParserServiceImpl implements FileParserService {
//    private final RawTelegramRepository rawTelegramRepository;
//    private final FlightRepository flightRepository;
//    private final GeometryFactory geometryFactory;
//
//    public FileParserServiceImpl(RawTelegramRepository rawTelegramRepository, FlightRepository flightRepository) {
//        this.rawTelegramRepository = rawTelegramRepository;
//        this.flightRepository = flightRepository;
//        this.geometryFactory = new GeometryFactory();
//    }
//
//    @Override
//    public List<RawTelegram> parseExcelFile(MultipartFile file) {
//        List<RawTelegram> telegrams = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream();
//             Workbook workbook = new XSSFWorkbook(inputStream)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                RawTelegram telegram = parseRow(row);
//                if (telegram != null) {
//                    telegram.setFileName(file.getOriginalFilename());
//                    rawTelegramRepository.save(telegram);
//                    telegrams.add(telegram);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка парсинга Excel файла: " + e.getMessage(), e);
//        }
//
//        return telegrams;
//    }
//
//    @Override
//    public List<ParsedFlightData> parseFlightData(MultipartFile file) {
//        List<ParsedFlightData> flightDataList = new ArrayList<>();
//        List<RawTelegram> telegrams = parseExcelFile(file);
//
//        for (RawTelegram telegram : telegrams) {
//            try {
//                ParsedFlightData flightData = extractFlightDataFromTelegram(telegram);
//                if (flightData != null) {
//                    flightDataList.add(flightData);
//                }
//            } catch (Exception e) {
//                System.err.println("Ошибка парсинга телеграммы: " + e.getMessage());
//            }
//        }
//
//        return flightDataList;
//    }
//
//
//    @Override
//    public LocalTime extractTimeFromTelegram(String text, String timeType) {
//        if (text == null) return null;
//
//        try {
//            // Паттерн для времени: -0705 или -0600
//            Pattern pattern = Pattern.compile("-(\\d{4})");
//            Matcher matcher = pattern.matcher(text);
//
//            if (matcher.find()) {
//                String timeStr = matcher.group(1);
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
//                return LocalTime.parse(timeStr, formatter);
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка парсинга времени: " + e.getMessage());
//        }
//
//        return null;
//    }
//
//    // Обновляем метод extractFlightDataFromTelegram
//    @Override
//    public ParsedFlightData extractFlightDataFromTelegram(RawTelegram telegram) {
//        ParsedFlightData data = new ParsedFlightData();
//
//        if (telegram.getShrRawText() != null) {
//            String coords = extractCoordinates(telegram.getShrRawText());
//            if (coords != null) {
//                data.setCoordinates(coords);
//            }
//
//            LocalDate flightDate = extractFlightDate(telegram.getShrRawText());
//            data.setFlightDate(flightDate);
//
//            String droneType = extractDroneType(telegram.getShrRawText());
//            data.setDroneType(droneType);
//
//            String flightId = extractFlightId(telegram.getShrRawText());
//            data.setFlightId(flightId);
//        }
//
//        data.setRawTelegram(telegram);
//        return data;
//    }
//
//    private RawTelegram parseRow(Row row) {
//        if (row.getPhysicalNumberOfCells() < 4) return null;
//
//        RawTelegram telegram = new RawTelegram();
//        telegram.setCenter(getCellStringValue(row.getCell(0)));
//        telegram.setShrRawText(getCellStringValue(row.getCell(1)));
//        telegram.setDepRawText(getCellStringValue(row.getCell(2)));
//        telegram.setArrRawText(getCellStringValue(row.getCell(3)));
//        telegram.setProcessingStatus("PENDING");
//
//        return telegram;
//    }
//
//    private String getCellStringValue(Cell cell) {
//        if (cell == null) return "";
//
//        switch (cell.getCellType()) {
//            case STRING:
//                return cell.getStringCellValue().trim();
//            case NUMERIC:
//                if (DateUtil.isCellDateFormatted(cell)) {
//                    return cell.getLocalDateTimeCellValue().toString();
//                } else {
//                    return String.valueOf((long) cell.getNumericCellValue());
//                }
//            case BOOLEAN:
//                return String.valueOf(cell.getBooleanCellValue());
//            default:
//                return "";
//        }
//    }
//
//    private String extractCoordinates(String text) {
//        if (text == null) return null;
//
//        // Паттерн для координат типа "5957N02905E" или "5548N03730E"
//        Pattern pattern = Pattern.compile("(\\d{4}[NS]\\d{5}[EW])");
//        Matcher matcher = pattern.matcher(text);
//
//        if (matcher.find()) {
//            return normalizeCoordinates(matcher.group(1));
//        }
//        return null;
//    }
//
//    private String normalizeCoordinates(String coords) {
//        try {
//            // Пример: "5957N02905E" -> "59.95,29.08"
//            String latStr = coords.substring(0, 4);
//            String latDir = coords.substring(4, 5);
//            String lonStr = coords.substring(5, 9);
//            String lonDir = coords.substring(9, 10);
//
//            double lat = Double.parseDouble(latStr.substring(0, 2)) +
//                    Double.parseDouble(latStr.substring(2, 4)) / 60.0;
//            double lon = Double.parseDouble(lonStr.substring(0, 3)) +
//                    Double.parseDouble(lonStr.substring(3, 5)) / 60.0;
//
//            if ("S".equals(latDir)) lat = -lat;
//            if ("W".equals(lonDir)) lon = -lon;
//
//            return String.format("%.6f,%.6f", lat, lon);
//        } catch (Exception e) {
//            return coords; // возвращаем оригинальный формат если не удалось распарсить
//        }
//    }
//
//    private LocalDate extractFlightDate(String text) {
//        if (text == null) return null;
//
//        // Паттерн для даты: DOF/250201 (DDMMYY)
//        Pattern pattern = Pattern.compile("DOF/(\\d{6})");
//        Matcher matcher = pattern.matcher(text);
//
//        if (matcher.find()) {
//            String dateStr = matcher.group(1);
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
//            return LocalDate.parse(dateStr, formatter);
//        }
//        return LocalDate.now();
//    }
//
//    private String extractDroneType(String text) {
//        if (text == null) return "UNKNOWN";
//
//        // Паттерн для типа: TYP/BLA или TYP/SHAR и т.д.
//        Pattern pattern = Pattern.compile("TYP/([A-Z]{3})");
//        Matcher matcher = pattern.matcher(text);
//
//        return matcher.find() ? matcher.group(1) : "UNKNOWN";
//    }
//
//    private String extractFlightId(String text) {
//        if (text == null) return null;
//
//        // Ищем ID в начале строки SHR-XXXXX
//        String[] lines = text.split("\n");
//        if (lines.length > 0 && lines[0].startsWith("SHR-")) {
//            return lines[0].substring(4).trim();
//        }
//        return null;
//    }
//
//    @Override
//    public void saveParsedDataAsFlight(ParsedFlightData flightData) {
//        Flight flight = new Flight();
//
//        // Устанавливаем базовые данные
//        flight.setRawTelegram(flightData.getRawTelegram());
//        flight.setFlightCode(flightData.getFlightId());
//        flight.setDroneType(flightData.getDroneType());
//        flight.setFlightDate(flightData.getFlightDate());
//
//        // Устанавливаем координаты - КРИТИЧЕСКИ ВАЖНО!
//        String coordinates = flightData.getCoordinates();
//        if (coordinates != null) {
//            // Разделяем координаты на departure и arrival
//            // Если в coordinates только одна точка, используем её для обоих
//            flight.setDepartureCoords(coordinates);
//            flight.setArrivalCoords(coordinates);
//
//            // Создаем geometry точки
//            updateGeometryFromCoords(flight);
//        }
//
//        // Устанавливаем время
//        flight.setDepartureTime(flightData.getDepartureTime());
//        flight.setArrivalTime(flightData.getArrivalTime());
//
//        // Рассчитываем продолжительность
//        if (flightData.getDepartureTime() != null && flightData.getArrivalTime() != null) {
//            int durationMinutes = calculateDurationMinutes(
//                    flightData.getDepartureTime(),
//                    flightData.getArrivalTime()
//            );
//            flight.setDurationMinutes(durationMinutes);
//        }
//
//        flight.setProcessingStatus("PARSED");
//
//        // Сохраняем
//        flightRepository.save(flight);
//    }
//
//
//    private void updateGeometryFromCoords(Flight flight) {
//        try {
//            // Для departure
//            if (flight.getDepartureCoords() != null) {
//                String[] coords = flight.getDepartureCoords().split(",");
//                if (coords.length == 2) {
//                    double lat = Double.parseDouble(coords[0].trim());
//                    double lon = Double.parseDouble(coords[1].trim());
//                    Point departurePoint = geometryFactory.createPoint(new Coordinate(lon, lat));
//                    departurePoint.setSRID(4326);
//                    flight.setDeparturePoint(departurePoint);
//                }
//            }
//
//            // Для arrival (используем те же координаты)
//            if (flight.getArrivalCoords() != null) {
//                String[] coords = flight.getArrivalCoords().split(",");
//                if (coords.length == 2) {
//                    double lat = Double.parseDouble(coords[0].trim());
//                    double lon = Double.parseDouble(coords[1].trim());
//                    Point arrivalPoint = geometryFactory.createPoint(new Coordinate(lon, lat));
//                    arrivalPoint.setSRID(4326);
//                    flight.setArrivalPoint(arrivalPoint);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Ошибка создания geometry: " + e.getMessage());
//        }
//    }
//
//    private int calculateDurationMinutes(LocalTime departure, LocalTime arrival) {
//        if (departure == null || arrival == null) return 0;
//
//        int departureMinutes = departure.getHour() * 60 + departure.getMinute();
//        int arrivalMinutes = arrival.getHour() * 60 + arrival.getMinute();
//
//        if (arrivalMinutes < departureMinutes) {
//            arrivalMinutes += 24 * 60; // полет через полночь
//        }
//
//        return arrivalMinutes - departureMinutes;
//    }
//}
//
//
//
