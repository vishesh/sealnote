package com.twistedplane.sealnote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to easily access preference values used by SealNote.
 */
public class PreferenceHandler {
    public final static String TAG = "PreferenceHandler";

    public static enum NoteListViewType {
        VIEW_TILES, VIEW_COLUMN, VIEW_SIMPLE_LIST
    }

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

    public static NoteListViewType getNoteListViewType(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        String result = sharedPrefs.getString("NoteListViewType", "tiles");

        if (result.equals("tiles")) {
            return NoteListViewType.VIEW_TILES;
        } else if (result.equals("column")) {
            return NoteListViewType.VIEW_COLUMN;
        } else if (result.equals("simplelist")) {
            return NoteListViewType.VIEW_SIMPLE_LIST;
        }

        return NoteListViewType.VIEW_TILES;
    }

    public static boolean isAutosaveEnabled(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPrefs.getBoolean("AutoSave", false);
    }
}
