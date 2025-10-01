import React from "react";
import RussiaMap from "../../components/SimpleMap/RussiaMap";
import { useNavigate } from "react-router-dom";
import FederalDistrictsMap from "../../components/SimpleMap/FederalDistrictsMap";
import ExcelUploader from "../../components/ExcelUploader/ExcelUploader";
import type { UploadResponse } from "../../types/uploadTypes";
import FetchFlightsData from "../../components/FetchFlightsData/FetchFlightsData";

interface RegionData {
  id: string;
  name: string;
  value?: number;
}

const MainPage: React.FC = () => {
  const navigate = useNavigate();

  const handleRegionClick = (region: RegionData) => {
    console.log("Выбран регион:", region);
    navigate(`/region/${region.name}`);
  };

  const handleUploadSuccess = (response: UploadResponse) => {
    console.log("Файл успешно загружен:", response);
    // Дополнительные действия после успешной загрузки
  };

  const handleUploadError = (error: string) => {
    console.error("Ошибка загрузки:", error);
    // Обработка ошибок
  };

  const regionData = [
    { regionId: "Sakha", count: 150 },
    { regionId: "Krasnoyarsk", count: 89 },
    { regionId: "Yamalo-Nenets", count: 45 },
    // ... другие регионы
  ];

  return (
    <div>
      <div style={{ marginBottom: "30px" }}>
        <RussiaMap
          width="100%"
          height="600px"
          onRegionClick={handleRegionClick}
          data={regionData}
        />

        <ExcelUploader
          onUploadSuccess={handleUploadSuccess}
          onUploadError={handleUploadError}
        />

        <FetchFlightsData />

        <FederalDistrictsMap />
      </div>
    </div>
  );
};

export default MainPage;
