package com.clubci.dbms_projectapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class Registration {
    private String registrationId;
    private String eventId;
    private String eventName;
    private String username;
    private String fullName;
    private Date registrationDate;
    private String paymentStatus; // "pending", "completed"
    private String attendanceStatus; // "pending", "attended"
    private Date eventDate;
    private String venue;
    private double fee;

    public Registration() {
    }

    public Registration(String eventId, String username) {
        this.eventId = eventId;
        this.username = username;
        this.registrationDate = new Date();
        this.paymentStatus = "pending";
        this.attendanceStatus = "pending";
    }

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    /**
     * Check if payment is completed
     */
    public boolean isPaymentCompleted() {
        return "completed".equalsIgnoreCase(paymentStatus);
    }

    /**
     * Check if user attended
     */
    public boolean isAttended() {
        return "attended".equalsIgnoreCase(attendanceStatus);
    }

    /**
     * Convert Registration to JSON for API requests
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (registrationId != null)
            json.put("registrationId", registrationId);
        if (eventId != null)
            json.put("eventId", eventId);
        if (eventName != null)
            json.put("eventName", eventName);
        if (username != null)
            json.put("username", username);
        if (fullName != null)
            json.put("fullName", fullName);
        if (registrationDate != null)
            json.put("registrationDate", registrationDate.getTime());
        if (paymentStatus != null)
            json.put("paymentStatus", paymentStatus);
        if (attendanceStatus != null)
            json.put("attendanceStatus", attendanceStatus);
        if (eventDate != null)
            json.put("eventDate", eventDate.getTime());
        if (venue != null)
            json.put("venue", venue);
        json.put("fee", fee);
        return json;
    }

    /**
     * Create Registration from JSON response
     */
    public static Registration fromJson(JSONObject json) throws JSONException {
        Registration registration = new Registration();
        if (json.has("registrationId"))
            registration.setRegistrationId(json.getString("registrationId"));
        if (json.has("eventId"))
            registration.setEventId(json.getString("eventId"));
        if (json.has("eventName"))
            registration.setEventName(json.getString("eventName"));
        if (json.has("username"))
            registration.setUsername(json.getString("username"));
        if (json.has("fullName"))
            registration.setFullName(json.getString("fullName"));
        if (json.has("registrationDate")) {
            try {
                long timestamp = json.getLong("registrationDate");
                registration.setRegistrationDate(new Date(timestamp));
            } catch (JSONException e) {
                // Handle string date format if needed
            }
        }
        if (json.has("paymentStatus"))
            registration.setPaymentStatus(json.getString("paymentStatus"));
        if (json.has("attendanceStatus"))
            registration.setAttendanceStatus(json.getString("attendanceStatus"));
        if (json.has("eventDate")) {
            try {
                long timestamp = json.getLong("eventDate");
                registration.setEventDate(new Date(timestamp));
            } catch (JSONException e) {
                // Handle string date format if needed
            }
        }
        if (json.has("venue"))
            registration.setVenue(json.getString("venue"));
        if (json.has("fee"))
            registration.setFee(json.getDouble("fee"));
        return registration;
    }
}
