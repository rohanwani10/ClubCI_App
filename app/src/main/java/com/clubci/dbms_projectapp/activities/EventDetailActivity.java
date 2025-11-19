package com.clubci.dbms_projectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.models.Event;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.DateUtils;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvType, tvDate, tvVenue, tvDeadline;
    private TextView tvFee, tvRequirements, tvContact, tvParticipants, tvAttended;
    private Button btnRegister, btnViewQR, btnMarkAttendance, btnViewRegistrations;
    private ProgressBar progressBar;

    private String eventId;
    private Event currentEvent;
    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_event_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Event Details");
        }

        apiClient = new ApiClient(this);
        prefsManager = SharedPreferencesManager.getInstance(this);

        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            showError("Event ID not found");
            finish();
            return;
        }

        initViews();
        loadEventDetails();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvType = findViewById(R.id.tvType);
        tvDate = findViewById(R.id.tvDate);
        tvVenue = findViewById(R.id.tvVenue);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvFee = findViewById(R.id.tvFee);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvContact = findViewById(R.id.tvContact);
        tvParticipants = findViewById(R.id.tvParticipants);
        tvAttended = findViewById(R.id.tvAttended);

        btnRegister = findViewById(R.id.btnRegister);
        btnViewQR = findViewById(R.id.btnViewQR);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnViewRegistrations = findViewById(R.id.btnViewRegistrations);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerForEvent());
        btnViewQR.setOnClickListener(v -> viewQRCode());
        btnMarkAttendance.setOnClickListener(v -> startActivity(new Intent(this, QrScannerActivity.class)));
        btnViewRegistrations.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationListActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            if (currentEvent != null) {
                intent.putExtra("EVENT_NAME", currentEvent.getName());
            }
            startActivity(intent);
        });
    }

    private void loadEventDetails() {
        showProgress(true);

        apiClient.getAuth("/events/" + eventId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                showProgress(false);
                try {
                    JSONObject json = new JSONObject(response);
                    currentEvent = Event.fromJson(json);
                    displayEventDetails();
                    checkRegistrationStatus();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError("Error parsing event details");
                }
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                showError(error);
            }
        });
    }

    private void displayEventDetails() {
        tvName.setText(currentEvent.getName());
        tvDescription.setText(currentEvent.getDescription());
        tvType.setText(currentEvent.getType());

        if (currentEvent.getDateTime() != null) {
            tvDate.setText(DateUtils.formatDate(currentEvent.getDateTime()));
        }

        tvVenue.setText(currentEvent.getVenue());

        if (currentEvent.getRegistrationDeadline() != null) {
            tvDeadline.setText(DateUtils.formatDate(currentEvent.getRegistrationDeadline()));
        }

        if (currentEvent.getFee() == 0) {
            tvFee.setText("FREE");
        } else {
            tvFee.setText("₹" + String.format("%.2f", currentEvent.getFee()));
        }

        tvRequirements.setText(currentEvent.getRequirements());
        tvContact.setText(currentEvent.getContactInfo());
        tvParticipants.setText(currentEvent.getCurrentParticipants() + " / " + currentEvent.getMaxParticipants());

        if (prefsManager.isAdmin()) {
            tvAttended.setVisibility(View.VISIBLE);
            tvAttended.setText("Attended: " + currentEvent.getAttendedCount());
            btnMarkAttendance.setVisibility(View.VISIBLE);
            btnViewRegistrations.setVisibility(View.VISIBLE);
        }
    }

    private void checkRegistrationStatus() {
        String username = prefsManager.getUsername();
        apiClient.getAuth("/events/user/" + username + "/registrations", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject reg = jsonArray.getJSONObject(i);
                        if (eventId.equals(reg.getString("eventId"))) {
                            isRegistered = true;
                            btnRegister.setVisibility(View.GONE);
                            btnViewQR.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                // Ignore error, assume not registered
            }
        });
    }

    private void registerForEvent() {
        if (currentEvent.isFull()) {
            showError("Event is full");
            return;
        }

        if (!currentEvent.isRegistrationOpen()) {
            showError("Registration closed");
            return;
        }

        showProgress(true);

        try {
            JSONObject requestBody = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("username", prefsManager.getUsername());
            requestBody.put("user", user);

            apiClient.postAuth("/events/register/" + eventId, requestBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    showProgress(false);
                    showSuccess("Registration successful!");

                    // Check if event has fee
                    if (currentEvent.getFee() > 0) {
                        // Navigate to payment
                        Intent intent = new Intent(EventDetailActivity.this, PaymentActivity.class);
                        intent.putExtra("EVENT_ID", eventId);
                        intent.putExtra("AMOUNT", currentEvent.getFee());
                        startActivity(intent);
                    }

                    loadEventDetails(); // Refresh
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    showError(error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            showProgress(false);
            showError("Error creating request");
        }
    }

    private void viewQRCode() {
        // Navigate to QR fragment or show QR dialog
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SHOW_QR", true);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (prefsManager.isAdmin()) {
            getMenuInflater().inflate(R.menu.event_detail_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        showProgress(true);

        apiClient.deleteAuth("/events/delete/" + eventId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                showProgress(false);
                showSuccess("Event deleted successfully");
                finish();
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                showError(error);
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
