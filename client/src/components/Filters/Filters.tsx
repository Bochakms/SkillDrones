// components/Filters/Filters.tsx
import React from "react";
import { Button } from "../Button/Button";
import type { FlightsFilter, Region } from "../../types/flightTypes";
import styles from "./Filters.module.scss";

interface FiltersProps {
  filters: FlightsFilter;
  regions: Region[];
  regionsLoading: boolean;
  validationErrors: string[];
  onFiltersChange: (filters: FlightsFilter) => void;
  onApplyFilters: () => void;
  onResetFilters: () => void;
  className?: string;
}

export const Filters: React.FC<FiltersProps> = ({
  filters,
  regions,
  regionsLoading,
  validationErrors,
  onFiltersChange,
  onApplyFilters,
  onResetFilters,
  className = "",
}) => {
  const handleDateChange = (field: "startDate" | "endDate", value: string) => {
    onFiltersChange({
      ...filters,
      [field]: value,
    });
  };

  const handleRegionChange = (regionId: string) => {
    onFiltersChange({
      ...filters,
      regionId: regionId ? parseInt(regionId) : undefined,
    });
  };

  const hasActiveFilters = Boolean(
    filters.startDate || filters.endDate || filters.regionId
  );

  return (
    <div className={`${styles.filters} ${className}`}>
      <h3 className={styles.title}>Фильтры</h3>

      {/* Показ ошибок валидации */}
      {validationErrors.length > 0 && (
        <div className={styles.validationErrors}>
          <strong>Ошибки валидации:</strong>
          {validationErrors.map((error, index) => (
            <div key={index} className={styles.validationError}>
              ❌ {error}
            </div>
          ))}
        </div>
      )}

      <div className={styles.grid}>
        {/* Период дат */}
        <div className={styles.field}>
          <label className={styles.label}>Начальная дата</label>
          <input
            type="date"
            value={filters.startDate || ""}
            onChange={(e) => handleDateChange("startDate", e.target.value)}
            className={styles.input}
            max={filters.endDate}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Конечная дата</label>
          <input
            type="date"
            value={filters.endDate || ""}
            onChange={(e) => handleDateChange("endDate", e.target.value)}
            className={styles.input}
            min={filters.startDate}
          />
        </div>

        {/* Регион */}
        <div className={styles.field}>
          <label className={styles.label}>Регион</label>
          <select
            value={filters.regionId || ""}
            onChange={(e) => handleRegionChange(e.target.value)}
            className={styles.select}
            disabled={regionsLoading}
          >
            <option value="">Все регионы</option>
            {regions.map((region) => (
              <option key={region.id} value={region.id}>
                {region.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className={styles.actions}>
        <Button
          variant="primary"
          onClick={onApplyFilters}
          icon="🔍"
          isDisabled={validationErrors.length > 0}
        >
          Применить фильтры
        </Button>

        {hasActiveFilters && (
          <Button variant="secondary" onClick={onResetFilters} icon="🗑️">
            Сбросить
          </Button>
        )}
      </div>

      {hasActiveFilters && (
        <div className={styles.activeFilters}>
          <strong>Активные фильтры:</strong>
          {filters.startDate && ` С ${filters.startDate}`}
          {filters.endDate && ` По ${filters.endDate}`}
          {filters.regionId &&
            ` Регион: ${regions.find((r) => r.id === filters.regionId)?.name}`}
        </div>
      )}
    </div>
  );
};
