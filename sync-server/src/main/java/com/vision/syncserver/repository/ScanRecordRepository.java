package com.vision.syncserver.repository;

import com.vision.syncserver.model.ScanRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanRecordRepository extends JpaRepository<ScanRecordEntity, String> {
    List<ScanRecordEntity> findByPatientName(String patientName);
}