package com.twistedplane.sealnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteException;

public class PasswordPreference extends DialogPreference implements TextWatcher {
    EditText mOldView;
    EditText mNewView;
    EditText mNewConfirmView;
    Button mChangeButton;
    Button mCancelButton;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_password_pref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mOldView = (EditText) view.findViewById(R.id.diag_password_pref_oldPassword);
        mNewView = (EditText) view.findViewById(R.id.diag_password_pref_newPassword);
        mNewConfirmView = (EditText) view.findViewById(R.id.diag_password_pref_newPasswordConfirm);
        mChangeButton = (Button) view.findViewById(R.id.button_positive);
        mCancelButton = (Button) view.findViewById(R.id.button_negative);

        mChangeButton.setEnabled(false);
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changePassword()) {
                    getDialog().dismiss();
                }
            }
        });

        mOldView.addTextChangedListener(this);
        mNewView.addTextChangedListener(this);
        mNewConfirmView.addTextChangedListener(this);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        updateChangeButtonState();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        updateChangeButtonState();
    }

    @Override
    public void afterTextChanged(Editable editable) {
        updateChangeButtonState();
    }

    private void updateChangeButtonState() {
        String oldPassword = mOldView.getText().toString();
        String newPassword = mNewView.getText().toString();
        String newConfirmPassword = mNewConfirmView.getText().toString();

        if (oldPassword.equals("") || newPassword.equals("") || newConfirmPassword.equals("")) {
            mChangeButton.setEnabled(false);
            return;
        }

        if (!newPassword.equals(newConfirmPassword)) {
            mChangeButton.setEnabled(false);
            return;
        }

        mChangeButton.setEnabled(true);
    }

    private boolean changePassword() {
        Resources resources = getContext().getResources();
        DatabaseHandler db = new DatabaseHandler(getContext());

        String oldDbPassword = DatabaseHandler.getPassword();
        String oldPassword = mOldView.getText().toString();
        String newPassword = mNewView.getText().toString();
        String newConfirmPassword = mNewConfirmView.getText().toString();

        db.recycle();
        try {
            DatabaseHandler.setPassword(oldPassword);
            db.update();
        } catch (SQLiteException e) {
            Toast.makeText(getContext(), resources.getString(R.string.incorrect_old_password),
                    Toast.LENGTH_SHORT).show();
            db.recycle();
            DatabaseHandler.setPassword(oldDbPassword);
            db.update();
            return false;
        }

        if (!newPassword.equals(newConfirmPassword)) {
            Toast.makeText(getContext(), resources.getString(R.string.password_dont_match),
                    Toast.LENGTH_SHORT).show();
            db.recycle();
            DatabaseHandler.setPassword(oldDbPassword);
            db.update();
            return false;
        }

        // make query to change database key
        db.getWritableDatabase().rawQuery("PRAGMA rekey = '" + newPassword + "'", null);
        db.recycle();
        DatabaseHandler.setPassword(newPassword);
        db.update();
        Toast.makeText(getContext(), resources.getString(R.string.password_changed_success),
                Toast.LENGTH_SHORT).show();

        return true;
    }
}
