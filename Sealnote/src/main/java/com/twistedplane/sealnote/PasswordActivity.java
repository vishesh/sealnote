package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
                DatabaseHandler handler = new DatabaseHandler(PasswordActivity.this);
                handler.recycle();
                if (!password_input.equals("")) {
                    DatabaseHandler.setPassword(password_input);
                    handler.update();
                    Intent intent = new Intent(PasswordActivity.this, SealnoteActivity.class);
                    PasswordActivity.this.startActivity(intent);
                    PasswordActivity.this.finish();
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
                DatabaseHandler handler = new DatabaseHandler(PasswordActivity.this);
                handler.recycle();
                try {
                    handler.setPassword(password.getText().toString());
                    handler.update();
                    Intent intent = new Intent(PasswordActivity.this, SealnoteActivity.class);
                    PasswordActivity.this.startActivity(intent);
                    PasswordActivity.this.finish();
                } catch (SQLiteException e) {
                    AlertDialog.Builder alert  = new AlertDialog.Builder(PasswordActivity.this);
                    alert.setMessage("Invalid password!");
                    alert.setPositiveButton("OK", null);
                    alert.setCancelable(false);
                    alert.create().show();
                }
            }
        });
    }
}
