// api/regionsApi.ts
import axios from 'axios';
import { RegionsResponseSchema, type RegionData } from '../validations/regionValidations';

const API_BASE_URL = '/api';

export const regionsApi = {
  // Получение всех регионов
  async getRegions(): Promise<RegionData[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/analysis/regions`, {
        timeout: 5000,
      });

      const validatedResponse = RegionsResponseSchema.parse(response.data);
      
      if (!validatedResponse) {
        throw new Error('Ошибка получения данных регионов');
      }

      return validatedResponse;

    } catch (error) {
      console.log(error);
      console.log("используем моковые данные регионов");
      
      // Мок данные регионов
      return await this.getMockRegions();
    }
  },

  // Мок данные для разработки
  async getMockRegions(): Promise<RegionData[]> {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const mockRegions: RegionData[] = [
      { regionId: 1, name: 'Республика Алтай', areaKm2: 145994.71, geometry: "111", totalFlights: 20 },
      { regionId: 2, name: 'Псковская область', areaKm2: 102950.18, geometry: "111", totalFlights: 50 },
      { regionId: 3, name: 'Краснодарский край', areaKm2: 106610.43, geometry: "111", totalFlights: 100 },
      { regionId: 4, name: 'Карачаево-Черкесская Республика', areaKm2: 20089.7, geometry: "111", totalFlights: 15 },
      { regionId: 5, name: 'Кабардино-Балкарская Республика', areaKm2: 17283.81, geometry: "111", totalFlights: 10 },
    ];

    return mockRegions;
  }
};