package com.clubci.dbms_projectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.ApiClient;
import com.clubci.dbms_projectapp.utils.ValidationUtils;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputLayout tilFullName, tilPhone;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etFullName, etPhone;
    private Spinner spinnerBranch, spinnerYear;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupSpinners();
        initListeners();

        apiClient = new ApiClient(this);
    }

    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);

        spinnerBranch = findViewById(R.id.spinnerBranch);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        // Branch spinner
        String[] branches = { "Computer Science (CSE)", "Electronics (ECE)", "Mechanical", "Civil", "Other" };
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(branchAdapter);

        // Year spinner
        String[] years = { "1st Year", "2nd Year", "3rd Year", "4th Year" };
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void initListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptRegister() {
        // Clear previous errors
        clearErrors();

        // Get values
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String branch = getBranchCode(spinnerBranch.getSelectedItem().toString());
        String year = String.valueOf(spinnerYear.getSelectedItemPosition() + 1);

        // Validate
        boolean isValid = true;

        String usernameError = ValidationUtils.validateUsername(username);
        if (usernameError != null) {
            tilUsername.setError(usernameError);
            isValid = false;
        }

        String emailError = ValidationUtils.validateEmail(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }

        String passwordError = ValidationUtils.validatePassword(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }

        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (ValidationUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            isValid = false;
        }

        String phoneError = ValidationUtils.validatePhone(phone);
        if (phoneError != null) {
            tilPhone.setError(phoneError);
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
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("fullName", fullName);
            requestBody.put("branch", branch);
            requestBody.put("phone", phone);
            requestBody.put("year", year);

            JSONArray roles = new JSONArray();
            roles.put("ROLE_USER");
            requestBody.put("roles", roles);

            apiClient.post("/user/register", requestBody, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    showProgress(false);
                    try {
                        JSONObject json = new JSONObject(response);
                        String message = json.getString("message");

                        showSuccess("Registration successful! Please login.");

                        // Navigate to login after delay
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        }, 1500);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Registration successful but invalid response");
                    }
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    showError(error);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            showProgress(false);
            showError("Error creating request");
        }
    }

    private String getBranchCode(String branchName) {
        if (branchName.contains("CSE") || branchName.contains("Computer")) {
            return "CSE";
        } else if (branchName.contains("ECE") || branchName.contains("Electronics")) {
            return "ECE";
        } else if (branchName.contains("Mechanical")) {
            return "Mechanical";
        } else if (branchName.contains("Civil")) {
            return "Civil";
        }
        return "Other";
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFullName.setError(null);
        tilPhone.setError(null);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
