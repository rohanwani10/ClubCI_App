package com.clubci.dbms_projectapp.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class Event {
    private String eventId;
    private String name;
    private String description;
    private String type;
    private Date dateTime;
    private String venue;
    private Date registrationDeadline;
    private int maxParticipants;
    private int currentParticipants;
    private double fee;
    private String posterUrl;
    private String requirements;
    private String contactInfo;
    private int attendedCount;
    private String status;

    public Event() {
    }

    public Event(String eventId, String name, Date dateTime, String venue) {
        this.eventId = eventId;
        this.name = name;
        this.dateTime = dateTime;
        this.venue = venue;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public String getId() {
        return eventId; // Alias for getEventId
    }

    public void setId(String id) {
        this.eventId = id; // Alias for setEventId
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Date getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(Date registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public int getAttendedCount() {
        return attendedCount;
    }

    public void setAttendedCount(int attendedCount) {
        this.attendedCount = attendedCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Calculate progress percentage
     */
    public int getProgressPercentage() {
        if (maxParticipants == 0) {
            return 0;
        }
        return (int) ((currentParticipants * 100.0) / maxParticipants);
    }

    /**
     * Check if event is full
     */
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    /**
     * Check if registration is open
     */
    public boolean isRegistrationOpen() {
        if (registrationDeadline == null) {
            return true;
        }
        return new Date().before(registrationDeadline) && !isFull();
    }

    /**
     * Convert Event to JSON for API requests
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (eventId != null)
            json.put("eventId", eventId);
        if (name != null)
            json.put("name", name);
        if (description != null)
            json.put("description", description);
        if (type != null)
            json.put("type", type);
        if (dateTime != null)
            json.put("dateTime", dateTime.getTime());
        if (venue != null)
            json.put("venue", venue);
        if (registrationDeadline != null)
            json.put("registrationDeadline", registrationDeadline.getTime());
        json.put("maxParticipants", maxParticipants);
        json.put("currentParticipants", currentParticipants);
        json.put("fee", fee);
        if (posterUrl != null)
            json.put("posterUrl", posterUrl);
        if (requirements != null)
            json.put("requirements", requirements);
        if (contactInfo != null)
            json.put("contactInfo", contactInfo);
        json.put("attendedCount", attendedCount);
        if (status != null)
            json.put("status", status);
        return json;
    }

    /**
     * Create Event from JSON response
     */
    public static Event fromJson(JSONObject json) throws JSONException {
        Event event = new Event();
        // Check for both eventId and _id (MongoDB default)
        if (json.has("eventId"))
            event.setEventId(json.getString("eventId"));
        else if (json.has("_id"))
            event.setEventId(json.getString("_id"));
        else if (json.has("id"))
            event.setEventId(json.getString("id"));

        // Check for different name field variations
        if (json.has("name"))
            event.setName(json.getString("name"));
        else if (json.has("eventName"))
            event.setName(json.getString("eventName"));
        else if (json.has("title"))
            event.setName(json.getString("title"));

        if (json.has("description"))
            event.setDescription(json.getString("description"));
        if (json.has("type"))
            event.setType(json.getString("type"));
        if (json.has("dateTime")) {
            String dateStr = json.getString("dateTime");
            // Handle both timestamp and string formats
            try {
                long timestamp = Long.parseLong(dateStr);
                event.setDateTime(new Date(timestamp));
            } catch (NumberFormatException e) {
                // It's a date string, will be parsed by DateUtils if needed
            }
        }
        if (json.has("venue"))
            event.setVenue(json.getString("venue"));
        if (json.has("registrationDeadline")) {
            String deadlineStr = json.getString("registrationDeadline");
            try {
                long timestamp = Long.parseLong(deadlineStr);
                event.setRegistrationDeadline(new Date(timestamp));
            } catch (NumberFormatException e) {
                // It's a date string
            }
        }
        if (json.has("maxParticipants"))
            event.setMaxParticipants(json.getInt("maxParticipants"));
        if (json.has("currentParticipants"))
            event.setCurrentParticipants(json.getInt("currentParticipants"));
        if (json.has("fee"))
            event.setFee(json.getDouble("fee"));
        if (json.has("posterUrl"))
            event.setPosterUrl(json.getString("posterUrl"));
        if (json.has("requirements"))
            event.setRequirements(json.getString("requirements"));
        if (json.has("contactInfo"))
            event.setContactInfo(json.getString("contactInfo"));
        if (json.has("attendedCount"))
            event.setAttendedCount(json.getInt("attendedCount"));
        if (json.has("status"))
            event.setStatus(json.getString("status"));
        return event;
    }
}
