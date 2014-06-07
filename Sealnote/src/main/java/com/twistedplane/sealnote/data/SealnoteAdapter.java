package com.twistedplane.sealnote.data;

import android.content.Context;
import android.database.Cursor;
import com.twistedplane.sealnote.SealnoteCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridCursorAdapter;

public class SealnoteAdapter extends CardGridCursorAdapter {
    public SealnoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    protected Card getCardFromCursor(Cursor cursor) {
        SealnoteCard card = new SealnoteCard(super.getContext());
        setCardFromCursor(card, cursor);
        return card;
    }

    private void setCardFromCursor(SealnoteCard card, Cursor cursor) {
        Note note = DatabaseHandler.cursorToNote(cursor);
        card.setNote(note);
    }
}
