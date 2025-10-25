package com.clubci.dbms_projectapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.activities.QrScannerActivity;
import com.clubci.dbms_projectapp.models.Event;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.QRCodeGenerator;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class QrCodeFragment extends Fragment {

    private ImageView ivQrCode;
    private TextView tvUsername, tvEventName;
    private Spinner spinnerEvents;
    private MaterialButton btnGenerate, btnScanQR;

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;
    private List<Event> userEvents = new ArrayList<>();
    private String selectedEventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new ApiClient(requireContext());
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        ivQrCode = view.findViewById(R.id.ivQrCode);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEventName = view.findViewById(R.id.tvEventName);
        spinnerEvents = view.findViewById(R.id.spinnerEvents);
        btnGenerate = view.findViewById(R.id.btnGenerate);
        btnScanQR = view.findViewById(R.id.btnScanQR);

        tvUsername.setText("User: " + prefsManager.getUsername());

        // Admin-only scan button
        if (prefsManager.isAdmin()) {
            btnScanQR.setVisibility(View.VISIBLE);
            btnScanQR.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), QrScannerActivity.class);
                startActivity(intent);
            });
        } else {
            btnScanQR.setVisibility(View.GONE);
        }

        btnGenerate.setOnClickListener(v -> generateQRCode());

        loadUserEvents();
    }

    private void loadUserEvents() {
        String username = prefsManager.getUsername();
        apiClient.getAuth("/events/user/" + username + "/registrations", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    userEvents.clear();

                    List<String> eventNames = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Event event = new Event();
                        event.setEventId(json.optString("eventId"));
                        event.setName(json.optString("eventName"));
                        userEvents.add(event);
                        eventNames.add(event.getName());
                    }

                    if (!userEvents.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                eventNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEvents.setAdapter(adapter);
                    } else {
                        showError("No registered events found");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error loading events");
                }
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });
    }

    private void generateQRCode() {
        if (userEvents.isEmpty()) {
            showError("No events available");
            return;
        }

        int selectedPosition = spinnerEvents.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= userEvents.size()) {
            showError("Please select an event");
            return;
        }

        Event selectedEvent = userEvents.get(selectedPosition);
        selectedEventId = selectedEvent.getEventId();

        String username = prefsManager.getUsername();
        String eventName = selectedEvent.getName();
        String qrData = QRCodeGenerator.generateUserQRData(username, eventName, selectedEventId);

        try {
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrData, 512, 512);
            ivQrCode.setImageBitmap(qrBitmap);
            ivQrCode.setVisibility(View.VISIBLE);
            tvEventName.setText("Event: " + selectedEvent.getName());
            tvEventName.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error generating QR code");
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserEvents();
    }
}
