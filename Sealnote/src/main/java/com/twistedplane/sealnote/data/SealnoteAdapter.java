package com.twistedplane.sealnote.data;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.*;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteActivity;
import com.twistedplane.sealnote.SealnoteCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

public class SealnoteAdapter extends CardGridStaggeredCursorAdapter {
    ActionMode mActionMode;
    MultiChoiceCallback mMultiChoiceCallback;
    SparseBooleanArray mCheckedIds = new SparseBooleanArray();

    public SealnoteAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    protected Card getCardFromCursor(Cursor cursor) {
        SealnoteCard card = new SealnoteCard(super.getContext());
        Note note = DatabaseHandler.cursorToNote(cursor);
        card.setNote(note);
        card.init();
        initCardListeners(card);
        return card;
    }

    protected void initCardListeners(Card card) {
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                SealnoteCard sCard = (SealnoteCard) card;
                if (mActionMode == null) {
                    SealnoteCard.startNoteActivity(getContext(), sCard.getNote().getId());
                } else {
                    setItemChecked((CardView) view, !getItemChecked((CardView) view));
                    mActionMode.invalidate();

                    if (mCheckedIds.size() == 0) {
                        mActionMode.finish();
                    }
                }
            }
        });

        card.setOnLongClickListener(new Card.OnLongCardClickListener() {
            @Override
            public boolean onLongClick(Card card, View view) {
                if (mActionMode == null) {
                    SealnoteActivity.adapter.startActionMode(SealnoteActivity.activity);
                }
                setItemChecked((CardView) view, !getItemChecked((CardView) view));
                mActionMode.invalidate();
                return true;
            }
        });
    }

    public void startActionMode(Activity activity) {
        if (mMultiChoiceCallback == null) {
            mMultiChoiceCallback = new MultiChoiceCallback();
        }
        mActionMode = activity.startActionMode(mMultiChoiceCallback);
    }

    public void setItemChecked(CardView card, boolean value) {
        int id = Integer.parseInt(card.getCard().getId());

        card.setSelected(value);
        card.setActivated(value);
        if (value) {
            mCheckedIds.put(id, value);
        } else {
            mCheckedIds.delete(id);
        }
    }

    public boolean getItemChecked(CardView card) {
        int id = Integer.parseInt(card.getCard().getId());
        return mCheckedIds.get(id, false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = super.getView(position, convertView, parent);
        CardView cardView = (CardView) result;
        setItemChecked(cardView, getItemChecked(cardView));
        return result;
    }

    public int getSelectedItemCount() {
        return mCheckedIds.size();
    }

    class MultiChoiceCallback implements ActionMode.Callback {
        void deleteSelectedNotes() {
            DatabaseHandler db = new DatabaseHandler(getContext());

            for (int i = 0; i < mCheckedIds.size(); i++) {
                int key = mCheckedIds.keyAt(i);
                if (mCheckedIds.get(key)) {
                    db.deleteNote(key);
                }
            }

            Cursor newCursor = db.getAllNotesCursor();
            SealnoteAdapter.this.swapCursor(newCursor);

            mCheckedIds.clear();
            notifyDataSetChanged();
            mActionMode.finish();
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_notes_delete:
                    deleteSelectedNotes();
                    break;
                default:
                    return false;
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            mActionMode = actionMode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            mActionMode.setTitle(Integer.toString(getSelectedItemCount()));
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mCheckedIds.clear();
            mActionMode = null;
            notifyDataSetChanged();
        }
    }
}
