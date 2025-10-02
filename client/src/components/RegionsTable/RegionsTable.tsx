import React from "react";
import styles from "./RegionsTable.module.scss";
import type { Region } from "../../types/regionTypes";

interface RegionsTableProps {
  regions: Region[];
  loading?: boolean;
  className?: string;
}

const RegionsTable: React.FC<RegionsTableProps> = ({
  regions,
  loading = false,
  className = "",
}) => {
  if (loading) {
    return <div className={styles.loading}>Загрузка данных...</div>;
  }

  if (regions.length === 0) {
    return <div className={styles.empty}>Нет данных по регионам</div>;
  }

  return (
    <div className={`${styles.table} ${className}`}>
      <div className={styles.header}>
        Данные по регионам ({regions.length} записей)
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Регион</th>
              <th>Площадь</th>
              <th>Общее кол-во вылетов</th>
            </tr>
          </thead>
          <tbody>
            {regions.map((region) => (
              <tr key={region.regionId}>
                <td className={styles.highlight}>{region.name}</td>
                <td>{region.areaKm2}</td>
                <td>{region.totalFlights}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RegionsTable;
