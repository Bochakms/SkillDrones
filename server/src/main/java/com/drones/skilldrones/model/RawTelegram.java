package com.drones.skilldrones.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raw_telegrams")
public class RawTelegram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String center;
    
    @Column(name = "shr_raw_text", columnDefinition = "TEXT")
    private String shrRawText;
    
    @Column(name = "dep_raw_text", columnDefinition = "TEXT")
    private String depRawText;
    
    @Column(name = "arr_raw_text", columnDefinition = "TEXT")
    private String arrRawText;
    
    private String fileName;
    private LocalDateTime processedAt;
    private String processingStatus;

    @OneToMany(mappedBy = "rawTelegram", fetch = FetchType.LAZY)
    private List<Flight> flights = new ArrayList<>();
    
    // ДОБАВЬТЕ ЭТИ СЕТТЕРЫ:
    public void setId(Long id) { this.id = id; }
    public void setCenter(String center) { this.center = center; }
    public void setShrRawText(String shrRawText) { this.shrRawText = shrRawText; }
    public void setDepRawText(String depRawText) { this.depRawText = depRawText; }
    public void setArrRawText(String arrRawText) { this.arrRawText = arrRawText; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }
    
    // И ДОБАВЬТЕ ГЕТТЕРЫ:
    public Long getId() { return id; }
    public String getCenter() { return center; }
    public String getShrRawText() { return shrRawText; }
    public String getDepRawText() { return depRawText; }
    public String getArrRawText() { return arrRawText; }
    public String getFileName() { return fileName; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getProcessingStatus() { return processingStatus; }
}
