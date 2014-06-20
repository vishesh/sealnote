package com.twistedplane.sealnote;

import android.app.Application;
import net.sqlcipher.database.SQLiteDatabase;

public class SealnoteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this);
    }
}
