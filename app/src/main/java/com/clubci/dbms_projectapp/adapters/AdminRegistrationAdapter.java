package com.clubci.dbms_projectapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.models.Registration;
import com.google.android.material.chip.Chip;

import java.util.List;

public class AdminRegistrationAdapter extends RecyclerView.Adapter<AdminRegistrationAdapter.ViewHolder> {

    private final List<Registration> registrations;

    public AdminRegistrationAdapter(List<Registration> registrations) {
        this.registrations = registrations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_registration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registration registration = registrations.get(position);

        // Show username (fallback to full name if username missing)
        String username = registration.getUsername();
        if (username == null || username.isEmpty()) {
            username = registration.getFullName();
        }
        holder.tvUsername.setText(username != null ? username : "-");

        // Attendance chip
        if (registration.isAttended()) {
            holder.chipAttendance.setText("Attended");
            holder.chipAttendance.setChipBackgroundColorResource(R.color.success);
            holder.chipAttendance.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.chipAttendance.setText("Not Attended");
            holder.chipAttendance.setChipBackgroundColorResource(R.color.warning);
            holder.chipAttendance.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Chip chipAttendance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            chipAttendance = itemView.findViewById(R.id.chipAttendance);
        }
    }
}
