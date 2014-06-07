package com.twistedplane.sealnote;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.twistedplane.sealnote.data.Note;
import it.gmariotti.cardslib.library.internal.Card;

public class SealnoteCard extends Card {
    protected Note mNote;

    public SealnoteCard(Context context) {
        super(context, R.layout.cardcontent);
        init();
    }

    protected void init() {
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

    private float getBigFontSize(int width, String str) {
        // TODO: Make it not dumb
        int length = str.length();
        int calculated = width / length;

        if (calculated > 38) {
            return 38.0f;
        } else if (calculated < 18.0f) {
            return 18.0f;
        }
        return calculated;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        TextView textView = (TextView) view.findViewById(R.id.cardcontent_note);
        TextView titleView = (TextView) view.findViewById(R.id.cardcontent_title);

        String title = this.mNote.getTitle();
        if (title != null && !title.equals("")) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(this.mNote.getTitle());
        } else {
            titleView.setText("");
            titleView.setVisibility(View.GONE);
        }

        String text = this.mNote.getNote();
        if (text != null && !text.equals("")) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(this.mNote.getNote());
            // TODO: Dynamic font size for titles
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
        }

        if (mNote.getColor() != -1) {
            parent.setBackgroundColor(mNote.getColor());
        }
    }
}
