package com.clubci.dbms_projectapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static final String FORMAT_DISPLAY = "MMM dd, yyyy hh:mm a";
    public static final String FORMAT_DISPLAY_DATE = "MMM dd, yyyy";
    public static final String FORMAT_DISPLAY_TIME = "hh:mm a";
    public static final String FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_DATE_PICKER = "yyyy-MM-dd";
    public static final String FORMAT_TIME_PICKER = "HH:mm";

    /**
     * Format date to display format
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date to display date only
     */
    public static String formatDateOnly(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DISPLAY_DATE, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date to display time only
     */
    public static String formatTimeOnly(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DISPLAY_TIME, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date string from API to display format
     */
    public static String formatApiDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat(FORMAT_API, Locale.getDefault());
            Date date = apiFormat.parse(apiDate);
            return formatDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return apiDate;
        }
    }

    /**
     * Parse API date string to Date object
     */
    public static Date parseApiDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat(FORMAT_API, Locale.getDefault());
            return apiFormat.parse(apiDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format Date to API format string
     */
    public static String formatToApi(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_API, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Combine date and time strings to Date object
     */
    public static Date combineDateAndTime(String dateStr, String timeStr) {
        try {
            String combined = dateStr + " " + timeStr;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.parse(combined);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if date is in the past
     */
    public static boolean isPast(Date date) {
        return date != null && date.before(new Date());
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(Date date) {
        return date != null && date.after(new Date());
    }

    /**
     * Get current date as string
     */
    public static String getCurrentDate() {
        return formatDate(new Date());
    }

    /**
     * Get relative time (e.g., "2 hours ago", "in 3 days")
     */
    public static String getRelativeTime(Date date) {
        if (date == null) {
            return "";
        }

        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            return formatDateOnly(date);
        }
    }
}
