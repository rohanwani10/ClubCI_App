package com.clubci.dbms_projectapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.models.Event;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.DateUtils;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.ValidationUtils;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class CreateEventActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription, etVenue, etMaxParticipants;
    private TextInputEditText etFee, etRequirements, etContactInfo, etPosterUrl;
    private TextInputEditText etEventDate, etRegistrationDeadline;
    private Spinner spinnerType, spinnerStatus;
    private Button btnSave;
    private ProgressBar progressBar;

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;
    private String eventId; // null for create, set for edit
    private Event currentEvent;

    private long selectedEventDateTime = 0;
    private long selectedDeadlineDateTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_create_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiClient = new ApiClient(this);
        prefsManager = SharedPreferencesManager.getInstance(this);

        if (!prefsManager.isAdmin()) {
            showError("Access denied");
            finish();
            return;
        }

        eventId = getIntent().getStringExtra("EVENT_ID");

        initViews();
        setupSpinners();

        if (eventId != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Event");
            }
            loadEventDetails();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Create Event");
            }
        }

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etVenue = findViewById(R.id.etVenue);
        etMaxParticipants = findViewById(R.id.etMaxParticipants);
        etFee = findViewById(R.id.etFee);
        etRequirements = findViewById(R.id.etRequirements);
        etContactInfo = findViewById(R.id.etContactInfo);
        etPosterUrl = findViewById(R.id.etPosterUrl);
        etEventDate = findViewById(R.id.etEventDate);
        etRegistrationDeadline = findViewById(R.id.etRegistrationDeadline);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        etEventDate.setFocusable(false);
        etEventDate.setClickable(true);
        etEventDate.setOnClickListener(v -> showDateTimePicker(true));

        etRegistrationDeadline.setFocusable(false);
        etRegistrationDeadline.setClickable(true);
        etRegistrationDeadline.setOnClickListener(v -> showDateTimePicker(false));
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.event_statuses, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void showDateTimePicker(boolean isEventDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);

                                long timestamp = calendar.getTimeInMillis();
                                String formattedDate = DateUtils.formatDate(calendar.getTime());

                                if (isEventDate) {
                                    selectedEventDateTime = timestamp;
                                    etEventDate.setText(formattedDate);
                                } else {
                                    selectedDeadlineDateTime = timestamp;
                                    etRegistrationDeadline.setText(formattedDate);
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
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
                    populateFields();
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

    private void populateFields() {
        etName.setText(currentEvent.getName());
        etDescription.setText(currentEvent.getDescription());
        etVenue.setText(currentEvent.getVenue());
        etMaxParticipants.setText(String.valueOf(currentEvent.getMaxParticipants()));
        etFee.setText(String.valueOf(currentEvent.getFee()));
        etRequirements.setText(currentEvent.getRequirements());
        etContactInfo.setText(currentEvent.getContactInfo());
        etPosterUrl.setText(currentEvent.getPosterUrl());

        if (currentEvent.getDateTime() != null) {
            selectedEventDateTime = currentEvent.getDateTime().getTime();
            etEventDate.setText(DateUtils.formatDate(currentEvent.getDateTime()));
        }

        if (currentEvent.getRegistrationDeadline() != null) {
            selectedDeadlineDateTime = currentEvent.getRegistrationDeadline().getTime();
            etRegistrationDeadline.setText(DateUtils.formatDate(currentEvent.getRegistrationDeadline()));
        }

        // Set spinner selections
        String[] types = getResources().getStringArray(R.array.event_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(currentEvent.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        String[] statuses = getResources().getStringArray(R.array.event_statuses);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(currentEvent.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
    }

    private void saveEvent() {
        if (!validateFields()) {
            return;
        }

        showProgress(true);

        try {
            JSONObject eventData = new JSONObject();
            eventData.put("eventName", etName.getText().toString().trim());
            eventData.put("description", etDescription.getText().toString().trim());
            eventData.put("eventDate", selectedEventDateTime);
            eventData.put("venue", etVenue.getText().toString().trim());
            eventData.put("registrationDeadline", selectedDeadlineDateTime);
            eventData.put("maxParticipants", Integer.parseInt(etMaxParticipants.getText().toString().trim()));
            eventData.put("eventType", spinnerType.getSelectedItem().toString());
            eventData.put("fee", Double.parseDouble(etFee.getText().toString().trim()));
            eventData.put("posterUrl", etPosterUrl.getText().toString().trim());
            eventData.put("requirements", etRequirements.getText().toString().trim());
            eventData.put("contactInfo", etContactInfo.getText().toString().trim());
            eventData.put("status", spinnerStatus.getSelectedItem().toString());

            String endpoint = (eventId == null) ? "/events/create" : "/events/update/" + eventId;

            ApiClient.ApiCallback callback = new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    showProgress(false);
                    showSuccess(eventId == null ? "Event created successfully" : "Event updated successfully");
                    finish();
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    showError(error);
                }
            };

            if (eventId == null) {
                apiClient.postAuth(endpoint, eventData, callback);
            } else {
                apiClient.putAuth(endpoint, eventData, callback);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showProgress(false);
            showError("Error creating request");
        }
    }

    private boolean validateFields() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String maxParticipants = etMaxParticipants.getText().toString().trim();
        String fee = etFee.getText().toString().trim();
        String contactInfo = etContactInfo.getText().toString().trim();

        if (!ValidationUtils.isValidUsername(name)) {
            etName.setError("Event name is required");
            etName.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (selectedEventDateTime == 0) {
            etEventDate.setError("Event date is required");
            etEventDate.requestFocus();
            return false;
        }

        if (venue.isEmpty()) {
            etVenue.setError("Venue is required");
            etVenue.requestFocus();
            return false;
        }

        if (selectedDeadlineDateTime == 0) {
            etRegistrationDeadline.setError("Registration deadline is required");
            etRegistrationDeadline.requestFocus();
            return false;
        }

        if (selectedDeadlineDateTime >= selectedEventDateTime) {
            etRegistrationDeadline.setError("Deadline must be before event date");
            etRegistrationDeadline.requestFocus();
            return false;
        }

        if (maxParticipants.isEmpty()) {
            etMaxParticipants.setError("Max participants is required");
            etMaxParticipants.requestFocus();
            return false;
        }

        try {
            int max = Integer.parseInt(maxParticipants);
            if (max <= 0) {
                etMaxParticipants.setError("Must be greater than 0");
                etMaxParticipants.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etMaxParticipants.setError("Invalid number");
            etMaxParticipants.requestFocus();
            return false;
        }

        if (fee.isEmpty()) {
            etFee.setError("Fee is required (enter 0 for free events)");
            etFee.requestFocus();
            return false;
        }

        try {
            double feeValue = Double.parseDouble(fee);
            if (feeValue < 0) {
                etFee.setError("Fee cannot be negative");
                etFee.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etFee.setError("Invalid amount");
            etFee.requestFocus();
            return false;
        }

        if (contactInfo.isEmpty()) {
            etContactInfo.setError("Contact info is required");
            etContactInfo.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
