package com.twistedplane.sealnote.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.Window;
import android.view.WindowManager;
import com.twistedplane.sealnote.R;

/**
 * Miscellaneous helper functions
 */
public class Misc {
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
        return -1;
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
}
