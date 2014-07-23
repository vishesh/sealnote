package com.twistedplane.sealnote.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.fragment.SealnoteFragment;

/**
 * Loader for loading notes in Adapter
 */
public class AdapterLoader extends AsyncTaskLoader<Cursor> {
    private Note.Folder currentFolder = Note.Folder.FOLDER_LIVE;
    private int tagid = -1;
    private Cursor mCursor;

    /**
     * Loads database, get cursor to all notes given folder in database
     */
    @Override
    public Cursor loadInBackground() {
        Log.d(SealnoteFragment.TAG, "Adapter loader started!");

        final DatabaseHandler db = SealnoteApplication.getDatabase();
        final Cursor cursor;

        switch (currentFolder) {
            case FOLDER_LIVE:
                cursor = db.getNotesCursor();
                break;
            case FOLDER_ARCHIVE:
                cursor = db.getArchivedNotesCursor();
                break;
            case FOLDER_TRASH:
                cursor = db.getDeletedNotesCursor();
                break;
            case FOLDER_TAG:
                cursor = db.getNotesCursor(tagid);
                break;
            default:
                cursor = null;
        }

        return cursor;
    }

    public AdapterLoader(Context context, Note.Folder currentFolder, int tagid) {
        super(context);
        Log.d(SealnoteFragment.TAG, "New adapter loader created");
        this.currentFolder = currentFolder;
        this.tagid = tagid;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
