// components/FlightsTable/FlightsTable.tsx
import React from "react";
import type { Flight } from "../../types/flightTypes";
import styles from "./FlightsTable.module.scss";

interface FlightsTableProps {
  flights: Flight[];
  loading?: boolean;
  className?: string;
}

const FlightsTable: React.FC<FlightsTableProps> = ({
  flights,
  loading = false,
  className = "",
}) => {
  if (loading) {
    return <div className={styles.loading}>Загрузка данных...</div>;
  }

  if (flights.length === 0) {
    return <div className={styles.empty}>Нет данных о полетах</div>;
  }

  const formatTime = (time: string) => {
    return time.length === 5 ? time : time.substring(0, 5);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ru-RU");
  };

  const totalDuration = flights.reduce(
    (sum, flight) => sum + flight.durationMinutes,
    0
  );

  return (
    <div className={`${styles.table} ${className}`}>
      <div className={styles.header}>
        Данные о полетах ({flights.length} записей)
      </div>

      <div className={styles.container}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Код полета</th>
              <th>Дрон</th>
              <th>Регистрация</th>
              <th>Дата</th>
              <th>Время вылета</th>
              <th>Время прилета</th>
              <th>Длительность</th>
              <th>Откуда</th>
              <th>Куда</th>
            </tr>
          </thead>
          <tbody>
            {flights.map((flight) => (
              <tr key={flight.flightId}>
                <td className={styles.highlight}>{flight.flightCode}</td>
                <td>{flight.droneType}</td>
                <td>{flight.droneRegistration}</td>
                <td>{formatDate(flight.flightDate)}</td>
                <td>{formatTime(flight.departureTime)}</td>
                <td>{formatTime(flight.arrivalTime)}</td>
                <td>{flight.durationMinutes} мин</td>
                <td>{flight.departureRegionName}</td>
                <td>{flight.arrivalRegionName}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className={styles.footer}>
        Общее время полетов: {totalDuration} минут
      </div>
    </div>
  );
};

export default FlightsTable;
