// components/FetchFlightsData/FetchFlightsData.tsx
import React, { useEffect, useState } from "react";
import { useFlights } from "../../hooks/useFlights";
import FlightsTable from "../FlightsTable/FlightsTable";
import { Filters } from "../Filters/Filters";
import { Button } from "../Button/Button";
import type { FlightsFilter } from "../../types/flightTypes";
import styles from "./FetchFlightsData.module.scss";

interface FetchFlightsDataProps {
  regionName?: string;
  autoLoad?: boolean;
  className?: string;
}

const FetchFlightsData: React.FC<FetchFlightsDataProps> = ({
  regionName,
  autoLoad = true,
  className = "",
}) => {
  const {
    flights,
    loading,
    error,
    lastUpdated,
    fetchFlights,
    clearError,
    clearFlights,
  } = useFlights();

  const [filters, setFilters] = useState<FlightsFilter>({});

  useEffect(() => {
    if (autoLoad) {
      const filtersWithRegion = regionName
        ? { ...filters, regionName }
        : filters;

      fetchFlights(filtersWithRegion);
    }
  }, [autoLoad, fetchFlights, regionName, filters]);

  const handleApplyFilters = () => {
    const filtersWithRegion = regionName ? { ...filters, regionName } : filters;

    fetchFlights(filtersWithRegion);
  };

  const handleResetFilters = () => {
    const resetFilters: FlightsFilter = {};
    setFilters(resetFilters);

    const filtersWithRegion = regionName
      ? { ...resetFilters, regionName }
      : resetFilters;

    fetchFlights(filtersWithRegion);
  };

  const handleReload = () => {
    const filtersWithRegion = regionName ? { ...filters, regionName } : filters;

    fetchFlights(filtersWithRegion);
  };

  return (
    <div className={`${styles.fetchFlights} ${className}`}>
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>
            {regionName
              ? `Данные о полетах - ${regionName}`
              : "Данные о полетах"}
          </h2>
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
          >
            Обновить
          </Button>

          <Button variant="secondary" onClick={clearFlights}>
            Очистить
          </Button>
        </div>
      </div>

      <Filters
        filters={filters}
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
          {regionName && ` для региона: ${regionName}`}
          {filters.startDate && ` с ${filters.startDate}`}
          {filters.endDate && ` по ${filters.endDate}`}
        </div>
      )}
    </div>
  );
};

export default FetchFlightsData;
