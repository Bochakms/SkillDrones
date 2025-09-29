import { z } from 'zod';

const MAX_SIZE = 50;

export const ExcelUploadSchema = z.object({
  file: z.instanceof(File)
    .refine((file) => {
      const allowedTypes = [
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-excel.sheet.macroEnabled.12'
      ];
      return allowedTypes.includes(file.type);
    }, 'Недопустимый формат файла. Разрешены только .xls, .xlsx')
    .refine((file) => file.size <= MAX_SIZE * 1024 * 1024, `Файл не должен превышать ${MAX_SIZE} МБ`)
});

export const UploadResponseSchema = z.object({
  success: z.boolean(),
  message: z.string(),
  totalRecords: z.number().optional(),
  processedSuccessfully: z.number().optional(),
  failed: z.number().optional(),
  successRate: z.string().optional()
});

export type UploadResponseData = z.infer<typeof UploadResponseSchema>;