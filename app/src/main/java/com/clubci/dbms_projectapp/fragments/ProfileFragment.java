package com.clubci.dbms_projectapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.activities.LoginActivity;
import com.clubci.dbms_projectapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvFullName, tvRole;
    private MaterialButton btnEditProfile, btnManageEvents, btnLogout;
    private MaterialCardView cardAdminSection;

    private SharedPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvRole = view.findViewById(R.id.tvRole);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnManageEvents = view.findViewById(R.id.btnManageEvents);
        btnLogout = view.findViewById(R.id.btnLogout);
        cardAdminSection = view.findViewById(R.id.cardAdminSection);

        loadUserProfile();
        setupButtons();
    }

    private void loadUserProfile() {
        tvUsername.setText(prefsManager.getUsername());
        tvEmail.setText(prefsManager.getEmail());
        tvFullName.setText(prefsManager.getFullName());

        if (prefsManager.isAdmin()) {
            tvRole.setText("Role: Administrator");
            tvRole.setTextColor(requireContext().getColor(R.color.accent));
            cardAdminSection.setVisibility(View.VISIBLE);
        } else {
            tvRole.setText("Role: User");
            cardAdminSection.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile activity
            // For now, show message
            new AlertDialog.Builder(requireContext())
                    .setTitle("Edit Profile")
                    .setMessage("Profile editing feature coming soon!")
                    .setPositiveButton("OK", null)
                    .show();
        });

        btnManageEvents.setOnClickListener(v -> {
            // Switch to events fragment (admin can manage from there)
            if (getActivity() != null) {
                ((com.clubci.dbms_projectapp.activities.MainActivity) getActivity())
                        .switchToEventsTab();
            }
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        prefsManager.clearAll();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
