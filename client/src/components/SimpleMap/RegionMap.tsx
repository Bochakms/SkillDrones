// components/RegionMap/RegionMap.tsx
import React, { useEffect, useRef } from "react";
import * as am5 from "@amcharts/amcharts5";
import * as am5map from "@amcharts/amcharts5/map";
import am5geodata_russiaLow from "@amcharts/amcharts5-geodata/russiaHigh";
import am5geodata_russiaCrimeaLow from "@amcharts/amcharts5-geodata/russiaCrimeaHigh";
import am5themes_Animated from "@amcharts/amcharts5/themes/Animated";
import type { FeatureCollection, Feature } from "geojson";
import * as d3geo from "d3-geo";
import am5geodata_lang_RU from "@amcharts/amcharts5-geodata/lang/RU";

interface RegionMapProps {
  regionName: string;
  width?: string;
  height?: string;
}

const RegionMap: React.FC<RegionMapProps> = ({
  regionName,
  width = "100%",
  height = "600px",
}) => {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!chartRef.current) return;

    const root = am5.Root.new(chartRef.current);
    root.setThemes([am5themes_Animated.new(root)]);

    // Функция для поиска региона по ID
    const findRegionById = (regionName: string): Feature | null => {
      const combinedGeoJSON: FeatureCollection = {
        type: "FeatureCollection",
        features: [
          ...(am5geodata_russiaLow as FeatureCollection).features,
          ...(am5geodata_russiaCrimeaLow as FeatureCollection).features,
        ],
      };

      // Ищем регион по разным возможным полям
      const region = combinedGeoJSON.features.find((feature: Feature) => {
        return (
          feature.properties?.name?.toLowerCase() === regionName.toLowerCase()
        );
      });

      return region || null;
    };

    const regionFeature = findRegionById(regionName);

    if (!regionFeature) {
      console.error(`Регион с ID ${regionName} не найден`);
      return;
    }

    // Создаем FeatureCollection только с выбранным регионом
    const regionGeoJSON: FeatureCollection = {
      type: "FeatureCollection",
      features: [regionFeature],
    };

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

    const polygonSeries = chart.series.push(
      am5map.MapPolygonSeries.new(root, {
        geoJSON: regionGeoJSON,
        geodataNames: am5geodata_lang_RU,
      })
    );

    // Стили для выделенного региона
    polygonSeries.mapPolygons.template.setAll({
      fill: am5.color("#ff6b6b"),
      fillOpacity: 0.9,
      stroke: am5.color("#ffffff"),
      strokeWidth: 2,
      interactive: false, // Отключаем интерактивность
    });

    // Добавляем название региона
    polygonSeries.mapPolygons.template.set("tooltipText", "{name}");
    polygonSeries.set(
      "tooltip",
      am5.Tooltip.new(root, {
        themeTags: ["map"],
      })
    );

    return () => {
      root.dispose();
    };
  }, [regionName]);

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

export default RegionMap;
