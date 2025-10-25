package com.clubci.dbms_projectapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    // TODO: Replace with your actual API base URL
    private static final String BASE_URL = "http://10.228.99.56:3000";

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Context context;

    public ApiClient(Context context) {
        this.context = context.getApplicationContext();
    }

    public interface ApiCallback {
        void onSuccess(String response);

        void onError(String error);
    }

    /**
     * Make GET request
     */
    public void get(String endpoint, ApiCallback callback) {
        makeRequest("GET", endpoint, null, callback);
    }

    /**
     * Make POST request
     */
    public void post(String endpoint, JSONObject body, ApiCallback callback) {
        makeRequest("POST", endpoint, body, callback);
    }

    /**
     * Make PUT request
     */
    public void put(String endpoint, JSONObject body, ApiCallback callback) {
        makeRequest("PUT", endpoint, body, callback);
    }

    /**
     * Make DELETE request
     */
    public void delete(String endpoint, ApiCallback callback) {
        makeRequest("DELETE", endpoint, null, callback);
    }

    /**
     * Make authenticated GET request
     */
    public void getAuth(String endpoint, ApiCallback callback) {
        makeAuthRequest("GET", endpoint, null, callback);
    }

    /**
     * Make authenticated POST request
     */
    public void postAuth(String endpoint, JSONObject body, ApiCallback callback) {
        makeAuthRequest("POST", endpoint, body, callback);
    }

    /**
     * Make authenticated PUT request
     */
    public void putAuth(String endpoint, JSONObject body, ApiCallback callback) {
        makeAuthRequest("PUT", endpoint, body, callback);
    }

    /**
     * Make authenticated DELETE request
     */
    public void deleteAuth(String endpoint, ApiCallback callback) {
        makeAuthRequest("DELETE", endpoint, null, callback);
    }

    /**
     * Generic request method
     */
    private void makeRequest(String method, String endpoint, JSONObject body, ApiCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                if (body != null && (method.equals("POST") || method.equals("PUT"))) {
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

                int responseCode = connection.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    String response = readResponse(connection);
                    mainHandler.post(() -> callback.onSuccess(response));
                } else {
                    String error = readError(connection);
                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Generic authenticated request method
     */
    private void makeAuthRequest(String method, String endpoint, JSONObject body, ApiCallback callback) {
        String token = SharedPreferencesManager.getInstance(context).getToken();

        if (token == null || token.isEmpty()) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                android.util.Log.d("ApiClient", "Request: " + method + " " + url);
                if (body != null) {
                    android.util.Log.d("ApiClient", "Body: " + body.toString());
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                if (body != null && (method.equals("POST") || method.equals("PUT"))) {
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    os.close();
                }

                int responseCode = connection.getResponseCode();
                android.util.Log.d("ApiClient", "Response code: " + responseCode);

                if (responseCode >= 200 && responseCode < 300) {
                    String response = readResponse(connection);
                    android.util.Log.d("ApiClient", "Response: " + response);
                    mainHandler.post(() -> callback.onSuccess(response));
                } else {
                    String error = readError(connection);
                    android.util.Log.e("ApiClient", "Error response: " + error);
                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                android.util.Log.e("ApiClient", "Exception: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Read success response
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    /**
     * Read error response
     */
    private String readError(HttpURLConnection connection) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();

            // Try to parse error message from JSON
            try {
                JSONObject errorJson = new JSONObject(error.toString());
                if (errorJson.has("message")) {
                    return errorJson.getString("message");
                } else if (errorJson.has("error")) {
                    return errorJson.getString("error");
                }
            } catch (JSONException ignored) {
            }

            return error.toString();
        } catch (Exception e) {
            return "Error: " + connection.getResponseCode();
        }
    }

    /**
     * Build query string from parameters
     */
    public static String buildQueryString(String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must be key-value pairs");
        }

        StringBuilder query = new StringBuilder("?");
        for (int i = 0; i < params.length; i += 2) {
            if (i > 0) {
                query.append("&");
            }
            query.append(params[i]).append("=").append(params[i + 1]);
        }
        return query.toString();
    }
}
