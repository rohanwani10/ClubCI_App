package com.clubci.dbms_projectapp.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    // Google Pay (GPay) package name
    private static final String GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user";

    // TODO: Replace with your merchant's UPI VPA and name
    private static final String MERCHANT_UPI_ID = "rohan10wani@oksbi"; // e.g., clubci@oksbi
    private static final String MERCHANT_NAME = "ClubCI";

    private String eventId;
    private double amount;
    private String username;

    private ApiClient apiClient;
    private SharedPreferencesManager prefs;

    private ActivityResultLauncher<Intent> gpayLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        prefs = SharedPreferencesManager.getInstance(this);
        apiClient = new ApiClient(this);
        username = prefs.getUsername();

        Intent i = getIntent();
        eventId = i.getStringExtra("EVENT_ID");
        amount = i.getDoubleExtra("AMOUNT", 0.0);

        if (TextUtils.isEmpty(eventId) || amount <= 0) {
            Toast.makeText(this, "Invalid payment request", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Register launcher to receive UPI/GPay result
        gpayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleGpayResult);

        // Kick off payment immediately
        launchGPay();
    }

    private void launchGPay() {
        Uri upiUri = buildUpiUri(
                MERCHANT_UPI_ID,
                MERCHANT_NAME,
                amount,
                // Use a unique transaction reference id
                "EVT-" + eventId+ "-" + System.currentTimeMillis(),
                // Note shown to payer
                "Payment for event " + eventId + " by " + username);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(upiUri);
        intent.setPackage(GPAY_PACKAGE);

        if (!isAppInstalled(GPAY_PACKAGE)) {
            Toast.makeText(this, "Google Pay not installed", Toast.LENGTH_LONG).show();
            // Optional: fall back to any UPI app
            Intent chooser = new Intent(Intent.ACTION_VIEW, upiUri);
            try {
                startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No UPI app found", Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }

        try {
            gpayLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to open Google Pay", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handleGpayResult(ActivityResult result) {
        Intent data = result.getData();
        String response = null;
        if (data != null && data.getStringExtra("response") != null) {
            response = data.getStringExtra("response");
        } else if (data != null && data.getDataString() != null) {
            response = data.getDataString();
        }

        Map<String, String> parsed = parseUpiResponse(response);
        String status = valueCaseInsensitive(parsed, "Status");
        String txnId = valueCaseInsensitive(parsed, "txnId");
        String approvalRef = valueCaseInsensitive(parsed, "ApprovalRefNo");
        if (TextUtils.isEmpty(approvalRef)) {
            approvalRef = valueCaseInsensitive(parsed, "txnRef");
        }

        boolean success = status != null && status.equalsIgnoreCase("SUCCESS");

        // Send result to backend
        postPaymentToBackend(success, txnId, approvalRef, status, response);
    }

    private void postPaymentToBackend(boolean success, @Nullable String txnId, @Nullable String approvalRef,
            @Nullable String status, @Nullable String rawResponse) {
        try {
            JSONObject body = new JSONObject();
            body.put("eventId", eventId);
            body.put("username", username);
            body.put("amount", amount);
            body.put("provider", "GPay");
            body.put("status", success ? "COMPLETED" : "FAILED");
            if (txnId != null)
                body.put("transactionId", txnId);
            if (approvalRef != null)
                body.put("approvalRef", approvalRef);
            if (status != null)
                body.put("upiStatus", status);
            if (rawResponse != null)
                body.put("raw", rawResponse);

            // Assumed endpoint: adjust if your backend expects a different route
            String endpoint = "/events/" + eventId + "/payments";

            apiClient.postAuth(endpoint, body, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(PaymentActivity.this,
                            success ? "Payment successful" : "Payment recorded as failed",
                            Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(PaymentActivity.this,
                            "Failed to record payment: " + error,
                            Toast.LENGTH_LONG).show();
                    // Still finish; or keep user here to retry depending on UX
                    finish();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error forming payment data", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private Uri buildUpiUri(String upiId, String name, double amount, String txnRef, String note) {
        // Format amount to two decimal places
        String amt = String.format(Locale.US, "%.2f", amount);

        // Build UPI deep link
        Uri.Builder builder = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId) // payee address
                .appendQueryParameter("pn", name) // payee name
                .appendQueryParameter("tr", txnRef) // transaction reference
                .appendQueryParameter("tn", note) // transaction note
                .appendQueryParameter("am", amt) // amount
                .appendQueryParameter("cu", "INR"); // currency

        return builder.build();
    }

    private Map<String, String> parseUpiResponse(@Nullable String response) {
        Map<String, String> map = new HashMap<>();
        if (response == null)
            return map;

        // Response can be like:
        // "txnId=12345&responseCode=00&Status=SUCCESS&txnRef=ABCD&ApprovalRefNo=XYZ"
        String[] pairs = response.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length >= 1) {
                String key = kv[0];
                String value = kv.length > 1 ? kv[1] : "";
                map.put(key, value);
            }
        }
        return map;
    }

    @Nullable
    private String valueCaseInsensitive(Map<String, String> map, String wantedKey) {
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(wantedKey)) {
                return e.getValue();
            }
        }
        return null;
    }
}
