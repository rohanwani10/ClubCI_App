package com.clubci.dbms_projectapp.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Helper class to manage window insets and edge-to-edge display
 * for safe area handling across different Android versions
 */
public class WindowInsetsHelper {

    /**
     * Enable edge-to-edge display for the activity
     * This allows the app to draw behind system bars
     * Call this BEFORE setContentView() for best results
     * 
     * @param activity The activity to enable edge-to-edge for
     */
    public static void enableEdgeToEdge(Activity activity) {
        Window window = activity.getWindow();

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            // Note: getInsetsController() might return null if called too early
            // We'll set appearance in a safer way
            try {
                WindowInsetsController controller = window.getInsetsController();
                if (controller != null) {
                    // Make system bars semi-transparent
                    controller.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            } catch (Exception e) {
                // Ignore if controller is not available yet
                // The appearance will be set by fitsSystemWindows in the layout
            }
        } else {
            // Pre-Android 11
            try {
                View decorView = window.getDecorView();
                if (decorView != null) {
                    int flags = decorView.getSystemUiVisibility();
                    flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                    flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    decorView.setSystemUiVisibility(flags);
                }
            } catch (Exception e) {
                // Ignore if decor view is not ready yet
            }
        }
    }

    /**
     * Apply window insets to a view
     * This ensures content is not obscured by system bars
     * 
     * @param view The view to apply insets to
     */
    public static void applyWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid system bars
            v.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply top window insets only (useful for toolbars)
     * 
     * @param view The view to apply top insets to
     */
    public static void applyTopWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply only top padding
            v.setPadding(
                    v.getPaddingLeft(),
                    insets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom());

            return windowInsets;
        });
    }

    /**
     * Apply bottom window insets only (useful for bottom navigation)
     * 
     * @param view The view to apply bottom insets to
     */
    public static void applyBottomWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply only bottom padding
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    insets.bottom);

            return windowInsets;
        });
    }

    /**
     * Apply horizontal window insets only (left and right)
     * 
     * @param view The view to apply horizontal insets to
     */
    public static void applyHorizontalWindowInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply only horizontal padding
            v.setPadding(
                    insets.left,
                    v.getPaddingTop(),
                    insets.right,
                    v.getPaddingBottom());

            return windowInsets;
        });
    }

    /**
     * Get system bar insets for manual handling
     * 
     * @param view The view to get insets from
     * @return Insets object containing system bar insets
     */
    public static Insets getSystemBarInsets(View view) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(view);
        if (windowInsets != null) {
            return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
        }
        return Insets.NONE;
    }

    /**
     * Check if gesture navigation is enabled
     * 
     * @param view The view to check from
     * @return true if gesture navigation is enabled
     */
    public static boolean isGestureNavigationEnabled(View view) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(view);
        if (windowInsets != null) {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets mandatoryInsets = windowInsets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures());

            // If bottom system bar inset is significantly smaller than mandatory gesture
            // inset,
            // gesture navigation is likely enabled
            return mandatoryInsets.bottom > insets.bottom;
        }
        return false;
    }
}
