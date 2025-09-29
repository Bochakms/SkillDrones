// hooks/useExcelUpload.ts
import { useState, useCallback } from 'react';
import { type AxiosProgressEvent } from 'axios';
import { excelUploadApi } from '../api/excelUpload';
import type { UploadState, UploadResponse } from '../types/uploadTypes';

export const useExcelUpload = () => {
  const [uploadState, setUploadState] = useState<UploadState>({
    selectedFile: null,
    uploadStatus: '',
    isUploading: false,
    isDragOver: false,
    progress: null
  });

  const validateAndSetFile = useCallback(async (file: File): Promise<boolean> => {
    const validation = await excelUploadApi.validateFile(file);
    
    if (validation.isValid) {
      setUploadState(prev => ({
        ...prev,
        selectedFile: file,
        uploadStatus: `Готов к загрузке: ${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)`,
        progress: null
      }));
      return true;
    } else {
      setUploadState(prev => ({
        ...prev,
        uploadStatus: `Ошибка: ${validation.errors[0]}`,
        selectedFile: null
      }));
      return false;
    }
  }, []);

  const handleFileSelect = useCallback(async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      await validateAndSetFile(file);
    }
  }, [validateAndSetFile]);

  const handleDragOver = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setUploadState(prev => ({ ...prev, isDragOver: true }));
  }, []);

  const handleDragLeave = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setUploadState(prev => ({ ...prev, isDragOver: false }));
  }, []);

  const handleDrop = useCallback(async (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setUploadState(prev => ({ ...prev, isDragOver: false }));
    
    const files = event.dataTransfer.files;
    if (files.length > 0) {
      await validateAndSetFile(files[0]);
    }
  }, [validateAndSetFile]);

  const uploadFile = useCallback(async (): Promise<UploadResponse | null> => {
    if (!uploadState.selectedFile) {
      setUploadState(prev => ({ ...prev, uploadStatus: 'Ошибка: Файл не выбран' }));
      return null;
    }

    setUploadState(prev => ({ ...prev, isUploading: true, uploadStatus: 'Подготовка к загрузке...' }));

    try {
      const onProgress = (progressEvent: AxiosProgressEvent) => {
        const total = progressEvent.total || 0;
        if (total > 0) {
          const percentage = Math.round((progressEvent.loaded * 100) / total);
          setUploadState(prev => ({
            ...prev,
            progress: {
              loaded: progressEvent.loaded,
              total: total,
              percentage
            },
            uploadStatus: `Загрузка: ${percentage}%`
          }));
        }
      };

      const response = await excelUploadApi.uploadFile(uploadState.selectedFile, onProgress);
      
      setUploadState(prev => ({
        ...prev,
        uploadStatus: '✅ Файл успешно загружен',
        selectedFile: null,
        isUploading: false,
        progress: null
      }));

      return response;

    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Неизвестная ошибка';
      setUploadState(prev => ({
        ...prev,
        uploadStatus: `❌ Ошибка загрузки: ${errorMessage}`,
        isUploading: false,
        progress: null
      }));
      return null;
    }
  }, [uploadState.selectedFile]);

  const resetUpload = useCallback(() => {
    setUploadState({
      selectedFile: null,
      uploadStatus: '',
      isUploading: false,
      isDragOver: false,
      progress: null
    });
  }, []);

  return {
    uploadState,
    handleFileSelect,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    uploadFile,
    resetUpload
  };
};