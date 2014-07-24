package com.twistedplane.sealnote.storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.twistedplane.sealnote.data.DatabaseHandler;

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
                Log.d(TAG, "Error during backup!");
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
}
