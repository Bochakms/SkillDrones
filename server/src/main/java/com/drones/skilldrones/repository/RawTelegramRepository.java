package com.drones.skilldrones.repository;

import com.drones.skilldrones.model.RawTelegram;
import org.springframework.data.repository.CrudRepository;

public interface RawTelegramRepository extends CrudRepository<RawTelegram, Long> {
    long countByProcessingStatus(String processingStatus);
}
