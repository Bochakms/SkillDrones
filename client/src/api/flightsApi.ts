// api/flightsApi.ts
import axios from 'axios';
import { 
  FlightsResponseSchema,
  FlightsFilterSchema,
  type FlightData,
  type FlightsFilterData,
  FlightSchema
} from '../validations/flightValidations';
import { RegionsResponseSchema, type RegionData } from '../validations/regionValidations';

// const API_BASE_URL = 'http://localhost:8081/api';
const API_BASE_URL = '/api';

export const flightsApi = {
  // Получение всех полетов с фильтрацией
  async getFlights(filters?: FlightsFilterData): Promise<FlightData[]> {
    try {
      // Валидация фильтров
      if (filters) {
        FlightsFilterSchema.parse(filters);
      }

      const params = new URLSearchParams();
      
      // Добавляем параметры фильтрации
      if (filters?.startDate) {
        params.append('startDate', filters.startDate);
      }
      if (filters?.endDate) {
        params.append('endDate', filters.endDate);
      }
      if (filters?.regionId) {
        params.append('regionId', filters.regionId.toString());
      }

      const response = await axios.get(`${API_BASE_URL}/processing/flights?${params}`, {
        timeout: 10000,
      });

      console.log(response)

      const validatedResponse = FlightsResponseSchema.parse(response.data);
      
      if (!validatedResponse) {
        throw new Error('Ошибка получения данных');
      }

      return validatedResponse.content;

    } catch (error) {
        console.log(error)
        console.log("используем моковые данные")
        
      // Используем мок данные при ошибке с фильтрацией
      const mockFlights = await this.getMockFlights();
      return this.filterMockFlights(mockFlights, filters);
    }
  },

  // Получение списка регионов
  async getRegions(): Promise<RegionData[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/analysis/regions`, {
        timeout: 5000,
      });

      const validatedResponse = RegionsResponseSchema.parse(response.data);
      
      if (!validatedResponse) {
        throw new Error('Ошибка получения регионов');
      }

      return validatedResponse;

    } catch (error) {
        console.log(error)
      // Мок данные регионов
      return [
        { regionId: 1, name: 'Республика алтай', areaKm2: 145994.71, geometry: "111", totalFlights: 20 },
        { regionId: 2, name: 'Псковсная область', areaKm2: 102950.18, geometry: "111", totalFlights: 50},
        { regionId: 3, name: 'Краснодарский край', areaKm2: 106610.43, geometry: "111", totalFlights: 100 },
        { regionId: 4, name: 'Карачаево-Черкесская Руспублика', areaKm2: 20089.7, geometry: "111", totalFlights: 15 },
        { regionId: 5, name: 'Кабардино-Балкарская Республика', areaKm2: 17283.81, geometry: "111", totalFlights: 10 },
      ];
    }
  },

  // Фильтрация мок данных
  filterMockFlights(flights: FlightData[], filters?: FlightsFilterData): FlightData[] {
    if (!filters) return flights;

    return flights.filter(flight => {
      // Фильтр по дате
      if (filters.startDate && flight.flightDate < filters.startDate) {
        return false;
      }
      if (filters.endDate && flight.flightDate > filters.endDate) {
        return false;
      }

      // Фильтр по региону (ищем в обоих регионах - вылета и прилета)
      if (filters.regionId) {
        if (flight.departureRegionId !== filters.regionId && 
            flight.arrivalRegionId !== filters.regionId) {
          return false;
        }
      }

      return true;
    });
  },

  // Валидация данных полета
  validateFlight(flight: unknown): { isValid: boolean; errors: string[] } {
    try {
      FlightSchema.parse(flight);
      return { isValid: true, errors: [] };
    } catch (error) {
      if (error instanceof Error) {
        return { isValid: false, errors: [error.message] };
      }
      return { isValid: false, errors: ['Неизвестная ошибка валидации'] };
    }
  },

  // Валидация фильтров
  validateFilters(filters: unknown): { isValid: boolean; errors: string[] } {
    try {
      FlightsFilterSchema.parse(filters);
      return { isValid: true, errors: [] };
    } catch (error) {
      if (error instanceof Error) {
        return { isValid: false, errors: [error.message] };
      }
      return { isValid: false, errors: ['Неизвестная ошибка валидации фильтров'] };
    }
  },

  // Мок данные для разработки
  async getMockFlights(): Promise<FlightData[]> {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const mockFlights: FlightData[] = [
      {
        flightId: 1,
        droneId: 101,
        rawId: 1001,
        flightCode: "FL-001-2024",
        droneType: "DJI Mavic 3",
        droneRegistration: "DRN-001",
        flightDate: "2024-01-15",
        departureTime: "08:30",
        arrivalTime: "10:45",
        durationMinutes: 135,
        departureCoords: "55.7558,37.6173",
        arrivalCoords: "55.7602,37.6185",
        departureRegionId: 1,
        arrivalRegionId: 2,
        departureRegionName: "Центральный",
        arrivalRegionName: "Северный",
      },
      {
        flightId: 2,
        droneId: 102,
        rawId: 1002,
        flightCode: "FL-002-2024",
        droneType: "DJI Phantom 4",
        droneRegistration: "DRN-002",
        flightDate: "2024-01-16",
        departureTime: "09:15",
        arrivalTime: "11:30",
        durationMinutes: 135,
        departureCoords: "55.7512,37.6182",
        arrivalCoords: "55.7534,37.6258",
        departureRegionId: 1,
        arrivalRegionId: 3,
        departureRegionName: "Центральный",
        arrivalRegionName: "Южный",
      },
      {
        flightId: 3,
        droneId: 101,
        rawId: 1003,
        flightCode: "FL-003-2024",
        droneType: "DJI Mavic 3",
        droneRegistration: "DRN-001",
        flightDate: "2024-01-20",
        departureTime: "14:00",
        arrivalTime: "15:20",
        durationMinutes: 80,
        departureCoords: "55.7580,37.6150",
        arrivalCoords: "55.7620,37.6200",
        departureRegionId: 2,
        arrivalRegionId: 2,
        departureRegionName: "Северный",
        arrivalRegionName: "Северный",
      },
      {
        flightId: 4,
        droneId: 103,
        rawId: 1004,
        flightCode: "FL-004-2024",
        droneType: "DJI Air 2S",
        droneRegistration: "DRN-003",
        flightDate: "2024-01-25",
        departureTime: "10:00",
        arrivalTime: "12:30",
        durationMinutes: 150,
        departureCoords: "55.7500,37.6200",
        arrivalCoords: "55.7650,37.6300",
        departureRegionId: 3,
        arrivalRegionId: 4,
        departureRegionName: "Южный",
        arrivalRegionName: "Восточный",
      }
    ];

    return mockFlights;
  }
};