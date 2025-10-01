package com.drones.skilldrones.service;

import com.drones.skilldrones.dto.ParsedFlightData;
import com.drones.skilldrones.model.RawTelegram;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;

public interface FileParserService {
    /**
     * Парсит Excel файл и возвращает список сырых телеграмм
     */
    List<RawTelegram> parseExcelFile(MultipartFile file);

    /**
     * Парсит Excel файл и возвращает структурированные данные о полетах
     */
    List<ParsedFlightData> parseFlightData(MultipartFile file);

    /**
     * Извлекает данные о полете из текста телеграммы
     */
    ParsedFlightData extractFlightDataFromTelegram(RawTelegram telegram);

    /**
     * Новый метод: извлекает время из текста телеграммы
     */
    LocalTime extractTimeFromTelegram(String text, String timeType); // DEPARTURE или ARRIVAL

    void saveParsedDataAsFlight(ParsedFlightData flightData);

}
