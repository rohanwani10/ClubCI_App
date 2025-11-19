package com.clubci.dbms_projectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_splash);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(this);

        Intent intent;
        if (prefsManager.isLoggedIn()) {
            // User is logged in, go to MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User not logged in, go to LoginActivity
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
