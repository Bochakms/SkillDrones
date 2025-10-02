// components/Filters/Filters.tsx
import React from "react";
import { Button } from "../Button/Button";
import type { FlightsFilter } from "../../types/flightTypes";
import styles from "./Filters.module.scss";

interface FiltersProps {
  filters: FlightsFilter;
  onFiltersChange: (filters: FlightsFilter) => void;
  onApplyFilters: () => void;
  onResetFilters: () => void;
  className?: string;
}

export const Filters: React.FC<FiltersProps> = ({
  filters,
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

  const hasActiveFilters = Boolean(filters.startDate || filters.endDate);

  return (
    <div className={`${styles.filters} ${className}`}>
      <h3 className={styles.title}>Фильтры по дате</h3>

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
      </div>

      <div className={styles.actions}>
        <Button variant="primary" onClick={onApplyFilters}>
          Применить фильтры
        </Button>

        {hasActiveFilters && (
          <Button variant="secondary" onClick={onResetFilters}>
            Сбросить
          </Button>
        )}
      </div>

      {hasActiveFilters && (
        <div className={styles.activeFilters}>
          <strong>Активные фильтры:</strong>
          {filters.startDate && ` С ${filters.startDate}`}
          {filters.endDate && ` По ${filters.endDate}`}
        </div>
      )}
    </div>
  );
};
