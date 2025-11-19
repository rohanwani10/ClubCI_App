package com.clubci.dbms_projectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.ValidationUtils;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_login);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initListeners();

        apiClient = new ApiClient(this);
        prefsManager = SharedPreferencesManager.getInstance(this);
    }

    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate
        boolean isValid = true;

        if (ValidationUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            isValid = false;
        }

        if (ValidationUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Make API call
        showProgress(true);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);

            apiClient.post("/user/login", requestBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    showProgress(false);
                    try {
                        JSONObject json = new JSONObject(response);
                        String token = json.getString("token");
                        String role = json.getString("role");

                        // Get additional user info if available
                        String email = json.optString("email", "");
                        String fullName = json.optString("fullName", username);

                        // Save login data
                        prefsManager.saveLoginData(token, username, role, email, fullName);

                        // Navigate to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Invalid response from server");
                    }
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    if (error.contains("401") || error.toLowerCase().contains("invalid")) {
                        showError("Invalid credentials. Please try again.");
                    } else if (error.contains("500")) {
                        showError("Server error. Please try again later.");
                    } else {
                        showError(error);
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            showProgress(false);
            showError("Error creating request");
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
