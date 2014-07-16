package com.twistedplane.sealnote.view.simplelist;

import android.content.Context;
import android.database.Cursor;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;

/**
 * Adapter for SealnoteStaggeredGrid. Initialized cards and their event listeners. Since
 * StaggeredGrid doesn't have multi-choice mode enabled, we emulate this by adding
 * appropriate listeners to card and maintaining the CAB multi-choice state.
 */
public class SimpleListAdapter extends SimpleCursorAdapter implements SealnoteAdapter {
    public final static String TAG = "SealnoteAdapter";

    private ActionMode mActionMode;
    private Note.Folder mCurrentFolder;

    /**
     * Array of all checked items in view for emulating multi-choice mode
     */

    public SimpleListAdapter(Context context, Cursor cursor) {
        super(
                context,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {DatabaseHandler.COL_TITLE},  /* From columns */
                new int[] {android.R.id.text1}             /* Mapped resource ids in layout to columns */
        );
    }

    /**
     * Close the cursor held by adapter
     */
    @Override
    public void clearCursor() {
        Cursor cursor = swapCursor(null);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO: Check the implementation in array version of this adapter
        View result = super.getView(position, convertView, parent);
//        CardView cardView = (CardView) result;
//        setItemChecked(cardView, getItemChecked(cardView));
        return result;
    }

    /**
     * Sets current folder in view
     */
    @Override
    public void setCurrentFolder(Note.Folder folder) {
        mCurrentFolder = folder;
    }

    @Override
    public void startActionMode() {
        //
    }
}

