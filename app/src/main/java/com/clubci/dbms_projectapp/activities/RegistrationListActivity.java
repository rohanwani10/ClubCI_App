package com.clubci.dbms_projectapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.adapters.AdminRegistrationAdapter;
import com.clubci.dbms_projectapp.models.Registration;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RegistrationListActivity extends AppCompatActivity {

    private TextView tvEventName, tvRegistrationCount, tvAttendedCount, tvEmpty;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupFilter;

    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;
    private AdminRegistrationAdapter adapter;

    private String eventId;
    private String eventName;
    private List<Registration> allRegistrations = new ArrayList<>();
    private List<Registration> filteredRegistrations = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_registration_list);

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

        // Get event ID from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        eventName = getIntent().getStringExtra("EVENT_NAME");

        if (eventId == null || eventId.isEmpty()) {
            showError("Invalid event");
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();

        loadRegistrations();
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvRegistrationCount = findViewById(R.id.tvRegistrationCount);
        tvAttendedCount = findViewById(R.id.tvAttendedCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        // Hide Paid filter for admin simple attendance view
        View chipPaid = chipGroupFilter.findViewById(R.id.chipPaid);
        if (chipPaid != null) {
            chipPaid.setVisibility(View.GONE);
        }

        if (eventName != null && !eventName.isEmpty()) {
            tvEventName.setText(eventName);
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminRegistrationAdapter(filteredRegistrations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = "ALL";
            } else if (checkedId == R.id.chipAttended) {
                currentFilter = "ATTENDED";
            } else if (checkedId == R.id.chipNotAttended) {
                currentFilter = "NOT_ATTENDED";
            }
            filterRegistrations();
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRegistrations);
    }

    private void loadRegistrations() {
        swipeRefreshLayout.setRefreshing(true);
        tvEmpty.setVisibility(View.GONE);

        apiClient.getAuth("/events/" + eventId + "/registrations", new ApiClient.ApiCallback() {
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

                    updateStats();
                    filterRegistrations();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error parsing registrations");
                    updateEmptyState();
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

    private void updateStats() {
        int totalCount = allRegistrations.size();
        int attendedCount = 0;

        for (Registration reg : allRegistrations) {
            if (reg.isAttended()) {
                attendedCount++;
            }
        }

        tvRegistrationCount.setText("Total: " + totalCount);
        tvAttendedCount.setText("Attended: " + attendedCount);
    }

    private void filterRegistrations() {
        filteredRegistrations.clear();

        for (Registration reg : allRegistrations) {
            boolean matches = false;

            switch (currentFilter) {
                case "ALL":
                    matches = true;
                    break;
                case "ATTENDED":
                    matches = reg.isAttended();
                    break;
                case "NOT_ATTENDED":
                    matches = !reg.isAttended();
                    break;
            }

            if (matches) {
                filteredRegistrations.add(reg);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredRegistrations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);

            String message;
            switch (currentFilter) {
                case "ATTENDED":
                    message = "No attended registrations";
                    break;
                case "NOT_ATTENDED":
                    message = "No pending attendance";
                    break;
                default:
                    message = "No registrations found";
            }
            tvEmpty.setText(message);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showCancelConfirmation(Registration registration) {
        // Not used in admin view (no cancel action)
    }

    private void cancelRegistration(Registration registration) {
        // Not used in admin view (no cancel action)
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
