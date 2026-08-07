package com.vision.repository;

import com.vision.model.ScanRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanRepository extends JpaRepository<ScanRecord, Long> {

    List<ScanRecord> findByDeviceIdOrderByCapturedAtDesc(String deviceId);
}
