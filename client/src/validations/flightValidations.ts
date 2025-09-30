import { z } from 'zod';

// Схема для полета
export const FlightSchema = z.object({
  flightId: z.number(),
  droneId: z.number(),
  rawId: z.number(),
  flightCode: z.string(),
  droneType: z.string(),
  droneRegistration: z.string(),
  flightDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Неверный формат даты'),
  departureTime: z.string().regex(/^\d{2}:\d{2}$/, 'Неверный формат времени'),
  arrivalTime: z.string().regex(/^\d{2}:\d{2}$/, 'Неверный формат времени'),
  durationMinutes: z.number().min(0, 'Длительность не может быть отрицательной'),
  departureCoords: z.string(),
  arrivalCoords: z.string(),
  departureRegionId: z.number(),
  arrivalRegionId: z.number(),
  departureRegionName: z.string(),
  arrivalRegionName: z.string(),
  createdAt: z.string().datetime('Неверный формат даты создания'),
  updatedAt: z.string().datetime('Неверный формат даты обновления')
});

// Схема для региона
export const RegionSchema = z.object({
  id: z.number(),
  name: z.string().min(1, 'Название региона не может быть пустым')
});

// Схема для фильтров
export const FlightsFilterSchema = z.object({
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Неверный формат начальной даты').optional(),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Неверный формат конечной даты').optional(),
  regionId: z.number().positive('ID региона должен быть положительным числом').optional()
}).refine(
  (data) => {
    // Проверяем что конечная дата не раньше начальной
    if (data.startDate && data.endDate) {
      return data.endDate >= data.startDate;
    }
    return true;
  },
  {
    message: 'Конечная дата не может быть раньше начальной',
    path: ['endDate']
  }
);

// Схема для ответа с полетами
export const FlightsResponseSchema = z.object({
  success: z.boolean(),
  data: z.array(FlightSchema),
  message: z.string().optional(),
  total: z.number().optional()
});

// Схема для ответа с регионами
export const RegionsResponseSchema = z.object({
  success: z.boolean(),
  data: z.array(RegionSchema),
  message: z.string().optional()
});

// Типы
export type FlightData = z.infer<typeof FlightSchema>;
export type RegionData = z.infer<typeof RegionSchema>;
export type FlightsFilterData = z.infer<typeof FlightsFilterSchema>;
export type FlightsResponseData = z.infer<typeof FlightsResponseSchema>;
export type RegionsResponseData = z.infer<typeof RegionsResponseSchema>;