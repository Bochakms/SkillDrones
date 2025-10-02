// pages/RegionPage.tsx
import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import RegionMap from "../../components/SimpleMap/RegionMap";
import FetchFlightsData from "../../components/FetchFlightsData/FetchFlightsData";

const RegionPage: React.FC = () => {
  const { regionName } = useParams<{ regionName: string }>();
  const navigate = useNavigate();

  if (!regionName) {
    return <div>Регион не указан</div>;
  }

  const handleBackClick = () => {
    navigate(-1);
  };

  return (
    <div style={{ margin: "20px 0" }}>
      <button
        onClick={handleBackClick}
        style={{
          marginBottom: "20px",
          padding: "10px 20px",
          backgroundColor: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
        }}
      >
        Назад
      </button>

      <h1>Регион: {regionName}</h1>

      <div style={{ marginTop: "30px" }}>
        <RegionMap regionName={regionName} width="100%" height="600px" />
      </div>

      {/* Дополнительная информация о регионе */}
      <div style={{ marginTop: "30px" }}>
        <h2>Информация о регионе</h2>

        <FetchFlightsData regionName={regionName} />
      </div>
    </div>
  );
};

export default RegionPage;
