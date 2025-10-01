import React, { useRef } from "react";
import { useExcelUpload } from "../../hooks/useExcelUpload";
import styles from "./ExcelUploader.module.scss";

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
    resetUpload,
  } = useExcelUpload();

  const handleUploadClick = async () => {
    const response = await uploadFile();
    if (response && response.success && onUploadSuccess) {
      onUploadSuccess(response);
    } else if (onUploadError && uploadState.uploadStatus.includes("Ошибка")) {
      onUploadError(uploadState.uploadStatus);
    }
  };

  const handleDropAreaClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    fileInputRef.current?.click();
  };

  const handleUploadAnother = () => {
    resetUpload();
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const isSuccess =
    uploadState.uploadStatus.includes("успешно") ||
    uploadState.uploadStatus.includes("✅");

  // Получаем данные из ответа сервера
  const uploadResponse = uploadState.uploadResponse;

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>Загрузка Excel файла</h3>

      {!isSuccess && (
        <>
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={handleDropAreaClick}
            className={
              uploadState.isDragOver ? styles.dropAreaActive : styles.dropArea
            }
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".xls,.xlsx"
              onChange={handleFileSelect}
              className={styles.hidden}
              disabled={uploadState.isUploading}
            />

            <div>
              <div className={styles.fileIcon}>📎</div>
              <p>Перетащите Excel файл сюда или кликните для выбора</p>
              <small style={{ color: "#6c757d" }}>
                Поддерживаемые форматы: .xls, .xlsx (до 10MB)
              </small>
            </div>
          </div>

          {uploadState.selectedFile && (
            <div className={styles.fileInfo}>
              <div className={styles.fileName}>
                📄 {uploadState.selectedFile.name}
              </div>
              <div className={styles.fileSize}>
                Размер:{" "}
                {(uploadState.selectedFile.size / 1024 / 1024).toFixed(2)} MB
              </div>
            </div>
          )}

          {uploadState.progress && (
            <div className={styles.progressContainer}>
              <div className={styles.progressBar}>
                <div
                  className={styles.progressFill}
                  style={{ width: `${uploadState.progress.percentage}%` }}
                />
              </div>
              <div className={styles.progressText}>
                {uploadState.progress.percentage}%
              </div>
            </div>
          )}

          <button
            onClick={handleUploadClick}
            disabled={!uploadState.selectedFile || uploadState.isUploading}
            className={styles.uploadButton}
          >
            {uploadState.isUploading
              ? "⏳ Загрузка..."
              : "📤 Загрузить на сервер"}
          </button>
        </>
      )}

      {uploadState.uploadStatus && (
        <div className={isSuccess ? styles.statusSuccess : styles.statusError}>
          {uploadState.uploadStatus}

          {/* Блок с результатами загрузки */}
          {isSuccess && uploadResponse && (
            <div className={styles.resultsContainer}>
              <div className={styles.resultsTitle}>Результаты обработки:</div>
              <div className={styles.resultsGrid}>
                <div className={styles.resultItem}>
                  <div className={styles.resultLabel}>Всего записей</div>
                  <div className={styles.resultValue}>
                    {uploadResponse.totalRecords || 0}
                  </div>
                </div>
                <div className={styles.resultItem}>
                  <div className={styles.resultLabel}>Успешно</div>
                  <div
                    className={styles.resultValue}
                    style={{ color: "#28a745" }}
                  >
                    {uploadResponse.processedSuccessfully || 0}
                  </div>
                </div>
                <div className={styles.resultItem}>
                  <div className={styles.resultLabel}>Ошибки</div>
                  <div
                    className={styles.resultValue}
                    style={{
                      color: uploadResponse.failed ? "#dc3545" : "#28a745",
                    }}
                  >
                    {uploadResponse.failed || 0}
                  </div>
                </div>
                <div className={styles.resultItem}>
                  <div className={styles.resultLabel}>Успешность</div>
                  <div
                    className={styles.resultValue}
                    style={{ color: "#007bff" }}
                  >
                    {uploadResponse.successRate || "100%"}
                  </div>
                </div>
              </div>
            </div>
          )}

          {isSuccess && (
            <div className={styles.successActions}>
              <button
                onClick={handleUploadAnother}
                className={styles.anotherButton}
              >
                📎 Загрузить другой файл
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ExcelUploader;
