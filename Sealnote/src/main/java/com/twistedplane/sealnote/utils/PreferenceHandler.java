package com.twistedplane.sealnote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to easily access preference values used by SealNote.
 */
public class PreferenceHandler {
    public final static String TAG = "PreferenceHandler";

    public static boolean isSecureWindowEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("SecureWindow", false);
    }

    public static int getPasswordTimeout(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(sharedPrefs.getString("PasswordTimeout", "10000")); //FIXME: Get integer
    }

    public static boolean isDynamicFontSizeEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("DynamicFontSize", true);
    }

    public static boolean isMultiColumnGridEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("MultiColumnGrid", true);
    }

    public static boolean isAutosaveEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("AutoSave", false);
    }
}
