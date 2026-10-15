package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "vision_triage_session_prefs";

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTH_PROVIDER = "auth_provider";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_OFFLINE_STAFF_ID = "offline_staff_id";
    private static final String KEY_USER_PHOTO_URI = "user_photo_uri";
    private static final String KEY_GOOGLE_PHOTO_URL = "google_photo_url";

    private static SharedPreferences getEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences, falling back to private prefs", e);
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static boolean isLoggedIn(Context context) {
        if (context == null) return false;
        return getEncryptedPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void createGoogleSession(Context context, String name, String email, String role) {
        if (context == null) return;
        getEncryptedPrefs(context).edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_AUTH_PROVIDER, "GOOGLE")
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public static void createOfflineSession(Context context, String staffId, String name, String role) {
        if (context == null) return;
        getEncryptedPrefs(context).edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_AUTH_PROVIDER, "OFFLINE")
                .putString(KEY_OFFLINE_STAFF_ID, staffId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, staffId + "@visiontriage.local")
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public static String getUserName(Context context) {
        if (context == null) return "Clinical Staff";
        return getEncryptedPrefs(context).getString(KEY_USER_NAME, "Clinical Staff");
    }

    public static String getUserEmail(Context context) {
        if (context == null) return "clinician@visiontriage.org";
        return getEncryptedPrefs(context).getString(KEY_USER_EMAIL, "clinician@visiontriage.org");
    }

    public static String getUserRole(Context context) {
        if (context == null) return "TECHNICIAN";
        return getEncryptedPrefs(context).getString(KEY_USER_ROLE, "TECHNICIAN");
    }

    public static String getAuthProvider(Context context) {
        if (context == null) return "GOOGLE";
        return getEncryptedPrefs(context).getString(KEY_AUTH_PROVIDER, "GOOGLE");
    }

    public static void saveGooglePhotoUrl(Context context, String url) {
        if (context == null) return;
        getEncryptedPrefs(context).edit()
                .putString(KEY_GOOGLE_PHOTO_URL, url)
                .apply();
    }

    public static String getGooglePhotoUrl(Context context) {
        if (context == null) return null;
        return getEncryptedPrefs(context).getString(KEY_GOOGLE_PHOTO_URL, null);
    }

    public static void setUserPhotoUri(Context context, String uriStr) {
        if (context == null) return;
        getEncryptedPrefs(context).edit()
                .putString(KEY_USER_PHOTO_URI, uriStr)
                .apply();
    }

    public static String getUserPhotoUri(Context context) {
        if (context == null) return null;
        return getEncryptedPrefs(context).getString(KEY_USER_PHOTO_URI, null);
    }

    public static void saveUserProfilePhoto(Context context, String uriOrUrl) {
        setUserPhotoUri(context, uriOrUrl);
    }

    public static String getUserProfilePhoto(Context context) {
        return getUserPhotoUri(context);
    }

    public static void clearSessionAndLogout(Context context) {
        if (context == null) return;
        getEncryptedPrefs(context).edit().clear().apply();
    }
}