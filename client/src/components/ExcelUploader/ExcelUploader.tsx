// components/ExcelUploader.tsx
import React, { useRef } from "react";
import { useExcelUpload } from "../../hooks/useExcelUpload";

interface ExcelUploaderProps {
  onUploadSuccess?: (response: { success: boolean; message: string }) => void;
  onUploadError?: (error: string) => void;
}

const ExcelUploader: React.FC<ExcelUploaderProps> = ({
  onUploadSuccess,
  onUploadError,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const {
    uploadState,
    handleFileSelect,
    handleDragOver,
    handleDragLeave,
    handleDrop,
    uploadFile,
  } = useExcelUpload();

  const handleUploadClick = async () => {
    const response = await uploadFile();
    if (response && onUploadSuccess) {
      onUploadSuccess(response);
    } else if (onUploadError && uploadState.uploadStatus.includes("Ошибка")) {
      onUploadError(uploadState.uploadStatus);
    }
  };

  const handleDropAreaClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div style={{ maxWidth: "500px", margin: "20px auto" }}>
      <h3 style={{ marginBottom: "20px", textAlign: "center" }}>
        Загрузка Excel файла
      </h3>

      <div
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={handleDropAreaClick}
        style={{
          border: `2px dashed ${uploadState.isDragOver ? "#007bff" : "#ccc"}`,
          borderRadius: "8px",
          padding: "40px 20px",
          textAlign: "center",
          backgroundColor: uploadState.isDragOver ? "#f8f9fa" : "white",
          marginBottom: "20px",
          cursor: "pointer",
        }}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".xls,.xlsx"
          onChange={handleFileSelect}
          style={{ display: "none" }}
          disabled={uploadState.isUploading}
        />

        <div>
          <div style={{ fontSize: "48px", marginBottom: "10px" }}>📎</div>
          <p>Перетащите Excel файл сюда или кликните для выбора</p>
          <small style={{ color: "#6c757d" }}>
            Поддерживаемые форматы: .xls, .xlsx (до 10MB)
          </small>
        </div>
      </div>

      {uploadState.selectedFile && (
        <div
          style={{
            padding: "12px",
            backgroundColor: "#e9f7ef",
            borderRadius: "6px",
            marginBottom: "15px",
          }}
        >
          <strong>📄 {uploadState.selectedFile.name}</strong>
          <div style={{ fontSize: "14px", color: "#155724" }}>
            Размер: {(uploadState.selectedFile.size / 1024 / 1024).toFixed(2)}{" "}
            MB
          </div>
        </div>
      )}

      {uploadState.progress && (
        <div style={{ marginBottom: "15px" }}>
          <div
            style={{
              width: "100%",
              backgroundColor: "#e9ecef",
              borderRadius: "4px",
              overflow: "hidden",
            }}
          >
            <div
              style={{
                width: `${uploadState.progress.percentage}%`,
                height: "8px",
                backgroundColor: "#007bff",
                transition: "width 0.3s ease",
              }}
            />
          </div>
          <div style={{ textAlign: "center", marginTop: "4px" }}>
            {uploadState.progress.percentage}%
          </div>
        </div>
      )}

      <button
        onClick={handleUploadClick}
        disabled={!uploadState.selectedFile || uploadState.isUploading}
        style={{
          width: "100%",
          padding: "12px",
          backgroundColor:
            uploadState.selectedFile && !uploadState.isUploading
              ? "#007bff"
              : "#ccc",
          color: "white",
          border: "none",
          borderRadius: "6px",
          cursor:
            uploadState.selectedFile && !uploadState.isUploading
              ? "pointer"
              : "not-allowed",
        }}
      >
        {uploadState.isUploading ? "⏳ Загрузка..." : "📤 Загрузить на сервер"}
      </button>

      {uploadState.uploadStatus && (
        <div
          style={{
            marginTop: "15px",
            padding: "12px",
            borderRadius: "6px",
            backgroundColor: uploadState.uploadStatus.includes("Ошибка")
              ? "#f8d7da"
              : "#d4edda",
            color: uploadState.uploadStatus.includes("Ошибка")
              ? "#721c24"
              : "#155724",
          }}
        >
          {uploadState.uploadStatus}
        </div>
      )}
    </div>
  );
};

export default ExcelUploader;
