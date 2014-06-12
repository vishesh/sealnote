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
        Note note = DatabaseHandler.cursorToNote(cursor);
        card.setNote(note);
        card.init();
        return card;
    }
}
