// api/excelUpload.ts
import axios, { type AxiosProgressEvent } from 'axios';
import { 
  ExcelUploadSchema, 
  UploadResponseSchema, 
  type UploadResponseData
} from '../validations/uploadValidations';

// Spring Boot обычно на порту 8080
const API_BASE_URL = 'http://localhost:8081/api';

export const excelUploadApi = {
  async uploadFile(
    file: File,
    onProgress?: (progressEvent: AxiosProgressEvent) => void
  ): Promise<UploadResponseData> {
    try {
      // Валидация файла на клиенте
      const validationResult = ExcelUploadSchema.safeParse({ file });
      if (!validationResult.success) {
        const errorMessage = validationResult.error.errors[0]?.message || 'Ошибка валидации файла';
        throw new Error(errorMessage);
      }

      const formData = new FormData();
      formData.append('file', file); // Ключ "file" как в @RequestParam("file")

      const response = await axios.post(
        `${API_BASE_URL}/processing/process-file`, // Полный путь
        formData, 
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          onUploadProgress: onProgress,
          timeout: 30000,
        }
      );

      // Адаптируем ответ сервера под наш тип
      const serverData = response.data;
      const adaptedResponse = {
        success: true,
        message: serverData.message || 'Файл успешно обработан',
        // Дополнительные данные из ответа
        totalRecords: serverData.totalRecords,
        processedSuccessfully: serverData.processedSuccessfully,
        failed: serverData.failed,
        successRate: serverData.successRate
      };

      return UploadResponseSchema.parse(adaptedResponse);

    } catch (error) {
      if (axios.isAxiosError(error)) {
        if (error.response) {
          // Сервер возвращает { "error": "текст", "details": "..." }
          const serverError = error.response.data;
          const errorMessage = serverError?.error || serverError?.details || 'Ошибка сервера';
          throw new Error(errorMessage);
        } else if (error.request) {
          throw new Error('Не удалось соединиться с сервером. Проверьте CORS настройки.');
        }
      }
      
      if (error instanceof Error) {
        throw error;
      }
      
      throw new Error('Неизвестная ошибка при загрузке файла');
    }
  },

  async validateFile(file: File): Promise<{ isValid: boolean; errors: string[] }> {
    const validationResult = ExcelUploadSchema.safeParse({ file });
    
    if (validationResult.success) {
      return { isValid: true, errors: [] };
    } else {
      const errors = validationResult.error.errors.map(err => err.message);
      return { isValid: false, errors };
    }
  }
};