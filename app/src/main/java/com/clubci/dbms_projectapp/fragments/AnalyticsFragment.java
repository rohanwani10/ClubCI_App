package com.clubci.dbms_projectapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnalyticsFragment extends Fragment {

    private TextView tvTotalEvents, tvTotalRegistrations, tvTotalRevenue, tvAttendanceRate;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new ApiClient(requireContext());
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        if (!prefsManager.isAdmin()) {
            showError("Access denied");
            return;
        }

        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvTotalRegistrations = view.findViewById(R.id.tvTotalRegistrations);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvAttendanceRate = view.findViewById(R.id.tvAttendanceRate);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);

        swipeRefreshLayout.setOnRefreshListener(this::loadAnalytics);

        loadAnalytics();
    }

    private void loadAnalytics() {
        showProgress(true);

        // Load all events to calculate analytics
        apiClient.getAuth("/events/all", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray events = new JSONArray(response);
                    calculateAnalytics(events);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error parsing analytics data");
                }
                showProgress(false);
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                showError(error);
            }
        });
    }

    private void calculateAnalytics(JSONArray events) {
        try {
            int totalEvents = events.length();
            int totalRegistrations = 0;
            double totalRevenue = 0.0;
            int totalAttended = 0;
            int totalParticipants = 0;

            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                int currentParticipants = event.optInt("currentParticipants", 0);
                int attendedCount = event.optInt("attendedCount", 0);
                double fee = event.optDouble("fee", 0.0);

                totalRegistrations += currentParticipants;
                totalRevenue += (currentParticipants * fee);
                totalAttended += attendedCount;
                totalParticipants += currentParticipants;
            }

            // Update UI
            tvTotalEvents.setText(String.valueOf(totalEvents));
            tvTotalRegistrations.setText(String.valueOf(totalRegistrations));
            tvTotalRevenue.setText("₹" + String.format("%.2f", totalRevenue));

            if (totalParticipants > 0) {
                double attendanceRate = (totalAttended * 100.0) / totalParticipants;
                tvAttendanceRate.setText(String.format("%.1f%%", attendanceRate));
            } else {
                tvAttendanceRate.setText("0%");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error calculating analytics");
        }
    }

    private void showProgress(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalytics();
    }
}
