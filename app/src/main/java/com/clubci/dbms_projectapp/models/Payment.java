package com.clubci.dbms_projectapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class Payment {
    private String paymentId;
    private String eventId;
    private String eventName;
    private String username;
    private double amount;
    private Date paymentDate;
    private String paymentMethod;
    private String transactionId;
    private String status;

    public Payment() {
    }

    public Payment(String eventId, String username, double amount) {
        this.eventId = eventId;
        this.username = username;
        this.amount = amount;
        this.paymentDate = new Date();
        this.status = "pending";
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return "success".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status);
    }

    /**
     * Convert Payment to JSON for API requests
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (paymentId != null)
            json.put("paymentId", paymentId);
        if (eventId != null)
            json.put("eventId", eventId);
        if (eventName != null)
            json.put("eventName", eventName);
        if (username != null)
            json.put("username", username);
        json.put("amount", amount);
        if (paymentDate != null)
            json.put("paymentDate", paymentDate.getTime());
        if (paymentMethod != null)
            json.put("paymentMethod", paymentMethod);
        if (transactionId != null)
            json.put("transactionId", transactionId);
        if (status != null)
            json.put("status", status);
        return json;
    }

    /**
     * Create Payment from JSON response
     */
    public static Payment fromJson(JSONObject json) throws JSONException {
        Payment payment = new Payment();
        if (json.has("paymentId"))
            payment.setPaymentId(json.getString("paymentId"));
        if (json.has("eventId"))
            payment.setEventId(json.getString("eventId"));
        if (json.has("eventName"))
            payment.setEventName(json.getString("eventName"));
        if (json.has("username"))
            payment.setUsername(json.getString("username"));
        if (json.has("amount"))
            payment.setAmount(json.getDouble("amount"));
        if (json.has("paymentDate")) {
            try {
                long timestamp = json.getLong("paymentDate");
                payment.setPaymentDate(new Date(timestamp));
            } catch (JSONException e) {
                // Handle string date format if needed
            }
        }
        if (json.has("paymentMethod"))
            payment.setPaymentMethod(json.getString("paymentMethod"));
        if (json.has("transactionId"))
            payment.setTransactionId(json.getString("transactionId"));
        if (json.has("status"))
            payment.setStatus(json.getString("status"));
        return payment;
    }
}
