package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.model.RawTelegram;
import com.drones.skilldrones.repository.RawTelegramRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class FileParserServiceImpl implements FileParserService {
    private final RawTelegramRepository rawTelegramRepository;

    public FileParserServiceImpl(RawTelegramRepository rawTelegramRepository) {
        this.rawTelegramRepository = rawTelegramRepository;
    }

    @Override
    public List<RawTelegram> parseExcelFile(MultipartFile file) {
        List<RawTelegram> telegrams = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                RawTelegram telegram = parseRow(row);
                if (telegram != null) {
                    telegram.setFileName(file.getOriginalFilename());
                    rawTelegramRepository.save(telegram);
                    telegrams.add(telegram);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга Excel файла: " + e.getMessage(), e);
        }

        return telegrams;
    }

    @Override
    public List<ParsedFlightData> parseFlightData(MultipartFile file) {
        List<ParsedFlightData> flightDataList = new ArrayList<>();
        List<RawTelegram> telegrams = parseExcelFile(file);

        for (RawTelegram telegram : telegrams) {
            try {
                ParsedFlightData flightData = extractFlightDataFromTelegram(telegram);
                if (flightData != null) {
                    flightDataList.add(flightData);
                }
            } catch (Exception e) {
                System.err.println("Ошибка парсинга телеграммы: " + e.getMessage());
            }
        }

        return flightDataList;
    }


    @Override
    public LocalTime extractTimeFromTelegram(String text, String timeType) {
        if (text == null) return null;

        try {
            // Паттерн для времени: -0705 или -0600
            Pattern pattern = Pattern.compile("-(\\d{4})");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String timeStr = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
                return LocalTime.parse(timeStr, formatter);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга времени: " + e.getMessage());
        }

        return null;
    }

    // Обновляем метод extractFlightDataFromTelegram
    @Override
    public ParsedFlightData extractFlightDataFromTelegram(RawTelegram telegram) {
        ParsedFlightData data = new ParsedFlightData();

        if (telegram.getShrRawText() != null) {
            String coords = extractCoordinates(telegram.getShrRawText());
            if (coords != null) {
                data.setCoordinates(coords);
            }

            LocalDate flightDate = extractFlightDate(telegram.getShrRawText());
            data.setFlightDate(flightDate);

            String droneType = extractDroneType(telegram.getShrRawText());
            data.setDroneType(droneType);

            String flightId = extractFlightId(telegram.getShrRawText());
            data.setFlightId(flightId);
        }

        data.setRawTelegram(telegram);
        return data;
    }

    private RawTelegram parseRow(Row row) {
        if (row.getPhysicalNumberOfCells() < 4) return null;

        RawTelegram telegram = new RawTelegram();
        telegram.setCenter(getCellStringValue(row.getCell(0)));
        telegram.setShrRawText(getCellStringValue(row.getCell(1)));
        telegram.setDepRawText(getCellStringValue(row.getCell(2)));
        telegram.setArrRawText(getCellStringValue(row.getCell(3)));
        telegram.setProcessingStatus("PENDING");

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
        if (text == null) return null;

        // Паттерн для координат типа "5957N02905E" или "5548N03730E"
        Pattern pattern = Pattern.compile("(\\d{4}[NS]\\d{5}[EW])");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return normalizeCoordinates(matcher.group(1));
        }
        return null;
    }

    private String normalizeCoordinates(String coords) {
        try {
            // Пример: "5957N02905E" -> "59.95,29.08"
            String latStr = coords.substring(0, 4);
            String latDir = coords.substring(4, 5);
            String lonStr = coords.substring(5, 9);
            String lonDir = coords.substring(9, 10);

            double lat = Double.parseDouble(latStr.substring(0, 2)) +
                    Double.parseDouble(latStr.substring(2, 4)) / 60.0;
            double lon = Double.parseDouble(lonStr.substring(0, 3)) +
                    Double.parseDouble(lonStr.substring(3, 5)) / 60.0;

            if ("S".equals(latDir)) lat = -lat;
            if ("W".equals(lonDir)) lon = -lon;

            return String.format("%.6f,%.6f", lat, lon);
        } catch (Exception e) {
            return coords; // возвращаем оригинальный формат если не удалось распарсить
        }
    }

    private LocalDate extractFlightDate(String text) {
        if (text == null) return null;

        // Паттерн для даты: DOF/250201 (DDMMYY)
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

        // Паттерн для типа: TYP/BLA или TYP/SHAR и т.д.
        Pattern pattern = Pattern.compile("TYP/([A-Z]{3})");
        Matcher matcher = pattern.matcher(text);

        return matcher.find() ? matcher.group(1) : "UNKNOWN";
    }

    private String extractFlightId(String text) {
        if (text == null) return null;

        // Ищем ID в начале строки SHR-XXXXX
        String[] lines = text.split("\n");
        if (lines.length > 0 && lines[0].startsWith("SHR-")) {
            return lines[0].substring(4).trim();
        }
        return null;
    }
}
