package com.twistedplane.sealnote.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Window;
import android.view.WindowManager;
import com.twistedplane.sealnote.PasswordActivity;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteApplication;

/**
 * Miscellaneous helper functions
 */
public class Misc {
    public static final String TAG = "Misc";

    private Misc() {}

    /**
     * Get background color corresponsing to given code
     *
     * @param context   Context used to get resources
     * @param code      Code of background color
     * @return          Background color integer value
     */
    public static int getColorForCode(Context context, int code) {
        Resources resources = context.getResources();

        switch (code) {
            case 0:
                return resources.getColor(R.color.card_background_color0);
            case 1:
                return resources.getColor(R.color.card_background_color1);
            case 2:
                return resources.getColor(R.color.card_background_color2);
            case 3:
                return resources.getColor(R.color.card_background_color3);
            case 4:
                return resources.getColor(R.color.card_background_color4);
            case 5:
                return resources.getColor(R.color.card_background_color5);
            case 6:
                return resources.getColor(R.color.card_background_color6);
            case 7:
                return resources.getColor(R.color.card_background_color7);
        }

        // return the first color
        return resources.getColor(R.color.card_background_color0);
    }

    /**
     * Checks if secure window preference is enabled and update appropriate flags.
     * Secure window prevents non-secure displays such as application switcher,
     * screenshots etc to access the content of window.
     *
     * @param activity  Activity whose window is to be secured
     */
    public static void secureWindow(Activity activity) {
        boolean isSecureWindow = PreferenceHandler.isSecureWindowEnabled(activity);
        Window window = activity.getWindow();

        if (isSecureWindow) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            window.setFlags(0, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    public static boolean isPasswordLoaded() {
        return SealnoteApplication.getDatabase().getPassword() != null;
    }

    public static void startPasswordActivity(Activity activity) {
        Intent passwordIntent = new Intent(activity, PasswordActivity.class);
        activity.startActivity(passwordIntent);
        activity.finish();
    }
}
