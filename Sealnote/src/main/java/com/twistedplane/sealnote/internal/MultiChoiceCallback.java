package com.twistedplane.sealnote.internal;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;

/**
 * ActionMode callback used by mActionMode for emulating  multi-choice.
 * Handles the events related to actionbar such as creation, actionbar
 * icon clicks.
 */
abstract public class MultiChoiceCallback implements ActionMode.Callback {
    /**
     * Inflate context menu and update mActionMode
     */
    protected ActionMode mActionMode;

    protected abstract int                  getSelectedItemCount();
    protected abstract Note.Folder          getCurrentFolder();
    protected abstract SparseBooleanArray   getSelectedItems();
    protected abstract void                 done();
    protected abstract void                 notifyDataSetInvalidated();
    protected abstract void                 notifyDataSetChanged();
    protected abstract Context              getContext();

    public boolean isActionModeActive() {
        return mActionMode != null;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        mActionMode = actionMode;
        return true;
    }

    /**
     * Called when menu is invalidated. Updates the title that shows
     * count of selected items.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        actionMode.setTitle(Integer.toString(getSelectedItemCount()));

        MenuItem archiveItem = menu.findItem(R.id.action_archive);
        MenuItem unarchiveItem = menu.findItem(R.id.action_unarchive);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem restoreItem = menu.findItem(R.id.action_restore);

        Note.Folder currentFolder = getCurrentFolder();
        if (currentFolder == Note.Folder.FOLDER_LIVE) {
            archiveItem.setVisible(true);
            unarchiveItem.setVisible(false);
            deleteItem.setVisible(true);
            restoreItem.setVisible(false);
        } else if (currentFolder == Note.Folder.FOLDER_ARCHIVE) {
            archiveItem.setVisible(false);
            unarchiveItem.setVisible(true);
            deleteItem.setVisible(true);
            restoreItem.setVisible(false);
        } else if (currentFolder == Note.Folder.FOLDER_TRASH) {
            archiveItem.setVisible(false);
            unarchiveItem.setVisible(false);
            deleteItem.setVisible(true);
            restoreItem.setVisible(true);
        }

        return true;
    }

    /**
     * A menu item was clicked. Do appropriate action.
     */
    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                actionOnSelectedNotes(Note.FolderAction.NOTE_DELETE);
                break;
            case R.id.action_restore:
                actionOnSelectedNotes(Note.FolderAction.NOTE_RESTORE);
                break;
            case R.id.action_archive:
                actionOnSelectedNotes(Note.FolderAction.NOTE_ARCHIVE);
                break;
            case R.id.action_unarchive:
                actionOnSelectedNotes(Note.FolderAction.NOTE_UNARCHIVE);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * ActionMode is finished. Reset the state, like actionmode handle and
     * selected items array.
     */
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        done();
    }

    /**
     * Delete all selected notes. Done asynchronously with a dialog shown with progress.
     */
    void actionOnSelectedNotes(final Note.FolderAction action) {
        new AsyncTask<SparseBooleanArray, Integer, Void>() {
            final ProgressDialog dialog = new ProgressDialog(getContext());

            /**
             * Create and show dialog.
             */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMax(getSelectedItemCount());
                dialog.setTitle("Deleting notes.");
                dialog.setProgress(0);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.show();
            }

            /**
             * Delete each note one by one and update state in adapter.
             *
             * @param sparseBooleanArrays   Contains the selected item ids
             * @return                      Cursor to updated dataset after deletion
             */
            @Override
            protected Void doInBackground(SparseBooleanArray... sparseBooleanArrays) {
                DatabaseHandler db = SealnoteApplication.getDatabase();
                SparseBooleanArray mCheckedIds = sparseBooleanArrays[0];

                for (int i = 0; i < mCheckedIds.size(); i++) {
                    publishProgress(i);
                    int key = mCheckedIds.keyAt(i);
                    if (mCheckedIds.get(key)) {
                        if (action == Note.FolderAction.NOTE_DELETE) {
                            if (getCurrentFolder() == Note.Folder.FOLDER_TRASH) {
                                db.deleteNote(key);
                            } else {
                                db.trashNote(key, true);
                            }
                        } else if (action == Note.FolderAction.NOTE_ARCHIVE) {
                            db.archiveNote(key, true);
                        } else if (action == Note.FolderAction.NOTE_UNARCHIVE) {
                            db.archiveNote(key, false);
                        } else if (action == Note.FolderAction.NOTE_RESTORE) {
                            db.trashNote(key, false);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                dialog.setProgress(values[0]);
            }

            /**
             * After deleting notes, invalidate the dataset which
             * will notify to reload the dataset.
             *
             * Deletion leads to finish of actionmode.
             */
            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                mActionMode.finish();
                dialog.dismiss();
                notifyDataSetInvalidated();
            }
        }.execute(getSelectedItems());
    }
}