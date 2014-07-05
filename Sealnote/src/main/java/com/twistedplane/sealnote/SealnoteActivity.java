package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.utils.Misc;
import com.twistedplane.sealnote.utils.TimeoutHandler;
import com.twistedplane.sealnote.views.SealnoteCardGridStaggeredView;

//FIXME: Clean up code and update flag on settings changed.

/**
 * Main activity where all cards are listed in a staggered grid
 */
public class SealnoteActivity extends Activity {
    public final static String TAG = "SealnoteActivity";

    /**
     * Adapter used by Staggered Grid View to display note cards
     */
    private SealnoteAdapter mAdapter = new SealnoteAdapter(this, null);
    private SealnoteCardGridStaggeredView mNoteListView;

    /**
     * View to show when AdapterLoadTask is running. Show activity
     * progress circle
     */
    private View layoutProgressHeader;

    /**
     * View shown where is no cards available.
     */
    private View mEmptyGridLayout;

    /**
     * If a task is already executing to load adapter
     */
    private boolean mAdapterLoading = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        Misc.secureWindow(this);

        mNoteListView = (SealnoteCardGridStaggeredView) findViewById(R.id.main_note_grid);
        mEmptyGridLayout = findViewById(R.id.layout_empty_grid);
        layoutProgressHeader = findViewById(R.id.layoutHeaderProgress);

        if (SealnoteApplication.getDatabase().getPassword() == null) {
            // onResume will follow up which will start PasswordActivity and setup database password
            return;
        }

        /**
         * Called whenever there is change in dataset. Any future changes
         * will call this
         */
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onAdapterDataSetChanged();
            }
        });
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

        if (!mAdapterLoading) {
            new AdapterLoadTask().execute();
        }

        // preference may have changed, so do it again. Happens when coming
        // back from settings activity
        Misc.secureWindow(this);
    }

    /**
     * Schedules a delayed callback after configured password expiry timeout.
     */
    @Override
    public void onPause() {
        super.onPause();
        mAdapter.clearCursor();
        TimeoutHandler.instance().pause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Inflate the ActionBar menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * An item is selected from ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_note:
                onCreateNoteClick(null);
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_logout:
                TimeoutHandler.instance().expire(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load adapter to card grid view. Reload data from database. Also setup animations.
     */
    private void loadAdapter() {
        setAnimationAdapter();

        // get fresh data and swap
        mNoteListView.requestLayout();
        mNoteListView.invalidate();
    }

    /**
     * Set animation adapter for card grid view and make it card grid's external adapter.
     */
    private void setAnimationAdapter() {
        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(mAdapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(mNoteListView);
        mNoteListView.setExternalAdapter(animCardArrayAdapter, mAdapter);
    }

    /**
     * Create and show about dialog
     */
    private void showAboutDialog() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    /**
     * Start settings activity
     */
    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    /**
     * Callback when dataset in card grid's adapter is changed.
     */
    private void onAdapterDataSetChanged() {
        if (mAdapter.getCount() > 0) {
            mEmptyGridLayout.setVisibility(View.GONE);
        } else {
            mEmptyGridLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when any create new button/action is clicked.
     */
    public void onCreateNoteClick(View view) {
        NoteActivity.startForNoteId(SealnoteActivity.this, -1);
    }

    /**
     * Asynchronous Task to load adapter.
     *
     * Hide the grid view in activity and shows layoutProgressHeader.
     */
    private class AdapterLoadTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapterLoading = true;
            mAdapter.clearCursor();

            // Before starting background task, update visibility of views
            layoutProgressHeader.setVisibility(View.VISIBLE);
        }

        /**
         * Loads database, get cursor to all notes in database and return a
         * new Cursor Adapter.
         *
         * @return  Adapter object containing all notes
         */
        @Override
        protected Cursor doInBackground(Void... voids) {
            final DatabaseHandler db = SealnoteApplication.getDatabase();
            final Cursor cursor = db.getAllNotesCursor();
            return cursor;
        }

        /**
         * Takes the result of task ie. adapter and load it to the view.
         * Revert back the visibilities of views.
         *
         * @param cursor   Result containing cursor to notes
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mAdapter.changeCursor(cursor);

            // Make the progress view gone and card grid visible
            layoutProgressHeader.setVisibility(View.GONE);

            SealnoteActivity.this.loadAdapter();
            mAdapterLoading = false;
        }
    }
}
