package com.clubci.dbms_projectapp.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password (minimum 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validate phone number (10 digits)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() == 10;
    }

    /**
     * Validate username (alphanumeric, underscore, 3-20 characters)
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        return pattern.matcher(username).matches();
    }

    /**
     * Check if string is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validate required field
     */
    public static String validateRequired(String value, String fieldName) {
        if (isEmpty(value)) {
            return fieldName + " is required";
        }
        return null;
    }

    /**
     * Validate email field
     */
    public static String validateEmail(String email) {
        if (isEmpty(email)) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        return null;
    }

    /**
     * Validate password field
     */
    public static String validatePassword(String password) {
        if (isEmpty(password)) {
            return "Password is required";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    /**
     * Validate phone field
     */
    public static String validatePhone(String phone) {
        if (isEmpty(phone)) {
            return "Phone number is required";
        }
        if (!isValidPhone(phone)) {
            return "Phone must be 10 digits";
        }
        return null;
    }

    /**
     * Validate username field
     */
    public static String validateUsername(String username) {
        if (isEmpty(username)) {
            return "Username is required";
        }
        if (!isValidUsername(username)) {
            return "Username must be 3-20 alphanumeric characters";
        }
        return null;
    }

    /**
     * Check if passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate non-negative number
     */
    public static boolean isNonNegativeNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
