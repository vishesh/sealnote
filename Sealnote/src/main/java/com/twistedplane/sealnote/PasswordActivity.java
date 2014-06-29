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

public class PasswordActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkExistingDatabase()) {
            createLoginScreen();
        } else {
            createWelcomeScreen();
        }
    }

    public boolean checkExistingDatabase() {
        File dbFile = getDatabasePath(DatabaseHandler.DBNAME);
        return dbFile.exists();
    }

    public void createWelcomeScreen() {
        setContentView(R.layout.activity_password_first);

        final Button button = (Button) findViewById(R.id.create_password_button);
        final EditText password = (EditText) findViewById(R.id.new_password_input);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password_input = password.getText().toString();
                if (!password_input.equals("")) {
                    new LoginTask().execute(password.getText().toString());
                    button.setEnabled(false); //FIXME: Add progress bar
                } else {
                    AlertDialog.Builder alert  = new AlertDialog.Builder(PasswordActivity.this);
                    alert.setMessage("Invalid password!");
                    alert.setPositiveButton("OK", null);
                    alert.setCancelable(false);
                    alert.create().show();
                }
            }
        });
    }

    public void createLoginScreen() {
        setContentView(R.layout.activity_password);

        final Button button = (Button) findViewById(R.id.go_password_button);
        final EditText password = (EditText) findViewById(R.id.password_input);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask().execute(password.getText().toString());
                toggleProgress();
            }
        });
    }

    public void toggleProgress() {
        //TODO: Toggle editable property of password textedit
        final Button button = (Button) findViewById(R.id.go_password_button);
        final ProgressBar progress_circle = (ProgressBar) findViewById(R.id.go_password_progress);

        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.INVISIBLE);
            progress_circle.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
            progress_circle.setVisibility(View.GONE);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
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

        protected void onPostExecute(Boolean result) {
            if (result) {
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
