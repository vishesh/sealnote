package com.twistedplane.sealnote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.twistedplane.sealnote.utils.EasyDate;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.Misc;

//FIXME: Clean up code and update flag on settings changed.

/**
 * NoteActivity implements activity to show and edit note in a full window view.
 */
public class NoteActivity extends Activity implements ColorDialogFragment.ColorChangedListener{
    private Note mNote;
    private Intent mShareIntent;
    private boolean mSaveButtonClicked = false;

    private EditText mTitleView;
    private EditText mTextView;
    TextView mEditedView;

    int mBackgroundColor;

    /**
     * TextWatcher for note text and title. Right now just updates share button intent.
     */
    private TextWatcher mNoteTextWatcher = new TextWatcher() {
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
        mBackgroundColor = 0;

        Bundle extras = getIntent().getExtras();
        int id = extras.getInt("NOTE_ID");
        if (id != -1) {
            // existing note. Start an async task to load from storage
            new NoteLoadTask().execute(id);
        } else {
            init(); // new note simply setup views
        }

        //NOTE: For ICS
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Initialize views, listeners and update references
     */
    private void init() {
        setContentView(R.layout.activity_note);
        Misc.secureWindow(NoteActivity.this);

        mTitleView = (EditText) findViewById(R.id.note_activity_title);
        mTextView = (EditText) findViewById(R.id.note_activity_note);
        mEditedView = (TextView) findViewById(R.id.note_activity_edited);

        // TextWatcher to update share intent
        mTextView.addTextChangedListener(mNoteTextWatcher);
        mTitleView.addTextChangedListener(mNoteTextWatcher);

        mTitleView.setTypeface(FontCache.getFont(getBaseContext(), "RobotoSlab-Bold.ttf"));
        mTextView.setTypeface(FontCache.getFont(getBaseContext(), "RobotoSlab-Regular.ttf"));

        // set focus to text view //TODO: Only if id=-1
        mTextView.requestFocus();
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
            return;
        }
    }

    /**
     * Schedules a delayed callback after configured password expiry timeout.
     */
    @Override
    public void onPause() {
        super.onPause();
        TimeoutHandler.instance().pause(this);
    }

    /**
     * Inflate actionbar and add share button and intent
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_activity_actionbar, menu);

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
                saveNote();
                return true;
            case R.id.action_color:
                ColorDialogFragment cdf = new ColorDialogFragment();
                cdf.show(getFragmentManager(), "ColorDialogFragment");
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
        if (mSaveButtonClicked) return; else mSaveButtonClicked = true; //FIXME: Hack. Avoids double saving

        final DatabaseHandler handler = new DatabaseHandler(this);
        final String title = mTitleView.getText().toString();
        final String text = mTextView.getText().toString();

        if ((title == null && text == null) || (title.equals("") && text.equals(""))) {
            Toast.makeText(this, getResources().getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = this.mNote;
        if (note == null) {
            // this is a new note
            note = new Note();
        }
        note.setTitle(title);
        note.setNote(text);
        note.setColor(mBackgroundColor);

        if (mNote == null) {
            note.setPosition(-1);
            handler.addNote(note);
        } else {
            handler.updateNote(note);
        }

        Toast.makeText(this, getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        this.finish();
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
        view.setBackgroundColor(Misc.getColorForCode(getBaseContext(), color));
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
            DatabaseHandler db = new DatabaseHandler(getBaseContext());
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
        }
    }

}
