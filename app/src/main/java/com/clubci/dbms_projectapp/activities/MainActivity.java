package com.clubci.dbms_projectapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.fragments.EventListFragment;
import com.clubci.dbms_projectapp.fragments.MyRegistrationsFragment;
import com.clubci.dbms_projectapp.fragments.QrCodeFragment;
import com.clubci.dbms_projectapp.fragments.ProfileFragment;
import com.clubci.dbms_projectapp.fragments.AnalyticsFragment;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.clubci.dbms_projectapp.utils.WindowInsetsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for safe area handling
        WindowInsetsHelper.enableEdgeToEdge(this);

        setContentView(R.layout.activity_main);

        prefsManager = SharedPreferencesManager.getInstance(this);

        initViews();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new EventListFragment());
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setSupportActionBar(toolbar);
    }

    private void setupBottomNavigation() {
        // Dynamically add Analytics tab for admin
        if (prefsManager.isAdmin()) {
            Menu menu = bottomNavigation.getMenu();
            menu.add(Menu.NONE, R.id.nav_analytics, Menu.NONE, R.string.nav_analytics)
                    .setIcon(android.R.drawable.ic_menu_info_details);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "ClubCI";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new EventListFragment();
                title = getString(R.string.events);
            } else if (itemId == R.id.nav_my_events) {
                selectedFragment = new MyRegistrationsFragment();
                title = getString(R.string.nav_my_events);
            } else if (itemId == R.id.nav_qr_code) {
                selectedFragment = new QrCodeFragment();
                title = getString(R.string.my_qr_code);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = getString(R.string.profile);
            } else if (itemId == R.id.nav_analytics && prefsManager.isAdmin()) {
                selectedFragment = new AnalyticsFragment();
                title = getString(R.string.nav_analytics);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    prefsManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void switchToEventsTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
