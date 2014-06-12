package com.twistedplane.sealnote;

import android.app.Application;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteDatabase;

public class SealnoteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this);
        DatabaseHandler.setPassword("test123");
    }
}
