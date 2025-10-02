import React from "react";
import ExcelUploader from "../../components/ExcelUploader/ExcelUploader";
import type { UploadResponse } from "../../types/uploadTypes";

const UploadPage: React.FC = () => {
  const handleUploadSuccess = (response: UploadResponse) => {
    console.log("Файл успешно загружен:", response);
    // Дополнительные действия после успешной загрузки
  };

  const handleUploadError = (error: string) => {
    console.error("Ошибка загрузки:", error);
    // Обработка ошибок
  };

  return (
    <div style={{ padding: "20px" }}>
      <ExcelUploader
        onUploadSuccess={handleUploadSuccess}
        onUploadError={handleUploadError}
      />
    </div>
  );
};

export default UploadPage;
