# ClubCI Android App - Complete Implementation Guide

## Project Overview

This document provides the complete implementation guide for the ClubCI Android
application with role-based access for Users and Admins.

## Project Structure

```
app/src/main/java/com/clubci/dbms_projectapp/
├── activities/
│   ├── SplashActivity.java ✓ (Created)
│   ├── LoginActivity.java (Create)
│   ├── RegisterActivity.java (Create)
│   ├── MainActivity.java (Create)
│   ├── EventDetailActivity.java (Create)
│   ├── CreateEventActivity.java (Create)
│   ├── PaymentActivity.java (Create)
│   ├── QrScannerActivity.java (Create)
│   └── RegistrationListActivity.java (Create)
├── fragments/
│   ├── EventListFragment.java (Create)
│   ├── MyRegistrationsFragment.java (Create)
│   ├── QrCodeFragment.java (Create)
│   ├── ProfileFragment.java (Create)
│   └── AnalyticsFragment.java (Create)
├── adapters/
│   ├── EventAdapter.java (Create)
│   ├── RegistrationAdapter.java (Create)
│   └── AttendanceAdapter.java (Create)
├── models/
│   ├── User.java ✓ (Created)
│   ├── Event.java ✓ (Created)
│   ├── Registration.java ✓ (Created)
│   └── Payment.java ✓ (Created)
└── utils/
    ├── ApiClient.java ✓ (Created)
    ├── SharedPreferencesManager.java ✓ (Created)
    ├── QRCodeGenerator.java ✓ (Created)
    ├── ValidationUtils.java ✓ (Created)
    └── DateUtils.java ✓ (Created)
```

## Dependencies (Already Added)

- Material Design Components
- SwipeRefreshLayout
- RecyclerView
- CardView
- CameraX for QR scanning
- ZXing for QR code generation
- Google Play Services Wallet for payments

## API Endpoints Reference

### Authentication

- POST `/user/login` - Body: {username, password} - Returns: {token, role}
- POST `/user/register` - Body: {full user object} - Returns: {message: userId}

### Events

- GET `/events/all` - Get all events
- GET `/events/{eventId}` - Get event details
- POST `/events/create` - Create event (Admin)
- PUT `/events/update/{eventId}` - Update event (Admin)
- DELETE `/events/delete/{eventId}` - Delete event (Admin)
- POST `/events/register/{eventId}` - Register for event
- DELETE `/events/{eventId}/cancel/{username}` - Cancel registration

### Registrations

- GET `/events/user/{username}/registrations` - Get user registrations
- GET `/events/{eventId}/registrations` - Get event registrations (Admin)
- POST `/events/{eventId}/attendance/{username}` - Mark attendance (Admin)
- GET `/events/{eventId}/attended` - Get attended users (Admin)

### Payments

- POST `/payments/log` - Log payment

## Important Implementation Notes

### 1. Update API Base URL

In `ApiClient.java`, update the BASE_URL:

```java
private static final String BASE_URL = "http://your-actual-api-url.com";
```

### 2. Add Internet Permission

Already configured in AndroidManifest.xml

### 3. Camera Permission

Add to AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.camera" android:required="false" />
```

## Key Implementation Patterns

### A. ApiClient Usage Pattern

```java
ApiClient apiClient = new ApiClient(context);
apiClient.post("/user/login", jsonBody, new ApiClient.ApiCallback() {
    @Override
    public void onSuccess(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String token = json.getString("token");
            String role = json.getString("role");
            // Handle success
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String error) {
        // Show error to user
    }
});
```

### B. RecyclerView Adapter Pattern

```java
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
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
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        // Bind views
        void bind(Event event) {
            // Set data to views
        }
    }
}
```

### C. Fragment Creation Pattern

```java
public class EventListFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        initViews(view);
        loadEvents();
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadEvents);
    }

    private void loadEvents() {
        swipeRefresh.setRefreshing(true);
        ApiClient apiClient = new ApiClient(requireContext());
        apiClient.get("/events/all", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // Parse and display events
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                // Show error
            }
        });
    }
}
```

## Layout Patterns

### Material TextInputLayout Pattern

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/tilUsername"
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/username">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/etUsername"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="text"
        android:maxLines="1" />

</com.google.android.material.textfield.TextInputLayout>
```

### CardView Pattern for Events

```xml
<com.google.android.material.card.MaterialCardView
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
            android:scaleType="centerCrop" />

        <TextView
            android:id="@+id/tvEventName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp"
            android:textSize="18sp"
            android:textStyle="bold" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### ProgressBar Usage

```xml
<ProgressBar
    android:id="@+id/progressBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_centerInParent="true"
    android:visibility="gone" />
```

## QR Code Generation Example

```java
String qrData = QRCodeGenerator.generateUserQRData(username, eventId);
Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrData, 300, 300);
imageView.setImageBitmap(qrBitmap);
```

## QR Code Scanning Example

```java
// In QrScannerActivity using CameraX
private void startCamera() {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
        ProcessCameraProvider.getInstance(this);

    cameraProviderFuture.addListener(() -> {
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            bindPreview(cameraProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }, ContextCompat.getMainExecutor(this));
}
```

## Date Handling Example

```java
// Format API date
String displayDate = DateUtils.formatApiDate(apiDateString);

// Create date for API
Date eventDate = DateUtils.combineDateAndTime("2025-01-15", "18:30");
String apiDate = DateUtils.formatToApi(eventDate);
```

## Google Pay Integration Example

```java
// In PaymentActivity
private void initiateGooglePay() {
    PaymentsClient paymentsClient = Wallet.getPaymentsClient(this,
        new Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build());

    // Create payment data request
    // Handle payment result
}
```

## BottomNavigationView Setup

```java
// In MainActivity
BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
bottomNav.setOnItemSelectedListener(item -> {
    Fragment selected = null;

    if (item.getItemId() == R.id.nav_home) {
        selected = new EventListFragment();
    } else if (item.getItemId() == R.id.nav_my_events) {
        selected = new MyRegistrationsFragment();
    } else if (item.getItemId() == R.id.nav_qr_code) {
        selected = new QrCodeFragment();
    } else if (item.getItemId() == R.id.nav_profile) {
        selected = new ProfileFragment();
    }

    if (selected != null) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, selected)
            .commit();
    }

    return true;
});
```

## Error Handling Pattern

```java
private void handleApiError(String error) {
    Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
}
```

## Next Steps to Complete the App

1. **Create LoginActivity**

   - Layout with username, password fields
   - API integration for /user/login
   - Navigate to MainActivity on success

2. **Create RegisterActivity**

   - ScrollView with all user fields
   - Validation using ValidationUtils
   - API integration for /user/register

3. **Create MainActivity**

   - BottomNavigationView
   - Fragment container
   - Handle navigation between fragments
   - Logout menu item

4. **Create EventListFragment**

   - RecyclerView with EventAdapter
   - SwipeRefreshLayout
   - FAB for admin to create events
   - Load events from API

5. **Create EventDetailActivity**

   - Display full event details
   - Register button for users
   - Edit/Delete options for admin
   - Mark attendance button for admin

6. **Create Adapters**

   - EventAdapter for event cards
   - RegistrationAdapter for user registrations
   - AttendanceAdapter for admin attendance list

7. **Create remaining Fragments and Activities**
   - Follow the patterns provided above
   - Use ApiClient for all network calls
   - Handle errors gracefully
   - Show progress indicators

## Testing Checklist

- [ ] Login/Register flow
- [ ] Event listing and details
- [ ] Event registration
- [ ] QR code generation
- [ ] QR code scanning (Admin)
- [ ] Payment flow
- [ ] Analytics display (Admin)
- [ ] Profile editing
- [ ] Logout functionality

## Important Security Notes

1. Always use HTTPS in production
2. Store JWT token securely in SharedPreferences
3. Validate all user inputs
4. Handle authentication errors (401) by redirecting to login
5. Use ProGuard for release builds

## Build and Run

1. Sync Gradle files
2. Update API base URL in ApiClient.java
3. Run on emulator or physical device
4. Test with your backend API

---

Created for ClubCI Technical Club Management System
