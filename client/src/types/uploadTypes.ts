// types.ts
export interface UploadResponse {
  success: boolean;
  message: string;
  totalRecords?: number;
  processedSuccessfully?: number;
  failed?: number;
  successRate?: string;
}

export interface UploadProgress {
  loaded: number;
  total: number;
  percentage: number;
}

export interface UploadState {
  selectedFile: File | null;
  uploadStatus: string;
  isUploading: boolean;
  isDragOver: boolean;
  progress: UploadProgress | null;
}