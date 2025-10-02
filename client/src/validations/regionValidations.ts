import z from "zod";

// Схема для региона
export const RegionSchema = z.object({
  regionId: z.number(),
  name: z.string().min(1, 'Название региона не может быть пустым'),
  areaKm2: z.number(),
  geometry: z.string(),
  totalFlights: z.number().nullable(),
});

// Схема для ответа с регионами
export const RegionsResponseSchema = z.array(RegionSchema)

// Типы
export type RegionData = z.infer<typeof RegionSchema>;
export type RegionsResponseData = z.infer<typeof RegionsResponseSchema>;