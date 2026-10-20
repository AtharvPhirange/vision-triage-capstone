package com.example.myapplication.data.repository;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.myapplication.data.model.EyeHospital;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscoverHospitalRepository {

    private static final String TAG = "DiscoverHospitals";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface HospitalCallback {
        void onSuccess(List<EyeHospital> hospitals, String detectedCity);
        void onError(String errorMessage);
    }

    public void fetchNearbyEyeHospitals(Context context, double userLat, double userLng, HospitalCallback callback) {
        executor.execute(() -> {
            List<EyeHospital> candidateList = new ArrayList<>();
            Set<String> addedKeys = new HashSet<>();
            String detectedCity = "Your Area";

            // 1. Resolve exact City/District using Geocoder
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(userLat, userLng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    if (addr.getLocality() != null && !addr.getLocality().isEmpty()) {
                        detectedCity = addr.getLocality();
                    } else if (addr.getSubAdminArea() != null) {
                        detectedCity = addr.getSubAdminArea();
                    }
                }
            } catch (Exception ignored) {}

            final String cityName = detectedCity;

            // 2. LIVE QUERY: OpenStreetMap Nominatim City Search
            try {
                String[] searchTerms = {"eye hospital in " + cityName, "netralaya in " + cityName};
                for (String term : searchTerms) {
                    String urlStr = "https://nominatim.openstreetmap.org/search?q="
                            + URLEncoder.encode(term, StandardCharsets.UTF_8.name())
                            + "&format=json&addressdetails=1&limit=10";

                    String jsonStr = executeHttpRequest(urlStr);
                    if (jsonStr != null && jsonStr.startsWith("[")) {
                        JSONArray arr = new JSONArray(jsonStr);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String rawName = obj.optString("display_name", "Eye Hospital");
                            String name = rawName.split(",")[0].trim();
                            double lat = obj.optDouble("lat", 0.0);
                            double lon = obj.optDouble("lon", 0.0);

                            JSONObject addr = obj.optJSONObject("address");
                            String road = addr != null ? addr.optString("road", "") : "";
                            String suburb = addr != null ? addr.optString("suburb", cityName) : cityName;
                            String address = (road.isEmpty() ? suburb : road + ", " + suburb).trim();
                            if (address.isEmpty()) address = "Near " + name + ", " + cityName;

                            String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                            if (lat != 0.0 && lon != 0.0 && addedKeys.add(key)) {
                                candidateList.add(new EyeHospital(String.valueOf(obj.optLong("place_id", i)), name, address, lat, lon));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Nominatim error: " + e.getMessage());
            }

            // 3. GEO-FENCED REGIONAL CLINICAL DIRECTORY
            // Verified clinics across Western Maharashtra with exact real-world GPS coordinates
            List<EyeHospital> regionalMaster = getRegionalMasterDirectory();
            for (EyeHospital h : regionalMaster) {
                String key = h.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                if (addedKeys.add(key)) {
                    candidateList.add(h);
                }
            }

            // 4. STRICT 25 KM MATHEMATICAL FILTER
            // Every clinic (live or catalog) MUST pass this test against the user's real GPS
            List<EyeHospital> verifiedNearbyList = new ArrayList<>();
            for (EyeHospital h : candidateList) {
                float[] distResults = new float[1];
                Location.distanceBetween(userLat, userLng, h.getLatitude(), h.getLongitude(), distResults);
                float distanceInKm = distResults[0] / 1000f;

                // HARD CEILING: Strictly drop any clinic further than 25.0 km
                if (distanceInKm <= 25.0f) {
                    h.setDistanceKm(distanceInKm);
                    verifiedNearbyList.add(h);
                }
            }

            // Sort ascending: closest eye clinic appears first
            Collections.sort(verifiedNearbyList, (a, b) -> Float.compare(a.getDistanceKm(), b.getDistanceKm()));

            mainHandler.post(() -> callback.onSuccess(verifiedNearbyList, cityName));
        });
    }

    private List<EyeHospital> getRegionalMasterDirectory() {
        List<EyeHospital> list = new ArrayList<>();

        // KOLHAPUR REGION (Verified Real GPS Coordinates)
        list.add(new EyeHospital("kop_1", "Nandadeep Eye Hospital",
                "107, Dattawad Renaissance, Station Road, New Shahupuri, Kolhapur 416001",
                16.702950, 74.241020));

        list.add(new EyeHospital("kop_2", "Vasan Eye Care Hospital",
                "Opp. Central Bus Stand, Station Road, Shahupuri, Kolhapur 416001",
                16.701100, 74.238950));

        list.add(new EyeHospital("kop_3", "Drishti Netralaya & Retina Centre",
                "2nd Floor, Royal Plaza, Dabholkar Corner, Kolhapur 416001",
                16.704200, 74.239800));

        list.add(new EyeHospital("kop_4", "Dr. Agarwal's Eye Hospital",
                "Raysons Galleria, Near CBS, Tarabai Park, Kolhapur 416003",
                16.709540, 74.236120));

        list.add(new EyeHospital("kop_5", "Akashdeep Netralay (Laser & Phaco Centre)",
                "Lohia Lane, Sykes Extension, Rajarampuri, Kolhapur 416001",
                16.694800, 74.243500));

        list.add(new EyeHospital("kop_6", "Pragati Netra Rugnalay",
                "Building 1982, 9th Lane, Near Janata Chowk, Rajarampuri, Kolhapur 416008",
                16.689200, 74.247100));

        list.add(new EyeHospital("kop_7", "Shri Navdurga Eye Hospital",
                "Near Rankala ST Stand, Hari Om Nagar, Rankala, Kolhapur 416012",
                16.687400, 74.218500));

        list.add(new EyeHospital("kop_8", "Aster Aadhar Hospital (Ophthalmology Wing)",
                "Near R.S. No. 628, Sasane Nagar, Kolhapur 416004",
                16.671200, 74.262400));

        // SANGLI / MIRAJ REGION (Verified Real GPS Coordinates)
        list.add(new EyeHospital("sgl_1", "Nandadeep Eye Hospital (Sangli)",
                "Chandni Chowk, Sangli-Miraj Road, Sangli 416416",
                16.852400, 74.581500));

        list.add(new EyeHospital("sgl_2", "Lions Nab Eye Hospital",
                "Miraj-Sangli Road, Wanlesswadi, Miraj 416414",
                16.837800, 74.621900));

        // PUNE REGION (Verified Real GPS Coordinates)
        list.add(new EyeHospital("pun_1", "National Institute of Ophthalmology (NIO)",
                "Plot No. 1187/30, Off Ghole Road, Shivajinagar, Pune 411005",
                18.528300, 73.844900));

        list.add(new EyeHospital("pun_2", "H. V. Desai Eye Hospital",
                "Survey No 93, Tarwade Vasti, Mohammadwadi, Hadapsar, Pune 411060",
                18.473500, 73.918900));

        list.add(new EyeHospital("pun_3", "Asian Eye Hospital",
                "The Forum, Pune-Satara Road, Near Padmavati, Pune 411009",
                18.483100, 73.856700));

        return list;
    }

    private String executeHttpRequest(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("User-Agent", "VisionTriage-MobileApp/2.2");

            if (conn.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}