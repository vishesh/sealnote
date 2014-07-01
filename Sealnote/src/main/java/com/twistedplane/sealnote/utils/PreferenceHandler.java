package com.twistedplane.sealnote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHandler {
    public static boolean isSecureWindowEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("pref_secureWindow", false);
    }

    public static int getPasswordTimeout(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(sharedPrefs.getString("pref_passwordTimeout", "10000")); //FIXME: Get integer
    }

    public static boolean isDynamicFontSizeEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("pref_dynamicFontSize", true);
    }
}
