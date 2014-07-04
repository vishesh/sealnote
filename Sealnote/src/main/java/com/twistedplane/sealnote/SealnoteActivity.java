package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;

//FIXME: Clean up code and update flag on settings changed.

/**
 * Main activity where all cards are listed in a staggered grid
 */
public class SealnoteActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    public static SealnoteAdapter adapter;
    public static SealnoteActivity activity;

    final private AdapterLoadTask adapterLoadTask = new AdapterLoadTask();
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
     * Is adapter loaded? Is set by task that loads adapter asynchronously.
     * Prevents the code flow where resume executes before adapter is loaded
     */
    private boolean mAdapterLoaded = false;


    /**
     * Create activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        Misc.secureWindow(this);

        activity = this;
        mNoteListView = (SealnoteCardGridStaggeredView) findViewById(R.id.main_note_grid);
        mEmptyGridLayout = findViewById(R.id.layout_empty_grid);
        layoutProgressHeader = findViewById(R.id.layoutHeaderProgress);


        if (DatabaseHandler.getPassword() == null) {
            // onResume will follow up which will start PasswordActivity and setup database password
            return;
        }

        adapterLoadTask.execute();
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
        loadGridAdapter();

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

        // Close cursor used by adapter
        if (adapter != null) {
            Cursor cursor = adapter.swapCursor(null);
            if (cursor != null) {
                cursor.close();
            }
        }

        TimeoutHandler.instance().pause(this);
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
    private void loadGridAdapter() {
        if (mAdapterLoaded) {
            setAnimationAdapter();

            AnimationAdapter animationAdapter = (AnimationAdapter) mNoteListView.getAdapter();
            SealnoteAdapter dataAdapter = (SealnoteAdapter) animationAdapter.getDecoratedBaseAdapter();

            // get fresh data and swap
            dataAdapter.changeCursor(new DatabaseHandler(this).getAllNotesCursor());
            mNoteListView.requestLayout();
            mNoteListView.invalidate();
        } else {
            Log.w("DEBUG", "Adapter not loaded, view may misbehave?");
        }
    }

    /**
     * Set animation adapter for card grid view and make it card grid's external adapter.
     */
    private void setAnimationAdapter() {
        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(adapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(mNoteListView);
        mNoteListView.setExternalAdapter(animCardArrayAdapter, adapter);
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
        if (adapter.getCount() > 0) {
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
    private class AdapterLoadTask extends AsyncTask<Void, Void, SealnoteAdapter> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
        protected SealnoteAdapter doInBackground(Void... voids) {
            final DatabaseHandler db = new DatabaseHandler(SealnoteActivity.this);
            final Cursor cursor = db.getAllNotesCursor();
            return new SealnoteAdapter(SealnoteActivity.this, cursor);
        }

        /**
         * Takes the result of task ie. adapter and load it to the view.
         * Revert back the visibilities of views.
         *
         * @param sealnoteAdapter   Result of background task
         */
        @Override
        protected void onPostExecute(SealnoteAdapter sealnoteAdapter) {
            super.onPostExecute(sealnoteAdapter);

            adapter = sealnoteAdapter;
            mAdapterLoaded = true;

            /**
             * Called whenever there is change in dataset. Any future changes
             * will call this
             */
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    SealnoteActivity.this.onAdapterDataSetChanged();
                }
            });

            // Make the progress view gone and card grid visible
            layoutProgressHeader.setVisibility(View.GONE);

            SealnoteActivity.this.loadGridAdapter();
        }
    }
}
