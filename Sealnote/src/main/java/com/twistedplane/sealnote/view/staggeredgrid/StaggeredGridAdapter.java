package com.twistedplane.sealnote.view.staggeredgrid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.view.*;
import com.twistedplane.sealnote.NoteActivity;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.data.CardGridStaggeredCursorAdapter;
import com.twistedplane.sealnote.data.DatabaseHandler;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.data.SealnoteAdapter;
import com.twistedplane.sealnote.internal.SealnoteCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Adapter for SealnoteStaggeredGrid. Initialized cards and their event listeners. Since
 * StaggeredGrid doesn't have multi-choice mode enabled, we emulate this by adding
 * appropriate listeners to card and maintaining the CAB multi-choice state.
 */
public class StaggeredGridAdapter extends CardGridStaggeredCursorAdapter implements SealnoteAdapter {
    public final static String TAG = "SealnoteAdapter";

    private ActionMode mActionMode;
    private Note.Folder mCurrentFolder;

    /**
     * Array of all checked items in view for emulating multi-choice mode
     */
    final private SparseBooleanArray mCheckedIds = new SparseBooleanArray();
    private MultiChoiceCallback mMultiChoiceCallback; // Is instantiated only once per adapter.

    public StaggeredGridAdapter(Context context, Cursor c) {
        super(context, c, 0);
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
        CardView cardView = (CardView) result;
        setItemChecked(cardView, getItemChecked(cardView));
        return result;
    }

    /**
     * Called by adapter to convert first row in cursor to Card.
     *
     * @param cursor    Cursor to be converted to note
     * @return          SealnoteCard object initialized with CAB listeners and data
     */
    @Override
    protected Card getCardFromCursor(Cursor cursor) {
        SealnoteCard card = new SealnoteCard(super.getContext());
        Note note = DatabaseHandler.cursorToNote(cursor);

        card.setNote(note);
        card.init();
        initCardListeners(card);

        return card;
    }

    /**
     * Sets click and long-click listeners for CAB multi-mode emulation.
     *
     * @param card  Card to be initialized.
     */
    protected void initCardListeners(Card card) {
        card.setOnClickListener(new Card.OnCardClickListener() {
            /**
             * If action mode is not started Show current note attached with in
             * full editor window. If currently in action mode, toggle the
             * CardView checked state.
             *
             * @param card  Card object attached with the view clicked.
             * @param view  CardView object.
             */
            @Override
            public void onClick(Card card, View view) {
                SealnoteCard sCard = (SealnoteCard) card;
                if (mActionMode == null) {
                    NoteActivity.startForNoteId(getContext(), sCard.getNote().getId(), null);
                } else {
                    // Currently in action mode. Set the toggle current items checked state.
                    setItemChecked((CardView) view, !getItemChecked((CardView) view));

                    if (mCheckedIds.size() == 0) {
                        // Last checked item is now unchecked.
                        mActionMode.finish();
                    }
                }
            }
        });

        card.setOnLongClickListener(new Card.OnLongCardClickListener() {
            /**
             * Start emulated action mode for multi-mode selections. If action
             * is already started, toggle the checked status or card.
             *
             * @param card  Card object attached with the view clicked.
             * @param view  CardView object.
             * @return      true
             */
            @Override
            public boolean onLongClick(Card card, View view) {
                if (mActionMode == null) {
                    startActionMode();
                }
                setItemChecked((CardView) view, !getItemChecked((CardView) view));
                return true;
            }
        });
    }

    /**
     * Sets current folder in view
     */
    @Override
    public void setCurrentFolder(Note.Folder folder) {
        mCurrentFolder = folder;
    }

    /**
     * Start action mode emulating multi-choice mode.
     */
    @Override
    public void startActionMode() {
        if (mMultiChoiceCallback == null) {
            mMultiChoiceCallback = new MultiChoiceCallback();
        }
        // We assume activity is used as context
        Activity activity = (Activity) getContext();
        mActionMode = activity.startActionMode(mMultiChoiceCallback);
    }

    /**
     * Set card checked state and update the internal state
     *
     * @param card      CardView object whose state is to be updated
     * @param value     Checked or not
     */
    public void setItemChecked(CardView card, boolean value) {
        int id = Integer.parseInt(card.getCard().getId());

        card.setSelected(value);
        card.setActivated(value);

        // update the array containing ids of checked cards
        if (value) {
            mCheckedIds.put(id, true);
        } else {
            mCheckedIds.delete(id);
        }

        // To force redrawing the actionbar view and show updated number of
        // selected items
        if (mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    public boolean getItemChecked(CardView card) {
        int id = Integer.parseInt(card.getCard().getId());
        return mCheckedIds.get(id, false);
    }

    /**
     * Return number of checked items.
     */
    public int getSelectedItemCount() {
        return mCheckedIds.size();
    }

    /**
     * ActionMode callback used by mActionMode for emulating  multi-choice.
     * Handles the events related to actionbar such as creation, actionbar
     * icon clicks.
     */
    class MultiChoiceCallback implements ActionMode.Callback {
        /**
         * Inflate context menu and update mActionMode
         */
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
            mActionMode.setTitle(Integer.toString(getSelectedItemCount()));

            MenuItem archiveItem = menu.findItem(R.id.action_archive);
            MenuItem unarchiveItem = menu.findItem(R.id.action_unarchive);
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            MenuItem restoreItem = menu.findItem(R.id.action_restore);

            if (mCurrentFolder == Note.Folder.FOLDER_LIVE) {
                archiveItem.setVisible(true);
                unarchiveItem.setVisible(false);
                deleteItem.setVisible(true);
                restoreItem.setVisible(false);
            } else if (mCurrentFolder == Note.Folder.FOLDER_ARCHIVE) {
                archiveItem.setVisible(false);
                unarchiveItem.setVisible(true);
                deleteItem.setVisible(true);
                restoreItem.setVisible(false);
            } else if (mCurrentFolder == Note.Folder.FOLDER_TRASH) {
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
            mCheckedIds.clear();
            mActionMode = null;
            notifyDataSetChanged();
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
                    dialog.setMax(mCheckedIds.size());
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

                    for (int i = 0; i < mCheckedIds.size(); i++) {
                        publishProgress(i);
                        int key = mCheckedIds.keyAt(i);
                        if (mCheckedIds.get(key)) {
                            if (action == Note.FolderAction.NOTE_DELETE) {
                                if (mCurrentFolder == Note.Folder.FOLDER_TRASH) {
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
                    mCheckedIds.clear();
                    mActionMode.finish();
                    dialog.dismiss();

                    notifyDataSetInvalidated();
                }
            }.execute(mCheckedIds);
        }
    }
}
