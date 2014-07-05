package com.twistedplane.sealnote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.utils.*;

//FIXME: Clean up code and update flag on settings changed.

/**
 * NoteActivity implements activity to show and edit note in a full window view.
 */
public class NoteActivity extends Activity implements ColorDialogFragment.ColorChangedListener{
    public final static String TAG = "NoteActivity";

    private Note mNote;
    private Intent mShareIntent;
    private boolean mSaveButtonClicked = false;
    private boolean mAutoSaveEnabled;
    private boolean mLoadingNote = true;
    private boolean mTimedOut = false;

    private EditText mTitleView;
    private EditText mTextView;
    private TextView mEditedView;

    int mBackgroundColor;

    /**
     * Start a new NoteActivity with given note id.
     *
     * TODO: Take note object to make loading faster
     *
     * @param context   Context to use
     * @param id        Id of note. -1 for new note.
     */
    public static void startForNoteId(Context context, int id) {
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra("NOTE_ID", id);
        context.startActivity(intent);
    }

    /**
     * TextWatcher for note text and title. Right now just updates share button intent.
     */
    final private TextWatcher mNoteTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateShareIntent();
        }
    };

    /**
     * Check if note is new or existing, and update views appropriately with values.
     * If note is existing an async task is started which shows progress dialog
     * and delays loading of note
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        mBackgroundColor = 0;
        mAutoSaveEnabled = PreferenceHandler.isAutosaveEnabled(NoteActivity.this);

        int id = -1;
        Note bundledNote = null;
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt("NOTE_ID", -1);
            bundledNote = savedInstanceState.getParcelable("NOTE");
        }

        if (id == -1) {
            Bundle extras = getIntent().getExtras();
            id = extras.getInt("NOTE_ID", -1);
        }

        if (id != -1 && savedInstanceState != null && bundledNote != null) {
            Log.d(TAG, "Unsaved existing note being retrieved from bundle");
            init();
            loadNote(bundledNote);
            mLoadingNote = false;
        } else if (id != -1) {
            // existing note. Start an async task to load from storage
            new NoteLoadTask().execute(id);
        } else {
            Log.d(TAG, "Creating new note");
            init(); // new note simply setup views
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            mLoadingNote = false;
        }
    }

    /**
     * Initialize views, listeners and update references
     */
    private void init() {
        Misc.secureWindow(NoteActivity.this);

        mTitleView = (EditText) findViewById(R.id.note_activity_title);
        mTextView = (EditText) findViewById(R.id.note_activity_note);
        mEditedView = (TextView) findViewById(R.id.note_activity_edited);

        // TextWatcher to update share intent
        mTextView.addTextChangedListener(mNoteTextWatcher);
        mTitleView.addTextChangedListener(mNoteTextWatcher);

        mTitleView.setTypeface(FontCache.getFont(this, "RobotoSlab-Bold.ttf"));
        mTextView.setTypeface(FontCache.getFont(this, "RobotoSlab-Regular.ttf"));

        // set focus to text view //TODO: Only if id=-1
        mTextView.requestFocus();

        //NOTE: For ICS
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Update views with given note values
     */
    private void loadNote(Note note) {
        mNote = note;
        mTitleView.setText(mNote.getTitle());
        mTextView.setText(mNote.getNote());

        EasyDate date = mNote.getEditedDate();
        if (date == null) {
            mEditedView.setText("Edited " + EasyDate.now().friendly());
        } else {
            mEditedView.setText("Edited " + mNote.getEditedDate().friendly());
        }

        mBackgroundColor = mNote.getColor();
        onColorChanged(mBackgroundColor);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        updateShareIntent();
    }

    /**
     * When coming back from foreground check if timeout has expired and if
     * so load logout to password activity. Otherwise reset the timeout status.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (TimeoutHandler.instance().resume(this)) {
            mTimedOut = true;
            return;
        } else {
            mTimedOut = false;
        }
    }

    /**
     * Schedules a delayed callback after configured password expiry timeout.
     */
    @Override
    public void onPause() {
        super.onPause();

        // makes sure that note is loaded. onPause is also called when
        // progress dialog is shown at which moment we haven't set our
        // content view
        // FIXME: This can be made simpler maybe by making better layout
        if (!mTimedOut && mAutoSaveEnabled && !mLoadingNote) {
            saveNote();
            Log.d(TAG, "Note saved automatically due to activity pause.");
        }

        TimeoutHandler.instance().pause(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO: These bundles can be persistent and hence security hole
        if (mTimedOut || mLoadingNote) {
            // FIXME: After timeout user should be able to continue from where
            // they left off by giving password
            return;
        }

        int noteId = -1;
        if (mAutoSaveEnabled) {
            saveNote();
            noteId = mNote.getId();
            Log.d(TAG, "Note saved automatically due to activity pause.");
        } else {
            if (mNote != null && mNote.getId() != -1) {
                Log.d(TAG, "Save state in bundle for manual save");
                noteId = mNote.getId();
                outState.putParcelable("NOTE", mNote);
            }
        }

        outState.putInt("NOTE_ID", noteId);
    }

    /**
     * Inflate actionbar and add share button and intent
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_activity_actionbar, menu);

        // check if autosave is enabled and set visibility of action
        if (mAutoSaveEnabled) {
            MenuItem saveMenuItem = menu.findItem(R.id.action_save_note);
            saveMenuItem.setVisible(false);
        }

        // don't show delete action if note is newly created
        if (mNote == null || mNote.getId() == -1) {
            MenuItem deleteMenuItem = menu.findItem(R.id.action_note_delete);
            deleteMenuItem.setVisible(false);
        }

        // Fetch and store ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();

        mShareIntent = new Intent(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        shareActionProvider.setShareIntent(mShareIntent);
        updateShareIntent();

        return true;
    }

    /**
     * Update share intent with current note values
     */
    private void updateShareIntent() {
        if (mShareIntent == null) {
            //FIX: crash when orientation is changed
            return;
        }

        final EditText titleView = (EditText) findViewById(R.id.note_activity_title);
        final EditText textView = (EditText) findViewById(R.id.note_activity_note);
        String shareText;

        if (textView != null && titleView != null) {
            shareText = titleView.getText().toString() + "\n\n" + textView.getText().toString();
        } else {
            shareText = "";
        }

        mShareIntent.putExtra(Intent.EXTRA_TEXT, shareText.trim());
    }

    /**
     * An item on actionbar is selected. Dispatch appropriate action.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save_note:
                doSave();
                return true;
            case R.id.action_color:
                ColorDialogFragment cdf = new ColorDialogFragment();
                cdf.show(getFragmentManager(), "ColorDialogFragment");
                return true;
            case R.id.action_note_delete:
                doDelete();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save old or new note to database
     */
    public void saveNote() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        final String title = mTitleView.getText().toString();
        final String text = mTextView.getText().toString();

        if (title.equals("") && text.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mNote == null) {
            // this is a new note
            mNote = new Note();
        }
        mNote.setTitle(title);
        mNote.setNote(text);
        mNote.setColor(mBackgroundColor);

        if (mNote.getId() == -1) {
            mNote.setPosition(-1);
            int newNoteId = (int) handler.addNote(mNote);
            mNote.setId(newNoteId);
        } else {
            handler.updateNote(mNote);
        }
    }

    /**
     * Delete current note
     */
    public void doDelete() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        handler.deleteNote(mNote.getId());
        mNote = null;
        Toast.makeText(this, getResources().getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();

        // to disable saving when activity is finished when its
        // done in onPause()
        mAutoSaveEnabled = false;

        finish();
    }

    /**
     * Called when save action is invoked by click
     */
    public void doSave() {
        if (mSaveButtonClicked) return; else mSaveButtonClicked = true; //FIXME: Hack. Avoids double saving
        saveNote();
        Toast.makeText(this, getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Callback used when color is changed using ColorDialogFragment
     *
     * @param color New background color to use
     */
    public void onColorChanged(int color) {
        mBackgroundColor = color;

        if (color == -1) {
            return;
        }

        View view = findViewById(R.id.note_activity_title).getRootView();
        view.setBackgroundColor(Misc.getColorForCode(this, color));
    }

    /**
     * Asynchronous Task to load note
     */
    private class NoteLoadTask extends AsyncTask<Integer, Void, Note> {
        ProgressDialog mProgressDialog;

        /**
         * Before task starts, create and show a progress dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(NoteActivity.this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }

        /**
         * Fetch note from database in background
         */
        @Override
        protected Note doInBackground(Integer... integers) {
            DatabaseHandler db = SealnoteApplication.getDatabase();
            return db.getNote(integers[0]);
        }

        /**
         * Initialize the views and show data on it
         */
        @Override
        protected void onPostExecute(Note note) {
            super.onPostExecute(note);
            mProgressDialog.dismiss();
            init();
            loadNote(note);
            mLoadingNote = false;
        }
    }
}
