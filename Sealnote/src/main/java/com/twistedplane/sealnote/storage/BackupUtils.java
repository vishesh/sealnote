package com.twistedplane.sealnote.storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import java.io.*;

/**
 * Utilities to perform backup
 */
public class BackupUtils {
    private static final String TAG = "BackupUtils";

    /**
     * Copy content of src file to dst
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Backup database into a new file and return the backup File object
     */
    public static File backupDatabase(Context context) throws IOException {
        File dbFile = context.getDatabasePath(DatabaseHandler.DBNAME);
        File backupFile = new File(context.getExternalCacheDir(), "sealnote-backup.db");
        backupFile.deleteOnExit();
        copy(dbFile, backupFile);
        return backupFile;
    }

    /**
     * Restores give database as new database
     */
    public static File restoreDatabase(Context context, File file) throws IOException {
        DatabaseHandler dbHandler = SealnoteApplication.getDatabase();
        dbHandler.close();
        dbHandler.recycle();

        File dbFile = context.getDatabasePath(DatabaseHandler.DBNAME);
        copy(file, dbFile);

        return dbFile;
    }

    /**
     * Public check database password
     */
    public static boolean checkDatabasePassword(Context context, File file, String password) {
        try {
            SQLiteDatabase database = new SQLiteDatabase(file.getPath(),
                    password.toCharArray(), null, 0);
            database.close();
        } catch (SQLiteException e) {
            return false;
        }
        return true;
    }

    /**
     * Expected by BackupTask during start and finish of task
     */
    public static interface BackupListener {
        public void onBackupStart();
        public void onBackupFinish(File file);
    }

    /**
     * Task to do backup asynchronously. Expects a callback interfact
     * BackupListener
     */
    public static class BackupTask extends AsyncTask<Void, Void, File> {
        BackupListener mBackupListener;
        Context mContext;

        public BackupTask(Context context, BackupListener backupListener) {
            mBackupListener = backupListener;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBackupListener.onBackupStart();
        }

        @Override
        protected File doInBackground(Void... params) {
            File file = null;

            try {
                file = backupDatabase(mContext);
            } catch (IOException e) {
                Log.e(TAG, "Error during backup!");
                e.printStackTrace();
            }

            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mBackupListener.onBackupFinish(file);
        }
    }

    /**
     * Expected by RestoreTask during start and finish of task
     */
    public static interface RestoreListener {
        public void onRestoreStart();
        public void onRestoreFinish(boolean result);
    }

    /**
     * Task to do backup asynchronously. Expects a callback interface
     * BackupListener
     */
    public static class RestoreTask extends AsyncTask<File, Void, Boolean> {
        RestoreListener mRestoreListener;
        Context mContext;

        public RestoreTask(Context context, RestoreListener restoreListener) {
            mRestoreListener = restoreListener;
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mRestoreListener.onRestoreStart();
        }

        @Override
        protected Boolean doInBackground(File... files) {
            File file = files[0];

            try {
                restoreDatabase(mContext, file);
            } catch (IOException e) {
                Log.e(TAG, "Error during backup!");
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mRestoreListener.onRestoreFinish(result);
        }
    }
}
