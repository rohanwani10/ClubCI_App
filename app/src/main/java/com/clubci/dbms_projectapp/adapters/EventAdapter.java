package com.clubci.dbms_projectapp.adapters;

import android.content.res.TypedArray;
import android.util.TypedValue;
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
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Date;
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
        TextView tvName, tvDate, tvVenue, tvFee, tvProgress, tvType, tvStatus;
        ProgressBar progressBar;

        EventViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvFee = itemView.findViewById(R.id.tvFee);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        void bind(Event event, OnEventClickListener listener) {
            // Set click listener on the card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });

            tvName.setText(event.getName());

            // Format date
            if (event.getDateTime() != null) {
                tvDate.setText(DateUtils.formatDate(event.getDateTime()));
            } else {
                tvDate.setText("Date TBA");
            }

            tvVenue.setText(event.getVenue());

            // Format fee
            double fee = event.getFee();
            if (fee == 0) {
                tvFee.setText("FREE");
                tvFee.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                tvFee.setText("₹" + String.format("%.0f", fee));
                tvFee.setTextColor(itemView.getContext().getColor(R.color.accent));
            }

            // Progress
            int progress = event.getProgressPercentage();
            tvProgress.setText(event.getCurrentParticipants() + "/" + event.getMaxParticipants());
            progressBar.setProgress(progress);

            // Type
            String type = event.getType();
            if (type != null && !type.isEmpty()) {
                tvType.setText(type);
                tvType.setVisibility(View.VISIBLE);
            } else {
                tvType.setVisibility(View.GONE);
            }

            // Status
            String status = event.getStatus();
            if (status != null && !status.isEmpty()) {
                tvStatus.setText(status);
                tvStatus.setVisibility(View.VISIBLE);

                // Set status color
                int statusColor;
                if ("UPCOMING".equalsIgnoreCase(status)) {
                    statusColor = itemView.getContext().getColor(R.color.info);
                } else if ("ONGOING".equalsIgnoreCase(status)) {
                    statusColor = itemView.getContext().getColor(R.color.success);
                } else if ("COMPLETED".equalsIgnoreCase(status)) {
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
                } else {
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
                }
                tvStatus.setTextColor(statusColor);
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}
