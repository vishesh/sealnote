package com.twistedplane.sealnote;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.twistedplane.sealnote.storage.BackupUtils;
import com.twistedplane.sealnote.utils.FontCache;

import java.io.*;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Activity to do restore of database
 */
public class RestoreActivity extends Activity implements BackupUtils.RestoreListener {
    private static final String TAG = "RestoreActivity";

    private static final int REQUEST_RESTORE = 0x02;
    private static final int REQUEST_PICK_BACKUP = 0x01;
    private final int DELAY_PASSWORD_CHECK = 1500;

    private View        mRestoreButton;
    private View        mRestoreProgress;
    private EditText    mPasswordInput;
    private ViewFlipper mFlipper;
    private File        mBackupFile;
    final private CheckRunnable mChecker = new CheckRunnable();

    private class CheckRunnable implements Runnable {
        @Override
        public void run() {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    return BackupUtils.checkDatabasePassword(getBaseContext(),
                            mBackupFile, mPasswordInput.getText().toString());
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);

                    if (result) {
                        mRestoreButton.setVisibility(View.VISIBLE);
                    }
                    mRestoreProgress.setVisibility(View.GONE);
                }
            }.execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        mFlipper =  (ViewFlipper) findViewById(R.id.restore_flipper);
        mRestoreButton = findViewById(R.id.restore_button);
        mRestoreProgress = findViewById(R.id.restore_progress);
        mPasswordInput = (EditText) findViewById(R.id.input_password);

        // Setup flipper animations
        Animation animationFlipIn  = AnimationUtils.loadAnimation(this, R.anim.flipin);
        Animation animationFlipOut = AnimationUtils.loadAnimation(this, R.anim.flipout);

        mFlipper.setInAnimation(animationFlipIn);
        mFlipper.setOutAnimation(animationFlipOut);

        setupMessages();
        setupCheckCallbackup();
    }

    private void setupMessages() {
        TextView messageView1 = (TextView) findViewById(R.id.message_view_1);
        messageView1.setText(Html.fromHtml(
                getString(R.string.info_restore_message)
        ));
        messageView1.setTypeface(FontCache.getFont(this, "Roboto-Light"));

        TextView messageView2 = (TextView) findViewById(R.id.message_view_2);
        messageView2.setText(Html.fromHtml(
                getString(R.string.info_restore_message_2)
        ));
        messageView2.setTypeface(FontCache.getFont(this, "Roboto-Light"));

        TextView messageView3 = (TextView) findViewById(R.id.message_view_3);
        messageView3.setText(Html.fromHtml(
                getString(R.string.restore_complete_info)
        ));
        messageView3.setTypeface(FontCache.getFont(this, "Roboto-Light"));
    }

    /**
     * We check for password only after few seconds, to avoid
     * bombing checks while user types. If user types again
     * we remove any pending callbacks for check.
     */
    private void setupCheckCallbackup() {
        mPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mPasswordInput.removeCallbacks(mChecker);
                mRestoreButton.setVisibility(View.INVISIBLE);
                mRestoreProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPasswordInput.postDelayed(mChecker, DELAY_PASSWORD_CHECK);
            }
        });
    }

    /**
     * Pick up restore file
     */
    public void doPickFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, REQUEST_PICK_BACKUP);
    }

    /**
     * Start restore task
     */
    public void doRestore(View view) {
        mPasswordInput.setVisibility(View.GONE);
        new BackupUtils.RestoreTask(this, this).execute(mBackupFile);
    }

    /**
     * Called during restore start. Hide button and show progress circle
     */
    public void onRestoreStart() {
        findViewById(R.id.restore_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.restore_progress).setVisibility(View.VISIBLE);

        TextView message = (TextView) findViewById(R.id.message_view_2);
        message.setText(Html.fromHtml(getString(R.string.restoring)));
    }

    /**
     * Called when restore is done. Start a share intent for this file
     */
    public void onRestoreFinish(boolean result) {
        if (!result) {
            makeText(this, getResources().getString(R.string.restore_error), LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.restore_progress).setVisibility(View.INVISIBLE);
        mFlipper.showNext();
    }

    /**
     * Retrive content file
     */
    private void retrieveContentFile(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        mBackupFile = File.createTempFile("sealnote-backup", null);

        OutputStream out = new FileOutputStream(mBackupFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = input.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        input.close();
        out.close();
    }

    /**
     * Called after everything is done. Hide progress circle, show message and
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICK_BACKUP:
                if (resultCode != RESULT_OK) {
                    break;
                }

                try {
                    retrieveContentFile(data.getData());
                    mFlipper.showNext();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Backup file no longer exisits");
                } catch (IOException e) {
                    Log.e(TAG, "Error copying file!");
                }
                break;
            case REQUEST_RESTORE:
                makeText(this, getResources().getString(R.string.restore_complete), LENGTH_SHORT).show();
                break;
        }
    }
}

