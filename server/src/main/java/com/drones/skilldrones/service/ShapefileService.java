package com.drones.skilldrones.service;

import com.drones.skilldrones.model.Region;
import org.geotools.data.shapefile.ShapefileDataStore; // ИЗМЕНИТЕ ИМПОРТ
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ShapefileService {

    private final RegionAnalysisService regionAnalysisService;

    private static final Logger log = LoggerFactory.getLogger(ShapefileService.class);

    public ShapefileService(RegionAnalysisService regionAnalysisService) {
        this.regionAnalysisService = regionAnalysisService;
    }

    public int loadRegionsFromMultipart(MultipartFile shapefile,
                                        MultipartFile dbfFile,
                                        MultipartFile shxFile) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String sessionId = UUID.randomUUID().toString();
        String baseFilename = "shapefile_" + sessionId;

        Path shpPath = Paths.get(tempDir, baseFilename + ".shp");
        Path dbfPath = Paths.get(tempDir, baseFilename + ".dbf");
        Path shxPath = Paths.get(tempDir, baseFilename + ".shx");

        try {
            validateShapefileComponents(shapefile, dbfFile, shxFile);

            Files.copy(shapefile.getInputStream(), shpPath);
            Files.copy(dbfFile.getInputStream(), dbfPath);
            Files.copy(shxFile.getInputStream(), shxPath);

            log.info("Временные файлы созданы: {}", shpPath);

            List<Region> regions = loadShapefile(shpPath.toString());
            log.info("Загружено {} регионов из shapefile", regions.size());

            regionAnalysisService.saveRegions(regions);

            return regions.size();

        } catch (Exception e) {
            log.error("Ошибка загрузки шейп-файла", e);
            throw new RuntimeException("Ошибка загрузки шейп-файла: " + e.getMessage(), e);
        } finally {
            cleanupTempFiles(shpPath, dbfPath, shxPath);
        }
    }

    public List<Region> loadShapefile(String shapefilePath) {
        List<Region> regions = new ArrayList<>();
        log.info("Начало парсинга shapefile: {}", shapefilePath);
        try {
            ShapefileDataStore store = createShapefileDataStore(shapefilePath);
            SimpleFeatureSource featureSource = store.getFeatureSource();
            SimpleFeatureCollection collection = featureSource.getFeatures();

            int featureCount = collection.size();
            log.info("Найдено features в shapefile: {}", featureCount);

            try (SimpleFeatureIterator iterator = collection.features()) {
                WKTReader wktReader = new WKTReader();
                int processed = 0;
                int skipped = 0;

                while (iterator.hasNext()) {
                    var feature = iterator.next();
                    processed++;

                    Region region = new Region();

                    String name = getAttributeValue(feature);
                    region.setName(name);

                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    if (geometry != null) {
                        String wkt = geometry.toText();
                        Geometry normalizedGeometry = wktReader.read(wkt);
                        region.setGeometry(normalizedGeometry);

                        double area = calculateAreaKm2(normalizedGeometry);
                        region.setAreaKm2(area);
                    }

                    regions.add(region);

                    // Логируем прогресс
                    if (processed % 1000 == 0) {
                        log.info("Обработано {} регионов...", processed);
                    }
                }

                log.info("Парсинг завершен. Обработано: {}", processed);
            }

            store.dispose();
        } catch (Exception e) {
            log.error("Ошибка парсинга shapefile", e);
            throw new RuntimeException("Ошибка загрузки шейп-файла: " + e.getMessage(), e);
        }

        log.info("Создано {} объектов Region", regions.size());
        return regions;
    }

    private void validateShapefileComponents(MultipartFile shapefile,
                                             MultipartFile dbfFile,
                                             MultipartFile shxFile) {
        if (shapefile.isEmpty() || dbfFile.isEmpty() || shxFile.isEmpty()) {
            throw new IllegalArgumentException("Все компоненты шейп-файла обязательны");
        }

        String shpName = shapefile.getOriginalFilename();
        if (shpName == null || !shpName.toLowerCase().endsWith(".shp")) {
            throw new IllegalArgumentException("Основной файл должен иметь расширение .shp");
        }

        if (shapefile.getSize() == 0 || dbfFile.getSize() == 0 || shxFile.getSize() == 0) {
            throw new IllegalArgumentException("Файлы не должны быть пустыми");
        }
    }

    private void cleanupTempFiles(Path... paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                // Логируем, но не прерываем выполнение
                System.err.println("Не удалось удалить временный файл: " + path);
            }
        }
    }

    private ShapefileDataStore createShapefileDataStore(String shapefilePath) throws Exception {
        File file = new File(shapefilePath);
        URL fileURL = file.toURI().toURL();

        // Проверяем наличие всех компонентов
        if (!file.exists() ||
                !new File(shapefilePath.replace(".shp", ".dbf")).exists() ||
                !new File(shapefilePath.replace(".shp", ".shx")).exists()) {
            throw new IllegalArgumentException("Не все компоненты шейп-файла найдены");
        }

        ShapefileDataStore store = new ShapefileDataStore(fileURL);

        // Автоматически определяем кодировку
        Charset charset = detectCharset(shapefilePath);
        store.setCharset(charset);

        return store;
    }

    private String getAttributeValue(SimpleFeature feature) {
        try {
            Object value = feature.getAttribute("name");
            if (value != null) {
                String stringValue = value.toString();

                // Логируем сырые байты для отладки кодировки
                byte[] bytes = stringValue.getBytes(StandardCharsets.ISO_8859_1);
                log.info("Raw bytes for name: {}", Arrays.toString(bytes));
                log.info("Raw string: '{}'", stringValue);

                if (stringValue.trim().isEmpty()) {
                    return "Unknown";
                }
                return stringValue;
            }
            return "Unknown";
        } catch (Exception e) {
            log.warn("Ошибка получения атрибута 'name': {}", e.getMessage());
            return "Unknown";
        }
    }

    private double calculateAreaKm2(Geometry geometry) {
        try {
            double area = geometry.getArea() * 111.32 * 111.32;
            return Math.abs(Math.round(area * 100) / 100.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Charset detectCharset(String shapefilePath) {
        Charset[] possibleCharsets = {
                Charset.forName("Windows-1251"),  // Windows кириллица
                Charset.forName("CP866"),         // DOS кириллица
                Charset.forName("KOI8-R"),        // Unix кириллица
                Charset.forName("ISO-8859-5"),    // Latin/Cyrillic
                StandardCharsets.UTF_8
        };

        // Попробуем определить по наличию CPG-файла
        try {
            File cpgFile = new File(shapefilePath.replace(".shp", ".cpg"));
            if (cpgFile.exists()) {
                String encoding = Files.readString(cpgFile.toPath()).trim();
                log.info("Используется кодировка из CPG-файла: {}", encoding);
                return Charset.forName(encoding);
            }
        } catch (Exception e) {
            log.warn("CPG-файл не найден или ошибка чтения");
        }

        // Если CPG нет, используем Windows-1251 как наиболее вероятную
        log.info("CPG-файл не найден, используется Windows-1251 по умолчанию");
        return Charset.forName("Windows-1251");
    }
}
