package com.clubci.dbms_projectapp.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.json.JSONException;
import org.json.JSONObject;

public class QRCodeGenerator {

    /**
     * Generate QR Code bitmap from text
     * 
     * @param text   Text to encode
     * @param width  Width of the QR code
     * @param height Height of the QR code
     * @return Bitmap of QR code
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate QR code data for user in JSON format
     * 
     * @param username  Username
     * @param eventName Event name
     * @param eventId   Event ID
     * @return JSON string with username, eventName, eventId, and timestamp
     */
    public static String generateUserQRData(String username, String eventName, String eventId) {
        try {
            JSONObject qrData = new JSONObject();
            qrData.put("username", username);
            qrData.put("eventName", eventName);
            qrData.put("eventId", eventId);
            qrData.put("timestamp", System.currentTimeMillis());
            return qrData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            // Fallback to old format
            return username + "|" + eventId;
        }
    }

    /**
     * Generate QR code data for user (legacy method for backward compatibility)
     * 
     * @param username Username
     * @param eventId  Event ID (optional)
     * @return Encoded string
     */
    @Deprecated
    public static String generateUserQRData(String username, String eventId) {
        return generateUserQRData(username, "", eventId);
    }

    /**
     * Parse QR code data from JSON format
     * 
     * @param qrData QR code string (JSON or legacy format)
     * @return JSONObject with username, eventName, eventId, timestamp
     */
    public static JSONObject parseQRDataJson(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return null;
        }

        try {
            // Try to parse as JSON first
            JSONObject json = new JSONObject(qrData);
            return json;
        } catch (JSONException e) {
            // Fallback: try legacy format (username|eventId)
            try {
                JSONObject fallbackJson = new JSONObject();
                String[] parts = qrData.split("\\|");
                if (parts.length >= 1) {
                    fallbackJson.put("username", parts[0]);
                }
                if (parts.length >= 2) {
                    fallbackJson.put("eventId", parts[1]);
                }
                fallbackJson.put("eventName", "");
                fallbackJson.put("timestamp", System.currentTimeMillis());
                return fallbackJson;
            } catch (JSONException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Parse QR code data (legacy array format)
     * 
     * @param qrData QR code string
     * @return Array [username, eventId]
     * @deprecated Use parseQRDataJson instead
     */
    @Deprecated
    public static String[] parseQRData(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return new String[] { "", "" };
        }

        try {
            // Try JSON first
            JSONObject json = new JSONObject(qrData);
            return new String[] {
                    json.optString("username", ""),
                    json.optString("eventId", "")
            };
        } catch (JSONException e) {
            // Fallback to legacy format
            String[] parts = qrData.split("\\|");
            if (parts.length == 2) {
                return parts;
            } else if (parts.length == 1) {
                return new String[] { parts[0], "" };
            }
            return new String[] { "", "" };
        }
    }
}
