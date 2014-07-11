package com.twistedplane.sealnote.data;

import android.database.Cursor;
import android.widget.ListAdapter;

public interface SealnoteAdapter extends ListAdapter {
    public Cursor   getCursor();
    public void     clearCursor();
    public void     changeCursor(Cursor cursor);

    public void     setCurrentFolder(Note.Folder folder);

    public void     startActionMode();
}
