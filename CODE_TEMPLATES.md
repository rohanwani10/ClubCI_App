# ClubCI - Quick Start Code Templates

This document provides ready-to-use code templates for implementing the
remaining components.

## 1. EventAdapter (RecyclerView Adapter)

Create file:
`app/src/main/java/com/clubci/dbms_projectapp/adapters/EventAdapter.java`

```java
package com.clubci.dbms_projectapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.models.Event;
import com.clubci.dbms_projectapp.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        // TODO: Add view references from item_event.xml
        TextView tvName, tvDate, tvVenue, tvFee, tvProgress;
        ProgressBar progressBar;
        ImageView ivPoster;

        EventViewHolder(View itemView) {
            super(itemView);
            // TODO: Initialize views with findViewById
        }

        void bind(Event event, OnEventClickListener listener) {
            // TODO: Set event data to views
            // tvName.setText(event.getName());
            // tvDate.setText(DateUtils.formatDate(event.getDateTime()));
            // etc.

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
```

## 2. item_event.xml Layout

Create file: `app/src/main/res/layout/item_event.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="4dp"
    app:cardCornerRadius="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/ivPoster"
            android:layout_width="match_parent"
            android:layout_height="180dp"
            android:scaleType="centerCrop"
            android:background="@color/surface_light" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:id="@+id/tvEventName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:id="@+id/tvDate"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:textSize="14sp"
                android:textColor="@color/text_secondary" />

            <TextView
                android:id="@+id/tvVenue"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="2dp"
                android:textSize="14sp"
                android:textColor="@color/text_secondary" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/tvFee"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:textSize="16sp"
                    android:textColor="@color/accent"
                    android:textStyle="bold" />

                <TextView
                    android:id="@+id/tvProgress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="12sp"
                    android:textColor="@color/text_secondary" />

            </LinearLayout>

            <ProgressBar
                android:id="@+id/progressBar"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp" />

        </LinearLayout>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

## 3. Complete EventListFragment Implementation

Update `EventListFragment.java`:

```java
package com.clubci.dbms_projectapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadEvents() {
        swipeRefresh.setRefreshing(true);

        apiClient.get("/events/all", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                swipeRefresh.setRefreshing(false);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Event> events = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Event event = Event.fromJson(jsonObject);
                        events.add(event);
                    }

                    adapter.setEvents(events);

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error parsing events");
                }
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                showError(error);
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
```

## 4. fragment_event_list.xml Layout

Update the layout file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:padding="8dp" />

    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@android:drawable/ic_input_add"
        app:backgroundTint="@color/accent" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

## 5. QR Code Fragment Implementation

Update `QrCodeFragment.java`:

```java
package com.clubci.dbms_projectapp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.QRCodeGenerator;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

public class QrCodeFragment extends Fragment {

    private ImageView ivQrCode;
    private TextView tvUsername, tvSubtitle;
    private MaterialButton btnRefresh;
    private SharedPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code, container, false);

        initViews(view);
        generateQRCode();

        return view;
    }

    private void initViews(View view) {
        ivQrCode = view.findViewById(R.id.ivQrCode);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        tvUsername.setText(prefsManager.getUsername());

        btnRefresh.setOnClickListener(v -> generateQRCode());
    }

    private void generateQRCode() {
        String username = prefsManager.getUsername();
        String qrData = QRCodeGenerator.generateUserQRData(username, "");
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrData, 300, 300);

        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }
    }
}
```

## 6. fragment_qr_code.xml Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp"
    android:background="@color/background">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        app:cardElevation="8dp"
        app:cardCornerRadius="12dp"
        xmlns:app="http://schemas.android.com/apk/res-auto">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp"
            android:gravity="center">

            <ImageView
                android:id="@+id/ivQrCode"
                android:layout_width="300dp"
                android:layout_height="300dp"
                android:scaleType="fitCenter"
                android:contentDescription="QR Code" />

            <TextView
                android:id="@+id/tvUsername"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="@color/text_primary" />

            <TextView
                android:id="@+id/tvSubtitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:text="Show this code for attendance"
                android:textSize="14sp"
                android:textColor="@color/text_secondary" />

        </LinearLayout>

    </com.google.android.material.card.MaterialCardView>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnRefresh"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="Refresh QR Code"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton" />

</LinearLayout>
```

## Quick Implementation Checklist

- [ ] Sync Gradle files
- [ ] Update API base URL in ApiClient.java
- [ ] Create EventAdapter
- [ ] Create item_event.xml layout
- [ ] Implement EventListFragment (complete code above)
- [ ] Implement QrCodeFragment (complete code above)
- [ ] Create RegisterActivity with form fields
- [ ] Implement EventDetailActivity
- [ ] Test login and navigation flow
- [ ] Implement remaining fragments
- [ ] Add error handling throughout
- [ ] Test with backend API

---

Use these templates as starting points and modify based on your specific
requirements!
