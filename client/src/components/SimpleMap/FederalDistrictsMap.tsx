// components/FederalDistrictsMap/AmChartsUnion.tsx
import React, { useEffect, useRef } from "react";
import * as am5 from "@amcharts/amcharts5";
import * as am5map from "@amcharts/amcharts5/map";
import am5geodata_russiaLow from "@amcharts/amcharts5-geodata/russiaLow";
import am5geodata_russiaCrimeaLow from "@amcharts/amcharts5-geodata/russiaCrimeaLow";
import am5themes_Animated from "@amcharts/amcharts5/themes/Animated";
import type { FeatureCollection } from "geojson";
import am5geodata_lang_RU from "@amcharts/amcharts5-geodata/lang/RU";
import * as d3geo from "d3-geo";

interface FederalDistrict {
  id: string;
  name: string;
  regionIds: string[];
  color?: string;
}

const federalDistricts: FederalDistrict[] = [
  {
    id: "central",
    name: "Центральный федеральный округ",
    regionIds: [
      "RU-MOW",
      "RU-MOS",
      "RU-BEL",
      "RU-BRY",
      "RU-VLA",
      "RU-VOR",
      "RU-IVA",
      "RU-KLU",
      "RU-KOS",
      "RU-KRS",
      "RU-LIP",
      "RU-ORL",
      "RU-RYA",
      "RU-SMO",
      "RU-TAM",
      "RU-TVE",
      "RU-TUL",
      "RU-YAR",
    ],
    color: "#ff6b6b",
  },
  {
    id: "northwestern",
    name: "Северо-Западный федеральный округ",
    regionIds: [
      "RU-SPE",
      "RU-LEN",
      "RU-ARK",
      "RU-VLG",
      "RU-KGD",
      "RU-KR",
      "RU-KO",
      "RU-NEN",
      "RU-NGR",
      "RU-PSK",
      "RU-MUR",
    ],
    color: "#4ecdc4",
  },
  {
    id: "southern",
    name: "Южный федеральный округ",
    regionIds: [
      "RU-AD",
      "RU-AST",
      "RU-VGG",
      "RU-KL",
      "RU-KDA",
      "RU-KRY",
      "RU-ROS",
      "RU-SEV",
      "RU-CR",
    ],
    color: "#45b7d1",
  },
  {
    id: "northcaucasus",
    name: "Северо-Кавказский федеральный округ",
    regionIds: ["RU-DA", "RU-IN", "RU-KB", "RU-KC", "RU-SE", "RU-STA", "RU-CE"],
    color: "#96ceb4",
  },
  {
    id: "volga",
    name: "Приволжский федеральный округ",
    regionIds: [
      "RU-BA",
      "RU-KIR",
      "RU-ME",
      "RU-MO",
      "RU-NIZ",
      "RU-ORE",
      "RU-PNZ",
      "RU-SAM",
      "RU-SAR",
      "RU-TA",
      "RU-UD",
      "RU-ULY",
      "RU-CU",
      "RU-PER",
    ],
    color: "#feca57",
  },
  {
    id: "ural",
    name: "Уральский федеральный округ",
    regionIds: ["RU-KGN", "RU-KHM", "RU-SVE", "RU-TYU", "RU-CHE", "RU-YAN"],
    color: "#ff9ff3",
  },
  {
    id: "siberian",
    name: "Сибирский федеральный округ",
    regionIds: [
      "RU-ALT",
      "RU-AL",
      "RU-BU",
      "RU-ZAB",
      "RU-IRK",
      "RU-KEM",
      "RU-KYA",
      "RU-NVS",
      "RU-OMS",
      "RU-TOM",
      "RU-KK",
      "RU-TY",
    ],
    color: "#54a0ff",
  },
  {
    id: "far eastern",
    name: "Дальневосточный федеральный округ",
    regionIds: [
      "RU-AMU",
      "RU-YEV",
      "RU-KAM",
      "RU-MAG",
      "RU-PRI",
      "RU-SA",
      "RU-SAK",
      "RU-KHA",
      "RU-CHU",
    ],
    color: "#5f27cd",
  },
];

const FederalDistrictsMap: React.FC = () => {
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

    // Получаем все регионы
    const allRegions: FeatureCollection = {
      type: "FeatureCollection",
      features: [
        ...(am5geodata_russiaLow as FeatureCollection).features,
        ...(am5geodata_russiaCrimeaLow as FeatureCollection).features,
      ],
    };

    // Создаем серию для каждого федерального округа
    federalDistricts.forEach((district) => {
      // Фильтруем регионы, входящие в округ
      const districtRegions = allRegions.features.filter(
        (feature) =>
          district.regionIds.includes(feature.properties?.id as string) ||
          district.regionIds.includes(feature.id as string)
      );

      if (districtRegions.length === 0) return;

      // Создаем FeatureCollection для округа
      const districtGeoJSON: FeatureCollection = {
        type: "FeatureCollection",
        features: districtRegions,
      };

      // Создаем серию для этого округа
      const polygonSeries = chart.series.push(
        am5map.MapPolygonSeries.new(root, {
          geoJSON: districtGeoJSON,
          geodataNames: am5geodata_lang_RU,
          fill: am5.color(district.color || "#67b7dc"),
        })
      );

      // Настраиваем внешний вид без границ между регионами
      polygonSeries.mapPolygons.template.setAll({
        fill: am5.color("#67b7dc"),
        fillOpacity: 0.8,
        stroke: am5.color("#ffffff"),
        strokeWidth: 2,
        interactive: true,
      });

      // Убираем внутренние границы (границы между регионами внутри округа)
      polygonSeries.mapPolygons.template.set(
        "stroke",
        am5.color("rgba(255, 255, 255, 0)")
      ); // Прозрачные границы

      // Внешние границы округа
      polygonSeries.mapPolygons.template.set("strokeWidth", 1);

      // События
      polygonSeries.mapPolygons.template.events.on("click", () => {
        console.log("Выбран округ:", district.name);
      });

      polygonSeries.mapPolygons.template.set("tooltipText", district.name);
    });

    return () => {
      root.dispose();
    };
  }, []);

  return (
    <div
      ref={chartRef}
      style={{ width: "100%", height: "600px", backgroundColor: "#f0f2f5" }}
    />
  );
};

export default FederalDistrictsMap;
