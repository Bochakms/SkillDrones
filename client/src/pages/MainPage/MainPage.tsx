import React from "react";
import FederalDistrictsMap from "../../components/SimpleMap/FederalDistrictsMap";

const MainPage: React.FC = () => {
  return (
    <div>
      <div style={{ margin: "20px 0" }}>
        <FederalDistrictsMap />
      </div>
    </div>
  );
};

export default MainPage;
