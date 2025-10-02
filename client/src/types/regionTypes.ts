export interface Region {
  regionId: number;
  name: string;
  areaKm2: number;
  geometry: string;
  totalFlights: number | null;
}