package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class PatientSessionManager {

    private static final String PREF_NAME = "patient_session_prefs";
    private static final String KEY_ACTIVE_PATIENT_ID = "active_patient_id";
    public static final String DEFAULT_PATIENT_ID = "PAT-9105";

    public static void setActivePatientId(Context context, String patientId) {
        if (context == null || patientId == null || patientId.trim().isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ACTIVE_PATIENT_ID, patientId.trim()).apply();
    }

    public static String getActivePatientId(Context context) {
        if (context == null) return DEFAULT_PATIENT_ID;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACTIVE_PATIENT_ID, DEFAULT_PATIENT_ID);
    }

    public static void clearActivePatient(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ACTIVE_PATIENT_ID).apply();
    }
}