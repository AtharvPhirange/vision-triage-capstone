package com.example.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPatient(Patient patient);

    @Query("SELECT * FROM patients")
    LiveData<List<Patient>> getAllPatients();

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    Patient getPatientById(String patientId);

    @Query("DELETE FROM patients WHERE patientId = :patientId")
    void deletePatientById(String patientId);
}