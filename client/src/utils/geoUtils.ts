// utils/geoUtilsSimple.ts
import type { Feature, FeatureCollection } from "geojson";

// Интерфейс для федерального округа
interface FederalDistrict {
  id: string;
  name: string;
  regionIds: string[]; // ID регионов, входящих в округ
  color?: string;
}

// Список федеральных округов России с регионами
export const federalDistricts: FederalDistrict[] = [
  {
    id: "central",
    name: "Центральный федеральный округ",
    regionIds: ["RU-MOW", "RU-MOS", "RU-BEL", "RU-BRY", "RU-VLA", "RU-VOR", "RU-IVA", "RU-KLU", "RU-KOS", "RU-KRS", "RU-LIP", "RU-ORL", "RU-RYA", "RU-SMO", "RU-TAM", "RU-TVE", "RU-TUL", "RU-YAR"],
    color: "#ff6b6b"
  },
  {
    id: "northwestern",
    name: "Северо-Западный федеральный округ",
    regionIds: ["RU-SPE", "RU-LEN", "RU-ARK", "RU-VLG", "RU-KGD", "RU-KR", "RU-KO", "RU-NEN", "RU-NGR", "RU-PSK", "RU-MUR"],
    color: "#4ecdc4"
  },
  {
    id: "southern",
    name: "Южный федеральный округ",
    regionIds: ["RU-AD", "RU-AST", "RU-VGG", "RU-KL", "RU-KDA", "RU-KRY", "RU-ROS", "RU-SEV"],
    color: "#45b7d1"
  },
  {
    id: "northcaucasus",
    name: "Северо-Кавказский федеральный округ",
    regionIds: ["RU-DA", "RU-IN", "RU-KB", "RU-KC", "RU-SE", "RU-STA"],
    color: "#96ceb4"
  },
  {
    id: "volga",
    name: "Приволжский федеральный округ",
    regionIds: ["RU-BA", "RU-KIR", "RU-ME", "RU-MO", "RU-NIZ", "RU-ORE", "RU-PNZ", "RU-SAM", "RU-SAR", "RU-TA", "RU-UD", "RU-ULY", "RU-CU"],
    color: "#feca57"
  },
  {
    id: "ural",
    name: "Уральский федеральный округ",
    regionIds: ["RU-KGN", "RU-KHM", "RU-SVE", "RU-TYU", "RU-CHE", "RU-YAN"],
    color: "#ff9ff3"
  },
  {
    id: "siberian",
    name: "Сибирский федеральный округ",
    regionIds: ["RU-ALT", "RU-AL", "RU-BU", "RU-ZAB", "RU-IRK", "RU-KEM", "RU-KYA", "RU-NVS", "RU-OMS", "RU-TOM"],
    color: "#54a0ff"
  },
  {
    id: "far eastern",
    name: "Дальневосточный федеральный округ",
    regionIds: ["RU-AMU", "RU-YEV", "RU-KAM", "RU-MAG", "RU-PRI", "RU-SA", "RU-SAK", "RU-KHA", "RU-CHU"],
    color: "#5f27cd"
  }
];

// Упрощенная функция объединения (без внешних зависимостей)
export const createFederalDistrictPolygon = (
  district: FederalDistrict,
  allRegions: FeatureCollection
): Feature | null => {
  try {
    const districtRegions = allRegions.features.filter(feature => 
      district.regionIds.includes(feature.properties?.id as string) ||
      district.regionIds.includes(feature.id as string)
    );

    if (districtRegions.length === 0) {
      return null;
    }

    // Просто берем первый регион как представитель (упрощенный подход)
    // В реальном проекте лучше использовать Turf.js для правильного объединения
    const representativeRegion = districtRegions[0];

    const federalDistrictFeature: Feature = {
      type: "Feature",
      geometry: representativeRegion.geometry,
      properties: {
        id: district.id,
        name: district.name,
        type: "federal_district",
        regionCount: districtRegions.length,
        color: district.color
      },
      id: district.id
    };

    return federalDistrictFeature;
  } catch (error) {
    console.error(`Ошибка при создании округа ${district.name}:`, error);
    return null;
  }
};