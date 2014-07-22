package com.twistedplane.sealnote.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
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
import com.twistedplane.sealnote.view.PasswordInput;
import net.sqlcipher.database.SQLiteException;

/**
 * Preference that implements change password functionality in a dialog.
 *
 * The dialog layout adds custom buttons as the original do not allow to
 * override the default behaviour of dismissing dialog when clicked.
 */
public class PasswordPreference extends DialogPreference implements TextWatcher {
    public final static String TAG = "PasswordPreference";

    private EditText mOldView;
    private PasswordInput mNewView;
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
        mNewView = (PasswordInput) view.findViewById(R.id.diag_password_pref_newPassword);
        mNewConfirmView = (EditText) view.findViewById(R.id.diag_password_pref_newPasswordConfirm);
        mChangeButton = (Button) view.findViewById(R.id.button_positive);
        mCancelButton = (Button) view.findViewById(R.id.button_negative);

        //FIXME: Set in XML
        mNewView.setHint(getContext().getResources().getString(R.string.new_password));

        // initially everything is empty hence the button is disabled
        mChangeButton.setEnabled(false);

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: What if password didn't change successfully?
                new PasswordChangeTask().execute();
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
        String newPassword = mNewView.getText();
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
     * @param oldDbPassword Password current being used to access password in current session.
     *                      To restore state and continue with session.
     * @param oldPassword   Old password given by user via form to validate
     * @param newPassword   New password for database
     * @return              True if password changed successfully, else false
     */
    private boolean changePassword(String oldDbPassword, String oldPassword, String newPassword) {
        DatabaseHandler db = SealnoteApplication.getDatabase();

        // Recycle old password, set new one and check if given old password
        // is correct
        db.recycle();
        try {
            db.setPassword(oldPassword);
            db.update();
        } catch (SQLiteException e) {
            db.recycle();

            // If timeout has occurred the old password will be null
            // and we don't have to put database to original state
            if (oldDbPassword != null && !oldDbPassword.equals("")) {
                db.setPassword(oldDbPassword);
                db.update();
            }
            return false;
        }

        // make query to change database key
        String query = String.format("PRAGMA rekey = %s", DatabaseUtils.sqlEscapeString(newPassword));
        db.getWritableDatabase().execSQL(query);
        db.getWritableDatabase().close();

        // Recycle old password and state, and set new password in handler
        db.recycle();
        SealnoteApplication.getDatabase().setPassword(newPassword);
        db.update();

        return true;
    }

    /**
     * Asynchronous task to change password
     */
    class PasswordChangeTask extends AsyncTask<Void, Void, Boolean> {
        String oldDbPassword, oldPassword, newPassword;

        /**
         * Stores existing database password and entered passwords
         * into local variables and change UI state to disabled
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mChangeButton.setEnabled(false);
            mChangeButton.setText(R.string.please_wait);

            DatabaseHandler db = SealnoteApplication.getDatabase();
            oldDbPassword = db.getPassword();
            oldPassword = mOldView.getText().toString();
            newPassword = mNewView.getText();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return changePassword(oldDbPassword, oldPassword, newPassword);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mChangeButton.setEnabled(true);
            mChangeButton.setText(R.string.button_change);

            Resources resources = getContext().getResources();
            if (result) {
                getDialog().dismiss();
                Toast.makeText(getContext(), resources.getString(R.string.password_changed_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), resources.getString(R.string.incorrect_old_password),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
