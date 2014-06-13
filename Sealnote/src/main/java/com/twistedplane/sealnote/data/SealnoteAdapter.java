package com.twistedplane.sealnote.data;

import android.content.Context;
import android.database.Cursor;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.SealnoteCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

public class SealnoteAdapter extends CardGridStaggeredCursorMultiChoiceAdapter {
    ActionMode mActionMode;

    public SealnoteAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    protected Card getCardFromCursor(Cursor cursor) {
        SealnoteCard card = new SealnoteCard(super.getContext());
        Note note = DatabaseHandler.cursorToNote(cursor);
        card.setNote(note);
        card.init();
        return card;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mActionMode = mode;
        MenuInflater inflator = mode.getMenuInflater();
        inflator.inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked, CardView cardView, Card card) {
        Toast.makeText(getContext(), "Click;" + position + " - " + checked, Toast.LENGTH_SHORT).show();
        onItemCheckedStateChanged(actionMode, position, id, checked);
        cardView.setActivated(true);
    }

    @Override
    public int getPosition(Card card) {
        SealnoteCard sCard = (SealnoteCard) card;
        return sCard.getNote().getId();
    }
}
