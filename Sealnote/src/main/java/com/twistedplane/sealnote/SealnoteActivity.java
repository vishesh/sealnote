package com.twistedplane.sealnote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.views.SealnoteCardGridStaggeredView;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;

public class SealnoteActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    public static SealnoteAdapter adapter;
    public static SealnoteActivity activity;

    SealnoteCardGridStaggeredView noteListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;

        // setup database and password
        if (DatabaseHandler.getPassword() == null) {
            Intent intent = new Intent(this, PasswordActivity.class);
            this.startActivity(intent);
            this.finish();
            return;
        }

        /* Setup adapter for notes grid view */
        final DatabaseHandler db = new DatabaseHandler(this);
        final Cursor cursor = db.getAllNotesCursor();
        adapter = new SealnoteAdapter(this, cursor);

        noteListView = (SealnoteCardGridStaggeredView) findViewById(R.id.main_note_grid);
        //noteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //noteListView.setAdapter(adapter);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final CardGridStaggeredView noteListView = (CardGridStaggeredView) findViewById(R.id.main_note_grid);

        setScaleAnimationAdapter();

        AnimationAdapter animationAdapter = (AnimationAdapter) noteListView.getAdapter();
        SealnoteAdapter dataAdapter = (SealnoteAdapter) animationAdapter.getDecoratedBaseAdapter();

        dataAdapter.swapCursor(new DatabaseHandler(this).getAllNotesCursor());
    }

    private void setScaleAnimationAdapter() {
        AnimationAdapter animCardArrayAdapter = new ScaleInAnimationAdapter(adapter);

        animCardArrayAdapter.setAnimationDurationMillis(1000);
        animCardArrayAdapter.setAnimationDelayMillis(500);

        animCardArrayAdapter.setAbsListView(noteListView);
        noteListView.setExternalAdapter(animCardArrayAdapter, adapter);
    }
}
