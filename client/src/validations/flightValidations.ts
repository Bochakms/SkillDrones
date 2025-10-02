import { z } from 'zod';

// Схема для полета
export const FlightSchema = z.object({
  flightId: z.number().nullable(),
  droneId: z.number().nullable(),
  rawId: z.number().nullable(),
  flightCode: z.string().nullable(),
  droneType: z.string().nullable(),
  droneRegistration: z.string().nullable(),
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
  content: z.array(FlightSchema),
  currentPage: z.number().optional(),
  pageSize: z.number().optional(),
  totalItems: z.number().optional(),
  totalPages: z.number().optional(),
  hasNext: z.boolean(),
  hasPrevious: z.boolean()
});

// Типы
export type FlightData = z.infer<typeof FlightSchema>;
export type FlightsFilterData = z.infer<typeof FlightsFilterSchema>;
export type FlightsResponseData = z.infer<typeof FlightsResponseSchema>;