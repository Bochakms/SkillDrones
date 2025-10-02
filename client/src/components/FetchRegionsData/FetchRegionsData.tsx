// components/FetchRegionsData/FetchRegionsData.tsx
import React, { useEffect, useState } from "react";
import { regionsApi } from "../../api/regionsApi";
import RegionsTable from "../RegionsTable/RegionsTable";
import { Button } from "../Button/Button";
import type { Region } from "../../types/regionTypes";
import styles from "./FetchRegionsData.module.scss";

interface FetchRegionsDataProps {
  autoLoad?: boolean;
  className?: string;
}

interface RegionsState {
  regions: Region[];
  loading: boolean;
  error: string | null;
  lastUpdated: Date | null;
}

const FetchRegionsData: React.FC<FetchRegionsDataProps> = ({
  autoLoad = true,
  className = "",
}) => {
  const [state, setState] = useState<RegionsState>({
    regions: [],
    loading: false,
    error: null,
    lastUpdated: null,
  });

  // Автозагрузка данных при монтировании
  useEffect(() => {
    if (autoLoad) {
      fetchRegions();
    }
  }, [autoLoad]);

  const fetchRegions = async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));

    try {
      const regions = await regionsApi.getRegions();

      setState({
        regions,
        loading: false,
        error: null,
        lastUpdated: new Date(),
      });
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Неизвестная ошибка";
      setState((prev) => ({
        ...prev,
        loading: false,
        error: errorMessage,
      }));
    }
  };

  const handleReload = () => {
    fetchRegions();
  };

  const clearRegions = () => {
    setState({
      regions: [],
      loading: false,
      error: null,
      lastUpdated: null,
    });
  };

  const clearError = () => {
    setState((prev) => ({ ...prev, error: null }));
  };

  return (
    <div className={`${styles.fetchRegions} ${className}`}>
      <div className={styles.header}>
        <div>
          <h2 className={styles.title}>Данные по регионам</h2>
          {state.lastUpdated && (
            <div className={styles.timestamp}>
              Обновлено: {state.lastUpdated.toLocaleTimeString("ru-RU")}
            </div>
          )}
        </div>

        <div className={styles.controls}>
          <Button
            variant="primary"
            onClick={handleReload}
            isDisabled={state.loading}
            isLoading={state.loading}
          >
            Обновить
          </Button>

          <Button variant="secondary" onClick={clearRegions}>
            Очистить
          </Button>
        </div>
      </div>

      {state.error && (
        <div className={styles.error}>
          <span>❌ {state.error}</span>
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

      <RegionsTable regions={state.regions} loading={state.loading} />

      {state.regions.length > 0 && !state.loading && (
        <div className={styles.stats}>
          Показано {state.regions.length} регионов
        </div>
      )}
    </div>
  );
};

export default FetchRegionsData;
