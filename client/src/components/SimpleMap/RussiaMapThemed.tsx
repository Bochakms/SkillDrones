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

interface RegionCount {
  regionId: string;
  count: number;
}

interface RussiaMapProps {
  width?: string;
  height?: string;
  data?: RegionCount[];
  onRegionClick?: (region: RegionData) => void;
}

const RussiaMap: React.FC<RussiaMapProps> = ({
  width = "100%",
  height = "600px",
  data = [],
  onRegionClick,
}) => {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!chartRef.current) return;

    console.log("=== ИНИЦИАЛИЗАЦИЯ КАРТЫ ===");
    console.log("Полученные данные:", data);

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

    // Создаем карту данных для быстрого поиска
    const dataMap = data.reduce((acc, item) => {
      acc[item.regionId] = item.count;
      return acc;
    }, {} as Record<string, number>);

    console.log("DataMap создан:", dataMap);

    // Создаем массив данных для серии
    const seriesData: RegionData[] = (combinedGeoJSON.features || []).map(
      (feature) => {
        const properties = feature.properties || {};
        const regionName = properties.name;
        const count = dataMap[regionName] || 0;

        const regionData: RegionData = {
          id: (feature.id as string) || properties.id || "",
          name: regionName,
          value: count,
        };

        console.log(`Регион: ${regionName}, значение: ${count}`);

        return regionData;
      }
    );

    console.log("Все данные серии:", seriesData);

    const polygonSeries = chart.series.push(
      am5map.MapPolygonSeries.new(root, {
        geoJSON: combinedGeoJSON,
        geodataNames: am5geodata_lang_RU,
      })
    );

    // Логируем создание dataItems
    polygonSeries.events.on("datavalidated", () => {
      console.log("=== DATA VALIDATED ===");
      console.log("DataItems созданы:", polygonSeries.dataItems.length);
      polygonSeries.dataItems.forEach((dataItem, index) => {
        const dataContext = dataItem.dataContext as RegionData;
        console.log(`DataItem ${index}:`, dataContext);
      });
    });

    // Устанавливаем данные в серию
    polygonSeries.data.setAll(seriesData);
    console.log("Данные установлены в серию");

    // Функция для получения цвета на основе значения
    const getColor = (value: number): am5.Color => {
      const color =
        value === 0
          ? am5.color(0xcccccc)
          : value <= 10
          ? am5.color(0x67b7dc)
          : value <= 50
          ? am5.color(0x3498db)
          : value <= 100
          ? am5.color(0x2980b9)
          : am5.color(0x1c4e80);

      console.log(`getColor(${value}) = ${color}`);
      return color;
    };

    // Настраиваем внешний вид полигонов
    polygonSeries.mapPolygons.template.setAll({
      fill: am5.color(0x67b7dc), // базовый цвет
      fillOpacity: 0.8,
      stroke: am5.color(0xffffff),
      strokeWidth: 0.5,
      interactive: true,
    });

    // Устанавливаем цвета для каждого полигона на основе данных через адаптер
    polygonSeries.mapPolygons.template.adapters.add("fill", (fill, target) => {
      const dataItem = target.dataItem;
      console.log("Адаптер fill вызван для target:", target);

      if (dataItem) {
        const dataContext = dataItem.dataContext as RegionData;
        console.log("DataContext в адаптере:", dataContext);

        if (dataContext) {
          const newColor = getColor(dataContext.value);
          console.log(
            `Устанавливаем цвет для ${dataContext.name}: ${newColor}`
          );
          return newColor;
        }
      }

      console.log("Возвращаем базовый цвет:", fill);
      return fill;
    });

    polygonSeries.mapPolygons.template.states.create("hover", {
      fill: am5.color(0xff6b6b),
      fillOpacity: 1,
    });

    // Логируем создание полигонов
    polygonSeries.events.on("dataitemsvalidated", () => {
      console.log("=== DATAITEMS VALIDATED ===");
      console.log("Полигоны созданы:", polygonSeries.dataItems.length);
    });

    polygonSeries.mapPolygons.template.events.on("click", (ev) => {
      const dataItem = ev.target.dataItem;
      console.log("Клик по региону:", ev.target, dataItem);

      if (dataItem) {
        const dataContext = dataItem.dataContext as RegionData;
        console.log("DataContext при клике:", dataContext);

        if (onRegionClick) {
          onRegionClick(dataContext);
        }
      }
    });

    // Добавляем метки поверх регионов с логированием
    polygonSeries.bullets.push((root, series, dataItem) => {
      console.log("=== СОЗДАНИЕ БУЛЛЕТА ===");
      console.log("DataItem для буллета:", dataItem);

      if (!dataItem) {
        console.log("Нет dataItem, пропускаем создание буллета");
        return undefined;
      }

      const dataContext = dataItem.dataContext as RegionData;
      console.log("DataContext для буллета:", dataContext);

      const value = dataContext?.value || 0;
      console.log(`Значение для буллета: ${value}`);

      // Не показываем метку если значение 0
      if (value === 0) {
        console.log("Значение 0, буллет не создается");
        return undefined;
      }

      console.log("Создаем буллет для значения:", value);

      const label = am5.Label.new(root, {
        text: `{value}`,
        fill: am5.color(0x000000),
        centerX: am5.p50,
        centerY: am5.p50,
        populateText: true,
        fontSize: 10,
        fontWeight: "bold",
        background: am5.RoundedRectangle.new(root, {
          fill: am5.color(0xffffff),
          fillOpacity: 0.9,
          strokeWidth: 0,
        }),
        paddingTop: 3,
        paddingBottom: 3,
        paddingLeft: 5,
        paddingRight: 5,
      });

      // Логируем события label
      label.events.on("validated", () => {
        console.log("Label validated, текст:", label.text());
      });

      const bullet = am5.Bullet.new(root, {
        sprite: label,
      });

      console.log("Буллет создан успешно");
      return bullet;
    });

    // Логируем создание буллетов
    polygonSeries.events.on("bulletcreated", (ev) => {
      console.log("=== БУЛЛЕТ СОЗДАН ===");
      console.log("Bullet:", ev.target);
      console.log("DataItem:", ev.target.dataItem);
    });

    // Настраиваем тултипы
    polygonSeries.mapPolygons.template.set(
      "tooltipText",
      "[bold]{name}[/]\nКоличество: {value}"
    );

    polygonSeries.set(
      "tooltip",
      am5.Tooltip.new(root, {
        themeTags: ["map"],
      })
    );

    // Логируем готовность карты
    chart.events.on("ready", () => {
      console.log("=== КАРТА ГОТОВА ===");
      console.log("Все dataItems:", polygonSeries.dataItems.length);

      // Проверяем цвета полигонов
      polygonSeries.dataItems.forEach((dataItem, index) => {
        const polygon = dataItem.get("graphics");
        const dataContext = dataItem.dataContext as RegionData;
        console.log(`Полигон ${index} (${dataContext.name}):`, {
          fill: polygon?.get("fill"),
          value: dataContext.value,
        });
      });
    });

    return () => {
      console.log("=== УНИЧТОЖЕНИЕ КАРТЫ ===");
      root.dispose();
    };
  }, [onRegionClick, data]);

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
