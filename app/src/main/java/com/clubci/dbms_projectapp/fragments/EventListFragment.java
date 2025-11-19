package com.clubci.dbms_projectapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.activities.CreateEventActivity;
import com.clubci.dbms_projectapp.activities.EventDetailActivity;
import com.clubci.dbms_projectapp.adapters.EventAdapter;
import com.clubci.dbms_projectapp.models.Event;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fab;
    private LinearLayout tvEmpty;
    private EventAdapter adapter;
    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        initViews(view);
        setupRecyclerView();
        loadEvents();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fab = view.findViewById(R.id.fab);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        apiClient = new ApiClient(requireContext());
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        // Show FAB only for admin
        fab.setVisibility(prefsManager.isAdmin() ? View.VISIBLE : View.GONE);

        swipeRefresh.setOnRefreshListener(this::loadEvents);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateEventActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(event -> {
            // Show toast to verify click is working and debug event object
            String eventInfo = "Event object: " + (event != null ? "NOT NULL" : "NULL") + "\n" +
                    "Name: " + (event != null ? event.getName() : "null") + "\n" +
                    "ID: " + (event != null ? event.getEventId() : "null");

            android.util.Log.d("EventListFragment", "Click - " + eventInfo);
            Toast.makeText(getContext(), eventInfo, Toast.LENGTH_LONG).show();

            // Navigate to event detail activity
            if (event != null && event.getEventId() != null) {
                Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: Event ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadEvents() {
        swipeRefresh.setRefreshing(true);
        tvEmpty.setVisibility(View.GONE);

        apiClient.getAuth("/events/all", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                swipeRefresh.setRefreshing(false);
                try {
                    // Log the response to see what we're getting
                    android.util.Log.d("EventListFragment", "API Response: " + response);

                    JSONArray jsonArray = new JSONArray(response);
                    List<Event> events = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        // Log each event object
                        android.util.Log.d("EventListFragment", "Event " + i + ": " + jsonObject.toString());

                        // Log all keys in the JSON object
                        android.util.Log.d("EventListFragment", "Keys: " + jsonObject.keys().toString());

                        // Log specific field checks
                        android.util.Log.d("EventListFragment", "Has 'name': " + jsonObject.has("name"));
                        android.util.Log.d("EventListFragment", "Has 'eventName': " + jsonObject.has("eventName"));
                        android.util.Log.d("EventListFragment", "Has 'title': " + jsonObject.has("title"));
                        if (jsonObject.has("name")) {
                            android.util.Log.d("EventListFragment", "name value: " + jsonObject.getString("name"));
                        }

                        Event event = Event.fromJson(jsonObject);

                        // Log parsed event details
                        android.util.Log.d("EventListFragment",
                                "Parsed Event - ID: " + event.getEventId() + ", Name: " + event.getName());

                        events.add(event);
                    }

                    adapter.setEvents(events);

                    if (events.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error parsing events: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                showError(error);
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents(); // Refresh when returning from other activities
    }
}