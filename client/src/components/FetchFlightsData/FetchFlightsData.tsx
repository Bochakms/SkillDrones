// components/FetchFlightsData/FetchFlightsData.tsx
import React, { useEffect, useState } from "react";
import { useFlights } from "../../hooks/useFlights";
import FlightsTable from "../FlightsTable/FlightsTable";
import { Filters } from "../Filters/Filters";
import { Button } from "../Button/Button";
import type { FlightsFilter } from "../../types/flightTypes";
import styles from "./FetchFlightsData.module.scss";

interface FetchFlightsDataProps {
  autoLoad?: boolean;
  className?: string;
}

const FetchFlightsData: React.FC<FetchFlightsDataProps> = ({
  autoLoad = true,
  className = "",
}) => {
  const {
    flights,
    loading,
    error,
    lastUpdated,
    regions,
    regionsLoading,
    validationErrors,
    fetchFlights,
    fetchRegions,
    clearError,
    clearFlights,
  } = useFlights();

  const [filters, setFilters] = useState<FlightsFilter>({});

  // Загружаем регионы при монтировании
  useEffect(() => {
    fetchRegions();
  }, [fetchRegions]);

  // Автозагрузка данных
  useEffect(() => {
    if (autoLoad) {
      fetchFlights(filters);
    }
  }, [autoLoad, fetchFlights]);

  const handleApplyFilters = () => {
    fetchFlights(filters);
  };

  const handleResetFilters = () => {
    const resetFilters: FlightsFilter = {};
    setFilters(resetFilters);
    fetchFlights(resetFilters);
  };

  const handleReload = () => {
    fetchFlights(filters);
  };

  return (
    <div className={`${styles.fetchFlights} ${className}`}>
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Данные о полетах</h2>
          {lastUpdated && (
            <div className={styles.timestamp}>
              Обновлено: {lastUpdated.toLocaleTimeString("ru-RU")}
            </div>
          )}
        </div>

        <div className={styles.controls}>
          <Button
            variant="primary"
            onClick={handleReload}
            isDisabled={loading}
            isLoading={loading}
            icon="🔄"
          >
            Обновить
          </Button>

          <Button variant="secondary" onClick={clearFlights} icon="❌">
            Очистить
          </Button>
        </div>
      </div>

      {/* Компонент фильтров */}
      <Filters
        filters={filters}
        regions={regions}
        regionsLoading={regionsLoading}
        validationErrors={validationErrors}
        onFiltersChange={setFilters}
        onApplyFilters={handleApplyFilters}
        onResetFilters={handleResetFilters}
      />

      {error && (
        <div className={styles.error}>
          <span>❌ {error}</span>
          <button
            onClick={clearError}
            style={{
              background: "none",
              border: "none",
              color: "inherit",
              cursor: "pointer",
              fontSize: "16px",
            }}
          >
            ✕
          </button>
        </div>
      )}

      <FlightsTable flights={flights} loading={loading} />

      {flights.length > 0 && !loading && (
        <div className={styles.stats}>
          Показано {flights.length} полетов
          {filters.regionId &&
            ` для региона: ${
              regions.find((r) => r.id === filters.regionId)?.name
            }`}
          {filters.startDate && ` с ${filters.startDate}`}
          {filters.endDate && ` по ${filters.endDate}`}
        </div>
      )}
    </div>
  );
};

export default FetchFlightsData;
