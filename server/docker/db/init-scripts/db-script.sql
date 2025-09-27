-- Создание таблицы для сырых данных
CREATE TABLE IF NOT EXISTS raw_telegrams (
    id BIGSERIAL PRIMARY KEY,
    center VARCHAR(255),
    shr_raw_text TEXT,
    dep_raw_text TEXT,
    arr_raw_text TEXT,
    file_name VARCHAR(255),
    processed_at TIMESTAMP WITH TIME ZONE,
    processing_status VARCHAR(50)
    );

-- Создание таблицы регионов с площадью для расчета Flight Density
CREATE TABLE IF NOT EXISTS regions (
    region_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    area_km2 DOUBLE PRECISION, -- Площадь региона в км² для расчета плотности полетов
    geometry GEOMETRY(Geometry, 4326),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );

-- Создание индексов для ускорения пространственных запросов
CREATE INDEX idx_regions_geometry ON regions USING GIST (geometry);

-- Таблица пользователей (операторы, аналитики, администраторы)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL CHECK (role IN ('OPERATOR', 'ANALYST', 'ADMIN')),
    email VARCHAR(255)
    );

-- Индекс для быстрого поиска по имени пользователя
CREATE INDEX idx_users_role ON users(role);

-- Таблица логов отчетов
CREATE TABLE IF NOT EXISTS report_log (
    report_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    report_type VARCHAR(100) NOT NULL, -- Тип отчета: flights_distribution, time_series, etc.
    report_period_start DATE NOT NULL, -- Начало периода отчета
    report_period_end DATE NOT NULL,   -- Конец периода отчета
    parameters JSONB, -- Параметры отчета в JSON формате
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    file_path VARCHAR(500), -- Путь к сгенерированному файлу (PNG/JSON)
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
                                                                                       );

-- Индексы для быстрого поиска отчетов
CREATE INDEX idx_report_log_user_id ON report_log(user_id);
CREATE INDEX idx_report_log_period ON report_log(report_period_start, report_period_end);
CREATE INDEX idx_report_log_status ON report_log(status);
CREATE INDEX idx_report_log_created_at ON report_log(created_at);

-- Создание таблицы обработанных полетов с дополнительными метриками
CREATE TABLE IF NOT EXISTS flights (
    flight_id BIGSERIAL PRIMARY KEY,
    drone_id INT,
    raw_id BIGINT REFERENCES raw_telegrams(id),
    report_id BIGINT NOT NULL REFERENCES report_log(report_id) ON DELETE CASCADE,
    -- Основная информация о полете
    flight_code VARCHAR(100), -- ID полета из телеграммы
    drone_type VARCHAR(100),
    drone_registration VARCHAR(100),

    -- Временные параметры
    flight_date DATE NOT NULL,
    departure_time TIME,
    arrival_time TIME,
    duration_minutes INTEGER,

    -- Координаты
    departure_coords VARCHAR(100),
    arrival_coords VARCHAR(100),
    departure_point GEOMETRY(Point, 4326),
    arrival_point GEOMETRY(Point, 4326),

    -- Геопривязка к регионам
    departure_region_id BIGINT REFERENCES regions(region_id),
    arrival_region_id BIGINT REFERENCES regions(region_id),

-- Технические поля
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );

-- Пространственные индексы для ускорения геозапросов
CREATE INDEX idx_flights_departure_point ON flights USING GIST (departure_point);
CREATE INDEX idx_flights_arrival_point ON flights USING GIST (arrival_point);
CREATE INDEX idx_flights_departure_region ON flights(departure_region_id);
CREATE INDEX idx_flights_arrival_region ON flights(arrival_region_id);
CREATE INDEX idx_flights_date ON flights(flight_date);
CREATE INDEX idx_flights_drone_type ON flights(drone_type);
-- Индексы для быстрого поиска
CREATE INDEX idx_flights_report_id ON flights(report_id);
CREATE INDEX idx_flights_flight_id ON flights(flight_id);
CREATE INDEX idx_flights_composite ON flights(report_id, flight_id);
-- Таблица для хранения предварительно рассчитанных метрик (оптимизация производительности)
CREATE TABLE IF NOT EXISTS region_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    region_id BIGINT REFERENCES regions(region_id),
    metric_date DATE NOT NULL, -- Дата расчета метрик
    metric_type VARCHAR(50) NOT NULL, -- Тип метрики: daily_flights, flight_density, etc.

-- Базовые метрики
    total_flights INTEGER DEFAULT 0,
    avg_duration_minutes DOUBLE PRECISION,
    peak_hourly_flights INTEGER,

    -- Расширенные метрики из ТЗ
    flight_density DOUBLE PRECISION, -- Полетов на 1000 км²
    zero_days_count INTEGER, -- Дней без полетов за период
    growth_percentage DOUBLE PRECISION, -- Рост/падение по сравнению с предыдущим периодом

    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );

-- Индексы для быстрого доступа к метрикам
CREATE INDEX idx_region_metrics_composite ON region_metrics(region_id, metric_date, metric_type);
CREATE UNIQUE INDEX idx_region_metrics_unique ON region_metrics(region_id, metric_date, metric_type);



