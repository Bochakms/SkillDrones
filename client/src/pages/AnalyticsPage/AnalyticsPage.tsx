import React from "react";
import RussiaMap from "../../components/SimpleMap/RussiaMap";
import { useNavigate } from "react-router-dom";
import FetchRegionsData from "../../components/FetchRegionsData/FetchRegionsData";

interface RegionData {
  id: string;
  name: string;
  value?: number;
}

const AnalyticsPage: React.FC = () => {
  const navigate = useNavigate();

  const handleRegionClick = (region: RegionData) => {
    console.log("Выбран регион:", region);
    navigate(`/region/${region.name}`);
  };

  return (
    <div style={{ margin: "20px 0" }}>
      <RussiaMap
        width="100%"
        height="600px"
        onRegionClick={handleRegionClick}
      />

      <div style={{ marginTop: "30px" }}>
        <FetchRegionsData />
      </div>
    </div>
  );
};

export default AnalyticsPage;
