package com.twistedplane.sealnote.internal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.data.DatabaseHandler;
import net.sqlcipher.database.SQLiteException;

/**
 * Preference that implements change password functionality in a dialog.
 *
 * The dialog layout adds custom buttons as the original do not allow to
 * override the default behaviour of dismissing dialog when clicked.
 */
public class PasswordPreference extends DialogPreference implements TextWatcher {
    private EditText mOldView;
    private EditText mNewView;
    private EditText mNewConfirmView;
    private Button mChangeButton;
    private Button mCancelButton;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // initialize views
        setDialogLayoutResource(R.layout.dialog_password_pref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    /**
     * Hide the original buttons. Read comments on this class.
     */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    /**
     * Bind state of this class with the view i.e. text edits, buttons and
     * add listeners to them.
     *
     * @param view Dialog view
     */
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        mOldView = (EditText) view.findViewById(R.id.diag_password_pref_oldPassword);
        mNewView = (EditText) view.findViewById(R.id.diag_password_pref_newPassword);
        mNewConfirmView = (EditText) view.findViewById(R.id.diag_password_pref_newPasswordConfirm);
        mChangeButton = (Button) view.findViewById(R.id.button_positive);
        mCancelButton = (Button) view.findViewById(R.id.button_negative);

        // initially everything is empty hence the button is disabled
        mChangeButton.setEnabled(false);

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: What if password didn't change successfully?
                if (changePassword()) {
                    // password changed successfully
                    getDialog().dismiss();
                }
            }
        });

        // add listeners to text boxes so that we update button states with them
        mOldView.addTextChangedListener(this);
        mNewView.addTextChangedListener(this);
        mNewConfirmView.addTextChangedListener(this);

        // since we are not using default buttons, we have to add listener
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });
    }

    /**
     * Called before any of old password, new password or confirm password
     * EditText is changed but get focus.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        updateChangeButtonState();
    }

    /**
     * Called when any of old password, new password or confirm password
     * EditText is changed.
     */
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        updateChangeButtonState();
    }

    /**
     * Called after any of old password, new password or confirm password
     * EditText is changed. ie. loses focus after editing
     */
    @Override
    public void afterTextChanged(Editable editable) {
        updateChangeButtonState();
    }

    /**
     * Check if values in EditText are acceptable and change state of
     * button appropriately.
     */
    private void updateChangeButtonState() {
        String oldPassword = mOldView.getText().toString();
        String newPassword = mNewView.getText().toString();
        String newConfirmPassword = mNewConfirmView.getText().toString();

        // any of three EditText is empty, we won't accept
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

    /**
     * Change encrypted database password
     *
     * @return true if password changed successfully, else false
     */
    private boolean changePassword() {
        Resources resources = getContext().getResources();
        DatabaseHandler db = SealnoteApplication.getDatabase();

        String oldDbPassword = DatabaseHandler.getPassword();
        String oldPassword = mOldView.getText().toString();
        String newPassword = mNewView.getText().toString();
        String newConfirmPassword = mNewConfirmView.getText().toString();

        // Recycle old password, set new one and check if given old password
        // is correct
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

        //NOTE: Probably note required as we already checked for this
        if (!newPassword.equals(newConfirmPassword)) {
            Toast.makeText(getContext(), resources.getString(R.string.password_dont_match),
                    Toast.LENGTH_SHORT).show();
            db.recycle();
            DatabaseHandler.setPassword(oldDbPassword);
            db.update();
            return false;
        }

        // make query to change database key
        db.getWritableDatabase().rawQuery("PRAGMA rekey = '" + newPassword + "'", null).close();

        // Recycle old password and state, and set new password in handler
        db.recycle();
        DatabaseHandler.setPassword(newPassword);
        db.update();
        Toast.makeText(getContext(), resources.getString(R.string.password_changed_success),
                Toast.LENGTH_SHORT).show();

        return true;
    }
}
