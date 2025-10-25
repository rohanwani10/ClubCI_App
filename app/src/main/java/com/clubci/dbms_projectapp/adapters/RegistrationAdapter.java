package com.clubci.dbms_projectapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.clubci.dbms_projectapp.R;
import com.clubci.dbms_projectapp.models.Registration;
import com.clubci.dbms_projectapp.utils.DateUtils;
import com.google.android.material.chip.Chip;
import java.util.List;

public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {

    private final List<Registration> registrations;
    private final OnCancelClickListener cancelListener;

    public interface OnCancelClickListener {
        void onCancelClick(Registration registration);
    }

    public RegistrationAdapter(List<Registration> registrations, OnCancelClickListener cancelListener) {
        this.registrations = registrations;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registration registration = registrations.get(position);

        holder.tvEventName.setText(registration.getEventName());

        if (registration.getRegistrationDate() != null) {
            holder.tvRegistrationDate.setText("Registered: " +
                    DateUtils.formatDate(registration.getRegistrationDate()));
        }

        // Payment Status Chip
        holder.chipPaymentStatus.setText(registration.getPaymentStatus());
        if ("COMPLETED".equalsIgnoreCase(registration.getPaymentStatus())) {
            holder.chipPaymentStatus.setChipBackgroundColorResource(R.color.success);
        } else if ("PENDING".equalsIgnoreCase(registration.getPaymentStatus())) {
            holder.chipPaymentStatus.setChipBackgroundColorResource(R.color.warning);
        } else {
            holder.chipPaymentStatus.setChipBackgroundColorResource(R.color.error);
        }

        // Attendance Status
        if (registration.isAttended()) {
            holder.tvAttendanceStatus.setVisibility(View.VISIBLE);
            holder.tvAttendanceStatus.setText("✓ Attended");
            holder.tvAttendanceStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.success));
        } else {
            holder.tvAttendanceStatus.setVisibility(View.GONE);
        }

        // Cancel button only for pending payments
        if ("PENDING".equalsIgnoreCase(registration.getPaymentStatus())) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onCancelClick(registration);
                }
            });
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return registrations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvRegistrationDate, tvAttendanceStatus;
        Chip chipPaymentStatus;
        Button btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvRegistrationDate = itemView.findViewById(R.id.tvRegistrationDate);
            tvAttendanceStatus = itemView.findViewById(R.id.tvAttendanceStatus);
            chipPaymentStatus = itemView.findViewById(R.id.chipPaymentStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
