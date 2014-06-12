package com.twistedplane.sealnote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;
import it.gmariotti.cardslib.library.internal.CardGridCursorAdapter;

public class SealnoteActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // setup database and password
        if (DatabaseHandler.getPassword() == null) {
            Intent intent = new Intent(this, PasswordActivity.class);
            this.startActivity(intent);
            this.finish();
            return;
        }

        final CardGridStaggeredView noteListView = (CardGridStaggeredView) findViewById(R.id.main_note_grid);

        /* Setup adapter for notes grid view */
        final DatabaseHandler db = new DatabaseHandler(this);
        final Cursor cursor = db.getAllNotesCursor();
        final SealnoteAdapter adapter = new SealnoteAdapter(this, cursor, 0);
        noteListView.setAdapter(adapter);
        noteListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
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
        CardGridCursorAdapter adapter = (CardGridCursorAdapter) noteListView.getAdapter();
        adapter.swapCursor(new DatabaseHandler(this).getAllNotesCursor());
    }
}
