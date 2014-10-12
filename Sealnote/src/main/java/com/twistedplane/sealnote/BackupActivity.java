package com.twistedplane.sealnote;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.twistedplane.sealnote.storage.BackupUtils;
import com.twistedplane.sealnote.utils.FontCache;

import java.io.File;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Activity to do backup of database
 */
public class BackupActivity extends Activity implements BackupUtils.BackupListener {
    private static final String TAG = "BackupActivity";
    private static final int REQUEST_BACKUP = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        TextView messageView = (TextView) findViewById(R.id.message_view);
        messageView.setText(Html.fromHtml(
                getString(R.string.info_backup_message)
        ));

        messageView.setTypeface(FontCache.getFont(this, "Roboto-Light"));
    }

    /**
     * Start backup activity
     */
    public void doBackup(View view) {
        new BackupUtils.BackupTask(this, this).execute();
    }

    /**
     * Called during backup start. Hide button and show progress circle
     */
    public void onBackupStart() {
        findViewById(R.id.backup_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.backup_progress).setVisibility(View.VISIBLE);
    }

    /**
     * Called when backup is done. Start a share intent for this file
     *
     * @param file File object containing backup database
     */
    public void onBackupFinish(File file) {
        if (file == null) {
            makeText(this, getResources().getString(R.string.backup_error), LENGTH_SHORT).show();
            return;
        }
        shareBackupFile(file);
    }

    /**
     * Start an ACTION_SEND intent for sharing the backup file
     *
     * @param file File object containing backup database
     */
    public void shareBackupFile(File file) {
        Uri uri = Uri.fromFile(file);
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("application/octet-stream");

        startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.send_to)),
                REQUEST_BACKUP);
    }

    /**
     * Called after everything is done. Hide progress circle, show message and
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BACKUP) {
            makeText(this, getResources().getString(R.string.backup_complete), LENGTH_SHORT).show();
        }

        findViewById(R.id.backup_progress).setVisibility(View.INVISIBLE);
        findViewById(R.id.backup_button).setVisibility(View.VISIBLE);
    }
}
