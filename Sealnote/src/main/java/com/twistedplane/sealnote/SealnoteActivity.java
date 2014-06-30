package com.twistedplane.sealnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.LinearLayout;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.views.SealnoteCardGridStaggeredView;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;

//FIXME: Secure window. Clean up code and update flag on settings changed.

public class SealnoteActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    public static SealnoteAdapter adapter;
    public static SealnoteActivity activity;

    SealnoteCardGridStaggeredView noteListView;
    private boolean mAdapterLoaded = false;

    private void secureWindow() {
        // secure window content
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSecureWindow = sharedPrefs.getBoolean("pref_secureWindow", false);
        if (isSecureWindow) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminate(true);

        setContentView(R.layout.main);
        secureWindow();

        activity = this;
        noteListView = (SealnoteCardGridStaggeredView) findViewById(R.id.main_note_grid);
        final LinearLayout layoutProgressHeader = (LinearLayout) findViewById(R.id.layoutHeaderProgress);

        if (DatabaseHandler.getPassword() == null) {
            // onResume will follow up which will start PasswordActivity and setup database password
            return;
        }

        AsyncTask<Void, Void, SealnoteAdapter> task = new AsyncTask<Void, Void, SealnoteAdapter>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                noteListView.setVisibility(View.GONE);
                layoutProgressHeader.setVisibility(View.VISIBLE);
            }

            @Override
            protected SealnoteAdapter doInBackground(Void... voids) {
                final DatabaseHandler db = new DatabaseHandler(SealnoteActivity.this);
                final Cursor cursor = db.getAllNotesCursor();
                return new SealnoteAdapter(SealnoteActivity.this, cursor);
            }

            @Override
            protected void onPostExecute(SealnoteAdapter sealnoteAdapter) {
                super.onPostExecute(sealnoteAdapter);

                adapter = sealnoteAdapter;
                mAdapterLoaded = true;
                loadGridAdapter();

                noteListView.setVisibility(View.VISIBLE);
                layoutProgressHeader.setVisibility(View.GONE);
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_note:
                SealnoteCard.startNoteActivity(this, -1);
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

    private void loadGridAdapter() {
        if (mAdapterLoaded) {
            final CardGridStaggeredView noteListView = (CardGridStaggeredView) findViewById(R.id.main_note_grid);

            setScaleAnimationAdapter();

            AnimationAdapter animationAdapter = (AnimationAdapter) noteListView.getAdapter();
            SealnoteAdapter dataAdapter = (SealnoteAdapter) animationAdapter.getDecoratedBaseAdapter();

            dataAdapter.swapCursor(new DatabaseHandler(this).getAllNotesCursor());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TimeoutHandler.instance().resume(this)) {
            return;
        }
        loadGridAdapter();
        secureWindow();
    }

    @Override
    public void onPause() {
        super.onPause();
        TimeoutHandler.instance().pause(this);
    }

    private void setScaleAnimationAdapter() {
        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(adapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(noteListView);
        noteListView.setExternalAdapter(animCardArrayAdapter, adapter);
    }

    private void showAboutDialog() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }
}
