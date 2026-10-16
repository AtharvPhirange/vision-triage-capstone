package com.example.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_records")
public class ScanRecord {

    @PrimaryKey
    @NonNull
    private String scanId;
    private String patientName;
    private long scanTimestamp;
    private double baselinePupilSize;
    private double minimumPupilSize;
    private long constrictionLatencyMs;
    private String triageResult;
    private boolean isSynced;
    private String scanSource; // "PATIENT" or "TECHNICIAN"

    @ColumnInfo(name = "consultantReviewStatus")
    private String consultantReviewStatus = "PENDING"; // "PENDING", "CONCURRED", "OVERRIDDEN"

    @ColumnInfo(name = "finalClinicalVerdict")
    private String finalClinicalVerdict; // "NORMAL", "SLUGGISH", "UNRESPONSIVE"

    @ColumnInfo(name = "specialistNotes")
    private String specialistNotes = "";

    @ColumnInfo(name = "specialistSignOff")
    private String specialistSignOff = "Dr. A. Sharma (Reg #MMC-84920)";

    @ColumnInfo(name = "triageDisposition")
    private String triageDisposition; // "URGENT_TRAUMA", "TELE_OPHTHALMOLOGY", "ROUTINE_DISCHARGE"

    @ColumnInfo(name = "reviewedTimestamp")
    private long reviewedTimestamp = 0L;

    public ScanRecord(@NonNull String scanId, String patientName, long scanTimestamp,
                      double baselinePupilSize, double minimumPupilSize,
                      long constrictionLatencyMs, String triageResult, boolean isSynced) {
        this.scanId = scanId;
        this.patientName = patientName;
        this.scanTimestamp = scanTimestamp;
        this.baselinePupilSize = baselinePupilSize;
        this.minimumPupilSize = minimumPupilSize;
        this.constrictionLatencyMs = constrictionLatencyMs;
        this.triageResult = triageResult;
        this.isSynced = isSynced;
        this.scanSource = "PATIENT";
    }

    @NonNull
    public String getScanId() { return scanId; }
    public void setScanId(@NonNull String scanId) { this.scanId = scanId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public long getScanTimestamp() { return scanTimestamp; }
    public void setScanTimestamp(long scanTimestamp) { this.scanTimestamp = scanTimestamp; }

    public double getBaselinePupilSize() { return baselinePupilSize; }
    public void setBaselinePupilSize(double baselinePupilSize) { this.baselinePupilSize = baselinePupilSize; }

    public double getMinimumPupilSize() { return minimumPupilSize; }
    public void setMinimumPupilSize(double minimumPupilSize) { this.minimumPupilSize = minimumPupilSize; }

    public long getConstrictionLatencyMs() { return constrictionLatencyMs; }
    public void setConstrictionLatencyMs(long constrictionLatencyMs) { this.constrictionLatencyMs = constrictionLatencyMs; }

    public String getTriageResult() { return triageResult; }
    public void setTriageResult(String triageResult) { this.triageResult = triageResult; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public String getScanSource() { return scanSource; }
    public void setScanSource(String scanSource) { this.scanSource = scanSource; }

    public String getConsultantReviewStatus() { return consultantReviewStatus; }
    public void setConsultantReviewStatus(String consultantReviewStatus) { this.consultantReviewStatus = consultantReviewStatus; }

    public String getFinalClinicalVerdict() { return finalClinicalVerdict; }
    public void setFinalClinicalVerdict(String finalClinicalVerdict) { this.finalClinicalVerdict = finalClinicalVerdict; }

    public String getSpecialistNotes() { return specialistNotes; }
    public void setSpecialistNotes(String specialistNotes) { this.specialistNotes = specialistNotes; }

    public String getSpecialistSignOff() { return specialistSignOff; }
    public void setSpecialistSignOff(String specialistSignOff) { this.specialistSignOff = specialistSignOff; }

    public String getTriageDisposition() { return triageDisposition; }
    public void setTriageDisposition(String triageDisposition) { this.triageDisposition = triageDisposition; }

    public long getReviewedTimestamp() { return reviewedTimestamp; }
    public void setReviewedTimestamp(long reviewedTimestamp) { this.reviewedTimestamp = reviewedTimestamp; }
}