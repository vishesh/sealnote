package com.twistedplane.sealnote;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.twistedplane.sealnote.data.Note;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

public class SealnoteCard extends Card {
    protected Note mNote;

    public SealnoteCard(Context context) {
        super(context, R.layout.cardcontent);
    }

    public void init() {
        this.mMultiChoiceEnabled = true;

        if (mNote != null && !mNote.getTitle().equals("")) {
            CardHeader header = new CardHeader(getContext());
            header.setTitle(mNote.getTitle());
            addCardHeader(header);
        }

        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                SealnoteCard sCard = (SealnoteCard) card;
                startNoteActivity(getContext(), sCard.getNote().getId());
            }
        });
    }

    public static void startNoteActivity(Context con, int id) {
        Intent intent = new Intent(con, NoteActivity.class);
        intent.putExtra("NOTE_ID", id);
        con.startActivity(intent);
    }

    public void setNote(Note note) {
        this.mNote = note;
    }

    public Note getNote() {
        return this.mNote;
    }

    private float getBigFontSize(String str) {
        // TODO: Make it not dumb
        int length = str.length();

        if (length > 100) {
            return 16;
        } else if (length < 100 && length > 70) {
            return 20;
        } else if (length < 70 && length > 30) {
            return 24;
        } else if (length < 30 && length > 20) {
            return 28;
        }
        return 34;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        TextView textView = (TextView) view.findViewById(R.id.cardcontent_note);
        String text = this.mNote.getNote();

        if (text != null && !text.equals("")) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(this.mNote.getNote());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getBigFontSize(text));
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
        }

        switch(mNote.getColor()) {
            case 0:
                this.setBackgroundResourceId(R.drawable.card_selector_color0);
                break;
            case 1:
                this.setBackgroundResourceId(R.drawable.card_selector_color1);
                break;
            case 2:
                this.setBackgroundResourceId(R.drawable.card_selector_color2);
                break;
            case 3:
                this.setBackgroundResourceId(R.drawable.card_selector_color3);
                break;
            case 4:
                this.setBackgroundResourceId(R.drawable.card_selector_color4);
                break;
            case 5:
                this.setBackgroundResourceId(R.drawable.card_selector_color5);
                break;
            case 6:
                this.setBackgroundResourceId(R.drawable.card_selector_color6);
                break;
            case 7:
                this.setBackgroundResourceId(R.drawable.card_selector_color7);
                break;
            case 8:
                this.setBackgroundResourceId(R.drawable.card_selector_color8);
                break;
        }
    }
}
