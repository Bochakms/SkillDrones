import React, { useEffect, useRef } from "react";
import * as am5 from "@amcharts/amcharts5";
import * as am5map from "@amcharts/amcharts5/map";
import am5geodata_russiaLow from "@amcharts/amcharts5-geodata/russiaLow";
import am5geodata_russiaCrimeaLow from "@amcharts/amcharts5-geodata/russiaCrimeaLow";
import am5themes_Animated from "@amcharts/amcharts5/themes/Animated";
import type { FeatureCollection } from "geojson";
import * as d3geo from "d3-geo";
import am5geodata_lang_RU from "@amcharts/amcharts5-geodata/lang/RU";

interface RegionData {
  id: string;
  name: string;
  value?: number;
}

interface RussiaMapProps {
  width?: string;
  height?: string;
  onRegionClick?: (region: RegionData) => void;
}

const RussiaMap: React.FC<RussiaMapProps> = ({
  width = "100%",
  height = "600px",
  onRegionClick,
}) => {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!chartRef.current) return;

    const root = am5.Root.new(chartRef.current);
    root.setThemes([am5themes_Animated.new(root)]);

    const customProjection = d3geo.geoTransverseMercator();

    const chart = root.container.children.push(
      am5map.MapChart.new(root, {
        panX: "none",
        panY: "none",
        wheelY: "none",
        pinchZoom: false,
        projection: customProjection,
        rotationX: -90,
      })
    );

    const combinedGeoJSON: FeatureCollection = {
      type: "FeatureCollection",
      features: [
        ...(am5geodata_russiaLow as FeatureCollection).features,
        ...(am5geodata_russiaCrimeaLow as FeatureCollection).features,
      ],
    };

    const polygonSeries = chart.series.push(
      am5map.MapPolygonSeries.new(root, {
        geoJSON: combinedGeoJSON,
        geodataNames: am5geodata_lang_RU,
      })
    );

    polygonSeries.mapPolygons.template.setAll({
      fillOpacity: 0.8,
      stroke: am5.color("#ffffff"),
      strokeWidth: 0.5,
      interactive: true,
    });

    polygonSeries.mapPolygons.template.states.create("hover", {
      fill: am5.color("#ff6b6b"),
      fillOpacity: 1,
    });

    polygonSeries.mapPolygons.template.events.on("click", (ev) => {
      const dataItem = ev.target.dataItem;
      if (dataItem) {
        const dataContext: unknown = dataItem.dataContext;
        if (isRegionData(dataContext) && onRegionClick) {
          onRegionClick(dataContext);
        }
      }
    });

    polygonSeries.mapPolygons.template.set(
      "tooltipHTML",
      "<div style='font-family: Arial, sans-serif;'>" +
        "<div style='font-weight: bold; font-size: 20px; margin-bottom: 5px;'>{name}</div>" +
        "<div style='font-size: 14px; color: #000000;'>Кликните для получения подробной информации</div>" +
        "</div>"
    );

    polygonSeries.set(
      "tooltip",
      am5.Tooltip.new(root, {
        themeTags: ["map"],
        background: am5.RoundedRectangle.new(root, {
          fill: am5.color(0xffffff),
          stroke: am5.color(0xcccccc),
          strokeWidth: 2,
          fillOpacity: 0.9,
        }),
      })
    );

    return () => {
      root.dispose();
    };
  }, [onRegionClick]);

  function isRegionData(data: unknown): data is RegionData {
    return (
      typeof data === "object" &&
      data !== null &&
      "id" in data &&
      "name" in data &&
      typeof (data as RegionData).id === "string" &&
      typeof (data as RegionData).name === "string"
    );
  }

  return (
    <div
      ref={chartRef}
      style={{
        width,
        height,
        backgroundColor: "#f0f2f5",
      }}
    />
  );
};

export default RussiaMap;
