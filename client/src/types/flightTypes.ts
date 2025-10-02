export interface Flight {
  flightId: number;
  droneId: number;
  rawId: number;
  flightCode: string;
  droneType: string;
  droneRegistration: string;
  flightDate: string;
  departureTime: string;
  arrivalTime: string;
  durationMinutes: number;
  departureCoords: string;
  arrivalCoords: string;
  departureRegionId: number;
  arrivalRegionId: number;
  departureRegionName: string;
  arrivalRegionName: string;
  createdAt: string;
  updatedAt: string;
}

export interface FlightsResponse {
  success: boolean;
  data: Flight[];
  message?: string;
  total?: number;
}

export interface FlightsState {
  flights: Flight[];
  loading: boolean;
  error: string | null;
  lastUpdated: Date | null;
}

// Типы для фильтров
export interface FlightsFilter {
  startDate?: string;
  endDate?: string;
  regionId?: number;
}