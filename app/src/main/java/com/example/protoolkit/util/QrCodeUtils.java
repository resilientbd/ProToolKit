package com.example.protoolkit.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;

import java.util.regex.Pattern;

public class QrCodeUtils {

    public enum QrType {
        URL,
        TEXT,
        WIFI,
        CONTACT,
        EMAIL,
        SMS,
        CALENDAR,
        GEO,
        OTHER
    }

    public static QrType getQrCodeType(String content) {
        if (content == null || content.isEmpty()) {
            return QrType.OTHER;
        }

        content = content.trim();

        // Check for URL
        if (Patterns.WEB_URL.matcher(content).matches() || 
            content.toLowerCase().startsWith("http://") || 
            content.toLowerCase().startsWith("https://")) {
            return QrType.URL;
        }

        // Check for WiFi configuration (WIFI:S:network_name;T:WPA;P:password;;)
        if (content.startsWith("WIFI:")) {
            return QrType.WIFI;
        }

        // Check for email (MAILTO:email@domain.com or simple email format)
        if (Patterns.EMAIL_ADDRESS.matcher(content).matches() || 
            content.toLowerCase().startsWith("mailto:")) {
            return QrType.EMAIL;
        }

        // Check for SMS (SMSTO:number:body or simple phone number format)
        if (content.toLowerCase().startsWith("smsto:") || 
            Patterns.PHONE.matcher(content).matches()) {
            return QrType.SMS;
        }

        // Check for contact information (MECARD or VCARD format)
        if (content.toUpperCase().startsWith("MECARD:") || 
            content.toUpperCase().startsWith("BEGIN:VCARD")) {
            return QrType.CONTACT;
        }

        // Check for calendar event (VEVENT)
        if (content.toUpperCase().startsWith("BEGIN:VEVENT")) {
            return QrType.CALENDAR;
        }

        // Check for geo location (geo:lat,lon)
        if (content.toLowerCase().startsWith("geo:")) {
            return QrType.GEO;
        }

        // Default to text
        return QrType.TEXT;
    }

    public static void handleQrResult(Context context, String content) {
        QrType type = getQrCodeType(content);

        switch (type) {
            case URL:
                handleUrl(context, content);
                break;
            case TEXT:
                handleText(context, content);
                break;
            case WIFI:
                handleWifi(context, content);
                break;
            case CONTACT:
                handleContact(context, content);
                break;
            case EMAIL:
                handleEmail(context, content);
                break;
            case SMS:
                handleSms(context, content);
                break;
            case CALENDAR:
                handleCalendar(context, content);
                break;
            case GEO:
                handleGeo(context, content);
                break;
            case OTHER:
                handleText(context, content);
                break;
        }
    }

    private static void handleUrl(Context context, String url) {
        String cleanUrl = url.toLowerCase();
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            // URL is already in correct format
        } else {
            // Add protocol if missing
            if (cleanUrl.startsWith("www.")) {
                url = "http://" + url;
            } else {
                url = "http://" + url;
            }
        }
        
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        }
    }

    private static void handleText(Context context, String text) {
        // For now, just copy to clipboard - in a real app you might show a dialog
        // with options to copy, share, etc.
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("QR Code Content", text);
        clipboard.setPrimaryClip(clip);
    }

    private static void handleWifi(Context context, String wifiString) {
        // WiFi QR format: WIFI:S:network_name;T:WPA;P:password;H:false;;
        // Parse the WiFi configuration
        String ssid = extractWifiValue(wifiString, "S:");
        String password = extractWifiValue(wifiString, "P:");
        String securityType = extractWifiValue(wifiString, "T:");
        
        // Try to open WiFi settings to guide user
        Intent wifiIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        boolean intentResolved = false;
        
        if (wifiIntent.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(wifiIntent);
                intentResolved = true;
            } catch (Exception e) {
                // Fallback to general settings if WiFi settings fail
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                if (settingsIntent.resolveActivity(context.getPackageManager()) != null) {
                    try {
                        context.startActivity(settingsIntent);
                        intentResolved = true;
                    } catch (Exception ex) {
                        // If both fail, we'll show a message
                    }
                }
            }
        }
        
        // Show a toast message to guide the user
        String toastMessage = intentResolved ? 
            "Opening WiFi settings." : 
            "Go to WiFi settings to connect manually.";
            
        try {
            android.widget.Toast.makeText(context, toastMessage, android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Fallback if toast fails
        }
        
        // NOTE: Password should already be copied separately in the Fragment when dialog shows
    }
    
    private static String extractWifiValue(String wifiString, String key) {
        int startIndex = wifiString.indexOf(key);
        if (startIndex == -1) return "";
        
        startIndex += key.length();
        int endIndex = wifiString.indexOf(";", startIndex);
        if (endIndex == -1) return wifiString.substring(startIndex);
        
        return wifiString.substring(startIndex, endIndex);
    }

    private static void handleContact(Context context, String contactString) {
        // For contacts, copy to clipboard 
        handleText(context, contactString);
    }

    private static void handleEmail(Context context, String emailString) {
        String email = emailString;
        if (email.toLowerCase().startsWith("mailto:")) {
            email = email.substring(7); // Remove "mailto:" prefix
        }
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(emailIntent);
        }
    }

    private static void handleSms(Context context, String smsString) {
        String phoneNumber = smsString;
        if (smsString.toLowerCase().startsWith("smsto:")) {
            phoneNumber = smsString.substring(6); // Remove "smsto:" prefix
        }
        
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        
        if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(smsIntent);
        }
    }

    private static void handleCalendar(Context context, String calendarString) {
        // Copy calendar event data to clipboard
        handleText(context, calendarString);
    }

    private static void handleGeo(Context context, String geoString) {
        // Format: geo:lat,lon or geo:lat,lon?z=zoom
        Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoString));
        if (geoIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(geoIntent);
        }
    }
}