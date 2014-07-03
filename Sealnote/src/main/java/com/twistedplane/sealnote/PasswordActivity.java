package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteException;

import java.io.File;

/**
 * Activity shown application is started first time to create new database
 * or to ask for password during login
 */
public class PasswordActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkExistingDatabase()) {
            createLoginScreen();
        } else {
            // This is first run for application
            createWelcomeScreen();
        }
    }

    /**
     * Check if database for this application already exisits
     */
    private boolean checkExistingDatabase() {
        File dbFile = getDatabasePath(DatabaseHandler.DBNAME);
        return dbFile.exists();
    }

    /**
     * Create views for welcome screen. Executed when application is
     * started for first time without any database in storage.
     */
    private void createWelcomeScreen() {
        setContentView(R.layout.activity_password_first);

        final Button createButton = (Button) findViewById(R.id.create_password_button);
        final EditText passwordView = (EditText) findViewById(R.id.new_password_input);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password_input = passwordView.getText().toString();
                if (!password_input.equals("")) {
                    new LoginTask().execute(passwordView.getText().toString());
                    createButton.setEnabled(false); //FIXME: Add progress bar
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(PasswordActivity.this);
                    alert.setMessage("Invalid password!");
                    alert.setPositiveButton("OK", null);
                    alert.setCancelable(false);
                    alert.create().show();
                }
            }
        });
    }

    /**
     * Create login screen
     */
    private void createLoginScreen() {
        setContentView(R.layout.activity_password);

        final Button button = (Button) findViewById(R.id.go_password_button);
        final EditText password = (EditText) findViewById(R.id.password_input);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleProgress();
                new LoginTask().execute(password.getText().toString());
            }
        });
    }

    /**
     * Toggle progress circle icon
     *
     * NOTE: Currently works for login password button
     */
    private void toggleProgress() {
        //TODO: Toggle editable property of password textedit
        final Button button = (Button) findViewById(R.id.go_password_button);
        final ProgressBar progress_circle = (ProgressBar) findViewById(R.id.go_password_progress);

        if (button.getVisibility() == View.VISIBLE) {
            // toggle ON
            button.setVisibility(View.INVISIBLE);
            progress_circle.setVisibility(View.VISIBLE);
        } else {
            // toggle OFF
            button.setVisibility(View.VISIBLE);
            progress_circle.setVisibility(View.GONE);
        }
    }

    /**
     * Asynchronous task to create new database and/or login to encrypted database
     */
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        /**
         * Recycles the database and update it with new password. If password is
         * incorrect result is false which is handled PostExecute.
         *
         * If database doesn't exist, one would be created by DatabaseHandler
         *
         * @param args  Expects one argument which is password
         * @return      true is successfully created/login to encrypted database
         */
        protected Boolean doInBackground(String... args) {
            DatabaseHandler handler = new DatabaseHandler(PasswordActivity.this);
            handler.recycle();
            try {
                DatabaseHandler.setPassword(args[0]);
                handler.update();
            } catch (SQLiteException e) {
                return false;
            }
            return true;
        }

        /**
         * Starts appropriate activity if login/create is successful.
         */
        protected void onPostExecute(Boolean result) {
            if (result) {
                // clear all timers if any to avoid bouncing back here from activity
                TimeoutHandler.instance().passwordTimeoutClear();

                Intent intent = new Intent(PasswordActivity.this, SealnoteActivity.class);
                PasswordActivity.this.startActivity(intent);
                PasswordActivity.this.finish();
            } else {
                Toast.makeText(PasswordActivity.this, getResources()
                     .getString(R.string.invalid_password), Toast.LENGTH_SHORT)
                     .show();
                PasswordActivity.this.toggleProgress();
            }
        }
    }
}
