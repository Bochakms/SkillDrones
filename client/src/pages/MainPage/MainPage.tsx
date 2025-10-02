import React from "react";
import RussiaMap from "../../components/SimpleMap/RussiaMap";
import { useNavigate } from "react-router-dom";
import FederalDistrictsMap from "../../components/SimpleMap/FederalDistrictsMap";
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

  return (
    <div>
      <div style={{ marginBottom: "30px" }}>
        <RussiaMap
          width="100%"
          height="600px"
          onRegionClick={handleRegionClick}
        />

        <FetchFlightsData />

        <FederalDistrictsMap />
      </div>
    </div>
  );
};

export default MainPage;
