package com.twistedplane.sealnote;

import android.app.Application;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteDatabase;

public class SealnoteApplication extends Application {
    private static DatabaseHandler mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // load sqlite-cipher native libraries
        SQLiteDatabase.loadLibs(this);
        mDatabase = new DatabaseHandler(this);
    }

    public static DatabaseHandler getDatabase() {
        return mDatabase;
    }
}
