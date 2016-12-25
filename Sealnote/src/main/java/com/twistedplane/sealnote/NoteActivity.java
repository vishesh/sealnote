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
import android.view.*;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.collect.Sets;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.NoteContent;
import com.twistedplane.sealnote.fragment.ColorDialogFragment;
import com.twistedplane.sealnote.utils.*;
import com.twistedplane.sealnote.view.NoteView;
import com.twistedplane.sealnote.view.TagEditText;


/**
 * NoteActivity implements activity to show and edit note in a full window view.
 */
public class NoteActivity extends Activity implements ColorDialogFragment.ColorChangedListener{
    public final static String TAG = "NoteActivity";

    private Note        mNote;
    private Note.Type   mNoteType;
    private Intent      mShareIntent;
    private boolean     mSaveButtonClicked = false; /* To avoid action on reclicking  */
    private boolean     mAutoSaveEnabled;
    private boolean     mLoadingNote = true;    /* Is note loaded from database? */

    /**
     * Timeout finishes current activity, and hence executes onPause() and saveInstanceState().
     * Since state has already been saved with previous pause which started the timer, this variable
     * helps to avoids saving note again unnecessarily.
     */
    private boolean     mTimedOut = false;

    // Views in activity
    private EditText    mTitleView;
    private ViewStub    mContentStub;   /* Inflated by appropriate view to edit given note type */
    private TextView    mEditedView;    /* Show last edited date/time */
    private NoteView    mNoteView;
    private TagEditText mTagEditText;    /* Chips view editor for tags */

    private int         mBackgroundColor;

    /**
     * Start a new NoteActivity with given note id.
     *
     * TODO: Take note object to make loading faster
     *
     * @param context   Context to use
     * @param id        Id of note. -1 for new note.
     */
    public static void startForNoteId(Context context, int id, Note.Type type) {
        if (id != -1 && type != null) {
            throw new IllegalArgumentException("We don't need Note.Type when given an id");
        }

        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra("NOTE_ID", id);

        if (type != null) {
            intent.putExtra("NOTE_TYPE", type.ordinal());
        }

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

        if (!Misc.isPasswordLoaded()){
            Misc.startPasswordActivity(this);
            return;
        }

        // Even though we change content view later, we secure window as soon as possible
        Misc.secureWindow(NoteActivity.this);

        mBackgroundColor = 0;
        mAutoSaveEnabled = PreferenceHandler.isAutosaveEnabled(NoteActivity.this);

        int id = -1;
        int typeInt = -1;
        Note bundledNote = null;

        if (savedInstanceState != null) {
            id = savedInstanceState.getInt("NOTE_ID", -1);
            bundledNote = savedInstanceState.getParcelable("NOTE");
        }

        if (id == -1) {
            Bundle extras = getIntent().getExtras();
            id = extras.getInt("NOTE_ID", -1);
            typeInt = extras.getInt("NOTE_TYPE", -1);
        }

        if (typeInt == -1) {
            mNoteType = Note.Type.TYPE_GENERIC;
        } else {
            mNoteType = Note.Type.values()[typeInt];
        }

        if (id != -1 && savedInstanceState != null && bundledNote != null) {
            Log.d(TAG, "Unsaved existing note being retrieved from bundle");
            mLoadingNote = false;
            init(false, bundledNote.getType());
            loadNote(bundledNote);
        } else if (id != -1) {
            // existing note. Start an async task to load from storage
            new NoteLoadTask().execute(id);
        } else {
            Log.d(TAG, "Creating new note");
            mLoadingNote = false;
            init(true, mNoteType); // new note simply setup views
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    /**
     * Initialize views, listeners and update references
     */
    private void init(boolean isNewNote, Note.Type type) {
        Misc.secureWindow(NoteActivity.this);

        mTitleView = (EditText) findViewById(R.id.note_activity_title);
        mEditedView = (TextView) findViewById(R.id.note_activity_edited);
        mTagEditText = (TagEditText) findViewById(R.id.note_activity_tags);
        mNoteView = inflateNoteContentView(type);
        mNoteType = type;

        // load suggestions
        // TODO: Do this asynchronously if required
        mTagEditText.loadSuggestions(SealnoteApplication.getDatabase().getAllTags().keySet());
        mTagEditText.setThreshold(1);

        // TextWatcher to update share intent
        mNoteView.addTextChangedListener(mNoteTextWatcher); //LOOK
        mTitleView.addTextChangedListener(mNoteTextWatcher);

        mTitleView.setTypeface(FontCache.getFont(this, PreferenceHandler.getFontBold()));
        mTagEditText.setTypeface(FontCache.getFont(this, PreferenceHandler.getFontDefault()));

        // set focus to text view //TODO: Only if id=-1
        if (isNewNote) {
            ((View) mNoteView).requestFocus();
        } else {
            mTitleView.requestFocus();
        }

        //NOTE: For ICS
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Inflate the content view stub
     */
    private NoteView inflateNoteContentView(Note.Type type) {
        mContentStub = (ViewStub) findViewById(R.id.notes_view_stub);
        switch (type) {
            case TYPE_GENERIC:
                mContentStub.setLayoutResource(R.layout.note_type_generic);
                break;
            case TYPE_CARD:
                mContentStub.setLayoutResource(R.layout.note_type_card);
                break;
            case TYPE_LOGIN:
                mContentStub.setLayoutResource(R.layout.note_type_login);
                break;
            default:
                mContentStub.setLayoutResource(R.layout.note_type_generic);
                break;
        }
        return (NoteView) mContentStub.inflate();
    }


    /**
     * Update views with given note values
     */
    private void loadNote(Note note) {
        mNote = note;
        mTitleView.setText(mNote.getTitle());
        mNoteView.setNoteContent(mNote.getNote());
        mTagEditText.setTagSet(mNote.loadGetTags());

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


        if (mNote.getIsDeleted()) {
            mTitleView.setEnabled(false);
            ((View) mNoteView).setEnabled(false);
        }
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
            noteId = (mNote == null) ?-1 :mNote.getId(); // No changes made to new note
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.note_activity_actionbar, menu);
        return true;
    }

    /**
     * Prepare action menu as per current folder
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveMenuItem = menu.findItem(R.id.action_save_note);
        MenuItem deleteMenuItem = menu.findItem(R.id.action_note_delete);
        MenuItem archiveMenuItem = menu.findItem(R.id.action_archive);
        MenuItem unarchiveMenuItem = menu.findItem(R.id.action_unarchive);
        MenuItem colorMenuItem = menu.findItem(R.id.action_color);
        MenuItem restoreMenuItem = menu.findItem(R.id.action_restore);
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // check if autosave is enabled and set visibility of action
        saveMenuItem.setVisible(!mAutoSaveEnabled);

        // don't show delete action if note is newly created
        if (mNote == null || mNote.getId() == -1) {
            deleteMenuItem.setVisible(false);
            archiveMenuItem.setVisible(false);
            restoreMenuItem.setVisible(false);
            unarchiveMenuItem.setVisible(false);
        } else {
            saveMenuItem.setVisible(!mAutoSaveEnabled && !mNote.getIsDeleted());
            deleteMenuItem.setVisible(!mNote.getIsDeleted());
            archiveMenuItem.setVisible(mNote.getIsLive());
            unarchiveMenuItem.setVisible(mNote.getIsArchived() && !mNote.getIsDeleted());
            colorMenuItem.setVisible(!mNote.getIsDeleted());
            shareItem.setVisible(!mNote.getIsDeleted());
            restoreMenuItem.setVisible(mNote.getIsDeleted());
        }

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        mShareIntent = new Intent(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        shareActionProvider.setShareIntent(mShareIntent);
        updateShareIntent();

        return super.onPrepareOptionsMenu(menu);
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
        String shareText;

        if (mNoteView != null && titleView != null) {
            shareText = titleView.getText().toString() + "\n\n" + mNoteView.getNoteContent().toString();
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
                ColorDialogFragment cdf = new ColorDialogFragment(mBackgroundColor);
                cdf.show(getFragmentManager(), "ColorDialogFragment");
                return true;
            case R.id.action_archive:
                doArchive();
                return true;
            case R.id.action_note_delete:
                doDelete();
                return true;
            case R.id.action_unarchive:
                doUnarchive();
                return true;
            case R.id.action_restore:
                doRestore();
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
    public boolean saveNote() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        final String title = mTitleView.getText().toString();
        final NoteContent noteContent = mNoteView.getNoteContent();
        final String text = noteContent.toString();

        if (title.equals("") && text.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return false;
        }

        final boolean tagsChanged = mNote != null && mNote.getTags() != null &&
                Sets.symmetricDifference(mTagEditText.getTagSet(), mNote.getTags()).size() != 0;
        final boolean backgroundChanged = (mNote != null && mBackgroundColor != mNote.getColor());
        final boolean contentChanged = (
                (mNote != null) &&
                (!title.equals(mNote.getTitle()) || !text.equals(mNote.getNote().toString()))
        );

        final boolean anythingChanged = tagsChanged || backgroundChanged || contentChanged;

        if (mNote == null) {
            // this is a new note
            mNote = new Note();
        } else if (mAutoSaveEnabled && !anythingChanged) {
            // Also avoid unnecessarily updating the edit timestamp of note
            Log.d(TAG, "Note didn't change. No need to autosave");
            return false;
        }

        mNote.setTitle(title);
        mNote.setNote(noteContent);
        mNote.setColor(mBackgroundColor);
        mNote.setType(mNoteType);
        mNote.setTags(mTagEditText.getTagSet());

        if (mNote.getId() == -1) {
            mNote.setPosition(-1);
            int newNoteId = handler.addNote(mNote);
            mNote.setId(newNoteId);
        } else {
            // don't update timestamp when only background or tags or
            // only those two have changed
            handler.updateNote(mNote, contentChanged);
        }
        return true;
    }

    /**
     * Delete current note
     */
    public void doDelete() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        handler.trashNote(mNote.getId(), true);
        mNote = null;
        Toast.makeText(this, getResources().getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();

        // to disable saving when activity is finished when its
        // done in onPause()
        mAutoSaveEnabled = false;

        finish();
    }

    /**
     * Un-archive a note
     */
    public void doUnarchive() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        handler.archiveNote(mNote.getId(), false);
        mNote = null;
        Toast.makeText(this, getResources().getString(R.string.note_unarchived), Toast.LENGTH_SHORT).show();

        // to disable saving when activity is finished when its
        // done in onPause()
        mAutoSaveEnabled = false;

        finish();
    }

    /**
     * Archive current note
     */
    public void doArchive() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        handler.archiveNote(mNote.getId(), true);
        mNote = null;
        Toast.makeText(this, getResources().getString(R.string.note_archived), Toast.LENGTH_SHORT).show();

        // to disable saving when activity is finished when its
        // done in onPause()
        mAutoSaveEnabled = false;

        finish();
    }

    /**
     * Restores note from trash
     */
    public void doRestore() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        handler.trashNote(mNote.getId(), false);
        mNote = null;
        Toast.makeText(this, getResources().getString(R.string.note_restored), Toast.LENGTH_SHORT).show();

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
        final boolean noteSaved = saveNote();
        if (noteSaved) {
            Toast.makeText(this, getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        }
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

        if (PreferenceHandler.isNoteActivityBackgroundEnabled(this)) {
            View view = findViewById(R.id.note_activity_title).getRootView();
            view.setBackgroundColor(Misc.getColorForCode(this, color));
        } else {
            View colorStrip1 = findViewById(R.id.note_activity_color_strip);
            colorStrip1.setBackgroundColor(Misc.getColorForCode(this, color));
            colorStrip1.setVisibility(View.VISIBLE);
        }
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
            init(false, note.getType());
            loadNote(note);
            mLoadingNote = false;

            // Update action bar icons again as now we have loaded a
            // a note asynchronously and the previous set of icons
            // will probably be not correct due to delay
            invalidateOptionsMenu();
        }
    }
}