package com.clubci.dbms_projectapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.adapters.RegistrationAdapter;
import com.clubci.dbms_projectapp.models.Registration;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MyRegistrationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmpty;
    private ChipGroup chipGroupFilter;

    private RegistrationAdapter adapter;
    private List<Registration> allRegistrations = new ArrayList<>();
    private List<Registration> filteredRegistrations = new ArrayList<>();

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;
    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_registrations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new ApiClient(requireContext());
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);

        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();

        loadRegistrations();
    }

    private void setupRecyclerView() {
        adapter = new RegistrationAdapter(filteredRegistrations, this::onCancelRegistration);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }

            int checkedId = checkedIds.get(0);
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentFilter = chip.getText().toString().toUpperCase();
                filterRegistrations();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRegistrations);
    }

    private void loadRegistrations() {
        swipeRefreshLayout.setRefreshing(true);

        String username = prefsManager.getUsername();
        apiClient.getAuth("/events/user/" + username + "/registrations", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                swipeRefreshLayout.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    allRegistrations.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Registration registration = Registration.fromJson(json);
                        allRegistrations.add(registration);
                    }

                    filterRegistrations();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error parsing registrations");
                }
            }

            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                showError(error);
                updateEmptyState();
            }
        });
    }

    private void filterRegistrations() {
        filteredRegistrations.clear();

        for (Registration reg : allRegistrations) {
            boolean matches = false;

            switch (currentFilter) {
                case "ALL":
                    matches = true;
                    break;
                case "PENDING":
                    matches = "PENDING".equalsIgnoreCase(reg.getPaymentStatus());
                    break;
                case "COMPLETED":
                    matches = "COMPLETED".equalsIgnoreCase(reg.getPaymentStatus());
                    break;
                case "ATTENDED":
                    matches = reg.isAttended();
                    break;
            }

            if (matches) {
                filteredRegistrations.add(reg);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void onCancelRegistration(Registration registration) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel Registration")
                .setMessage(
                        "Are you sure you want to cancel your registration for " + registration.getEventName() + "?")
                .setPositiveButton("Cancel Registration", (dialog, which) -> cancelRegistration(registration))
                .setNegativeButton("Keep Registration", null)
                .show();
    }

    private void cancelRegistration(Registration registration) {
        String username = prefsManager.getUsername();
        apiClient.deleteAuth("/events/" + registration.getEventId() + "/cancel/" + username,
                new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        showSuccess("Registration cancelled successfully");
                        loadRegistrations(); // Refresh list
                    }

                    @Override
                    public void onError(String error) {
                        showError(error);
                    }
                });
    }

    private void updateEmptyState() {
        if (filteredRegistrations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);

            String message;
            switch (currentFilter) {
                case "PENDING":
                    message = "No pending registrations";
                    break;
                case "COMPLETED":
                    message = "No completed registrations";
                    break;
                case "ATTENDED":
                    message = "No attended events";
                    break;
                default:
                    message = "No registrations yet\nRegister for events to see them here";
            }
            tvEmpty.setText(message);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Public method to refresh registrations data
     * Can be called from other activities/fragments
     */
    public void refreshData() {
        loadRegistrations();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRegistrations();
    }
}
