package com.clubci.dbms_projectapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.QRCodeGenerator;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerActivity extends AppCompatActivity {

    private static final String TAG = "QrScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private PreviewView previewView;
    private TextView tvInstruction, tvStatus;
    private ProgressBar progressBar;
    private View rootView;

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_qr_scanner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiClient = new ApiClient(this);
        prefsManager = SharedPreferencesManager.getInstance(this);

        // Check admin access
        if (!prefsManager.isAdmin()) {
            showError("Access denied. Admin only.");
            finish();
            return;
        }

        initViews();
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check camera permission
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        previewView = findViewById(R.id.previewView);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.CAMERA },
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                showError("Camera permission is required");
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                showError("Error starting camera");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis use case for QR scanning
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeAnalyzer());

        // Select back camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis);

        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            showError("Failed to start camera");
        }
    }

    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
        private final MultiFormatReader reader = new MultiFormatReader();

        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (isProcessing) {
                image.close();
                return;
            }

            try {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                        data,
                        image.getWidth(),
                        image.getHeight(),
                        0, 0,
                        image.getWidth(),
                        image.getHeight(),
                        false);

                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = reader.decode(bitmap);

                if (result != null) {
                    isProcessing = true;
                    String qrData = result.getText();
                    runOnUiThread(() -> processQRCode(qrData));
                }
            } catch (NotFoundException e) {
                // No QR code found in this frame
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing image", e);
            } finally {
                image.close();
            }
        }
    }

    private void processQRCode(String qrData) {
        Log.d(TAG, "QR Code detected: " + qrData);

        try {
            JSONObject qrJson = QRCodeGenerator.parseQRDataJson(qrData);

            if (qrJson == null) {
                showError("Invalid QR code format");
                resetScanning();
                return;
            }

            String username = qrJson.optString("username", "");
            String eventId = qrJson.optString("eventId", "");
            String eventName = qrJson.optString("eventName", "Unknown Event");
            long timestamp = qrJson.optLong("timestamp", 0);

            if (username.isEmpty() || eventId.isEmpty()) {
                showError("QR code missing required data");
                resetScanning();
                return;
            }

            // Show success message that QR was scanned
            showSuccess("QR Code detected! Verifying...");

            // Show confirmation dialog
            showConfirmationDialog(username, eventName, eventId, timestamp);

        } catch (Exception e) {
            Log.e(TAG, "Error processing QR code", e);
            showError("Error processing QR code");
            resetScanning();
        }
    }

    private void showConfirmationDialog(String username, String eventName, String eventId, long timestamp) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Mark Attendance")
                .setMessage("User: " + username + "\nEvent: " + eventName + "\n\nMark attendance?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    markAttendance(username, eventId, eventName);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    resetScanning();
                })
                .setOnCancelListener(dialog -> resetScanning())
                .show();
    }

    private void markAttendance(String username, String eventId, String eventName) {
        showProgress(true);
        tvStatus.setText("Validating registration...");

        // First, check if user is registered for the event
        apiClient.getAuth("/events/" + eventId + "/registrations", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Log.d(TAG, "Registrations response: " + response);
                    JSONArray registrations = new JSONArray(response);
                    boolean isRegistered = false;

                    // Check if username exists in registrations
                    for (int i = 0; i < registrations.length(); i++) {
                        JSONObject registration = registrations.getJSONObject(i);
                        String registeredUser = registration.optString("username", "");

                        if (registeredUser.equalsIgnoreCase(username)) {
                            isRegistered = true;
                            break;
                        }
                    }

                    if (isRegistered) {
                        // User is registered, proceed to mark attendance
                        proceedWithAttendance(username, eventId, eventName);
                    } else {
                        // User is not registered - allow admin override
                        showProgress(false);
                        tvStatus.setText("User not registered");
                        new MaterialAlertDialogBuilder(QrScannerActivity.this)
                                .setTitle("User not registered")
                                .setMessage("User \"" + username
                                        + "\" is not registered for this event.\n\nMark attendance anyway?")
                                .setPositiveButton("Proceed", (d, w) -> {
                                    showProgress(true);
                                    proceedWithAttendance(username, eventId, eventName);
                                })
                                .setNegativeButton("Cancel", (d, w) -> resetScanning())
                                .show();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing registrations", e);
                    showProgress(false);
                    showError("Error validating registration");
                    resetScanning();
                }
            }

            @Override
            public void onError(String error) {
                // Couldn't validate registration - allow admin to proceed anyway
                showProgress(false);
                tvStatus.setText("Validation failed");
                Log.e(TAG, "Validation error: " + error);
                new MaterialAlertDialogBuilder(QrScannerActivity.this)
                        .setTitle("Validation failed")
                        .setMessage(
                                "Couldn't validate registration (" + error + ").\n\nProceed to mark attendance anyway?")
                        .setPositiveButton("Proceed", (d, w) -> {
                            showProgress(true);
                            proceedWithAttendance(username, eventId, eventName);
                        })
                        .setNegativeButton("Cancel", (d, w) -> resetScanning())
                        .show();
            }
        });
    }

    private void proceedWithAttendance(String username, String eventId, String eventName) {
        tvStatus.setText("Marking attendance...");

        try {
            if (eventId == null || eventId.trim().isEmpty()) {
                showProgress(false);
                showError("Event ID missing in QR. Cannot mark attendance.");
                resetScanning();
                return;
            }

            JSONObject attendanceData = new JSONObject();
            attendanceData.put("username", username);
            attendanceData.put("eventId", eventId);
            attendanceData.put("timestamp", System.currentTimeMillis());

            // Log the request
            Log.d(TAG, "Marking attendance - Endpoint: /events/" + eventId + "/attendance");
            Log.d(TAG, "Attendance data: " + attendanceData.toString());

            // Prefer spec endpoint: /events/{eventId}/attendance/{username}
            String safeUsername;
            try {
                safeUsername = URLEncoder.encode(username, "UTF-8");
            } catch (Exception e) {
                safeUsername = username; // fallback without encoding
            }

            final String endpointV1 = "/events/" + eventId + "/attendance/" + safeUsername;
            final String endpointV0 = "/events/" + eventId + "/attendance"; // legacy

            // Try V1 first
            showInfo("Sending: POST " + endpointV1);
            apiClient.postAuth(endpointV1, attendanceData, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    showProgress(false);
                    Log.d(TAG, "Attendance marked successfully - Response: " + response);
                    showSuccess("✓ Attendance marked for " + username);
                    showSuccessDialog(username, eventName);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "V1 attendance endpoint failed: " + error + "); falling back to legacy endpoint");
                    // Fallback to legacy endpoint
                    showInfo("Retrying: POST " + endpointV0);
                    apiClient.postAuth(endpointV0, attendanceData, new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            showProgress(false);
                            Log.d(TAG, "Attendance marked via legacy endpoint - Response: " + response);
                            showSuccess("✓ Attendance marked for " + username);
                            showSuccessDialog(username, eventName);
                        }

                        @Override
                        public void onError(String error2) {
                            showProgress(false);
                            Log.e(TAG, "Failed to mark attendance (legacy) - Error: " + error2);
                            showError(error2);
                            resetScanning();
                        }
                    });
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating attendance data", e);
            showProgress(false);
            showError("Error creating request");
            resetScanning();
        }
    }

    private void showSuccessDialog(String username, String eventName) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success!")
                .setMessage("Attendance marked for " + username + "\nin " + eventName)
                .setPositiveButton("Scan Next", (dialog, which) -> resetScanning())
                .setNegativeButton("Close", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void resetScanning() {
        isProcessing = false;
        runOnUiThread(() -> {
            tvStatus.setText("Scanning...");
            tvInstruction.setText("Position QR code within the frame");
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            if (rootView != null) {
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccess(String message) {
        runOnUiThread(() -> {
            if (rootView != null) {
                Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(R.color.success))
                        .setTextColor(getColor(android.R.color.white))
                        .show();
            }
        });
    }

    private void showInfo(String message) {
        runOnUiThread(() -> {
            if (rootView != null) {
                Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(R.color.accent))
                        .setTextColor(getColor(android.R.color.white))
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
