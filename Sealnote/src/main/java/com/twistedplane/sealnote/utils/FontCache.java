package com.twistedplane.sealnote.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

public class FontCache {
    private static Map<String, Typeface> mFontMap = new HashMap<String, Typeface>();

    public static Typeface getFont(Context context, String fontName){
        if (mFontMap.containsKey(fontName)){
            return mFontMap.get(fontName);
        }
        else {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
            mFontMap.put(fontName, tf);
            return tf;
        }
    }
}
