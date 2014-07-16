package com.twistedplane.sealnote.view.staggeredgrid;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.twistedplane.sealnote.NoteActivity;
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
    public final static String TAG = "StaggeredGridAdapter";

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
                if (mMultiChoiceCallback == null || !mMultiChoiceCallback.isActionModeActive()) {
                    NoteActivity.startForNoteId(getContext(), sCard.getNote().getId(), null);
                } else {
                    // Currently in action mode. Set the toggle current items checked state.
                    setItemChecked((CardView) view, !getItemChecked((CardView) view));

                    if (mCheckedIds.size() == 0) {
                        // Last checked item is now unchecked.
                        mMultiChoiceCallback.getActionMode().finish();
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
                if (mMultiChoiceCallback == null || mMultiChoiceCallback.getActionMode() == null) {
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
    public void setFolder(Note.Folder folder) {
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
        activity.startActionMode(mMultiChoiceCallback);
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
        if (mMultiChoiceCallback != null && mMultiChoiceCallback.isActionModeActive()) {
            mMultiChoiceCallback.getActionMode().invalidate();
        }
    }

    public boolean getItemChecked(CardView card) {
        int id = Integer.parseInt(card.getCard().getId());
        return mCheckedIds.get(id, false);
    }

    /**
     * ActionMode callback used by mActionMode for emulating  multi-choice.
     * Handles the events related to actionbar such as creation, actionbar
     * icon clicks.
     */
    class MultiChoiceCallback extends com.twistedplane.sealnote.internal.MultiChoiceCallback {
        /**
         * Inflate context menu and update mActionMode
         */
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            mActionMode = actionMode;
            return true;
        }

        @Override
        protected Context getContext() {
            return StaggeredGridAdapter.this.getContext();
        }

        /**
         * Return number of checked items.
         */
        @Override
        protected int getSelectedItemCount() {
            return mCheckedIds.size();
        }

        @Override
        protected SparseBooleanArray getSelectedItems() {
            return mCheckedIds;
        }

        @Override
        protected Note.Folder getCurrentFolder() {
            return mCurrentFolder;
        }

        @Override
        protected void done() {
            mActionMode = null;
            mCheckedIds.clear();
            notifyDataSetChanged();
        }

        @Override
        protected void notifyDataSetChanged() {
            StaggeredGridAdapter.this.notifyDataSetChanged();
        }

        @Override
        protected void notifyDataSetInvalidated() {
            StaggeredGridAdapter.this.notifyDataSetInvalidated();
        }
    }
}
