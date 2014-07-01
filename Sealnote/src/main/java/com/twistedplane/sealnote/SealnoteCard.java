package com.twistedplane.sealnote;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.PreferenceHandler;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;


class SealnoteCardHeader extends CardHeader {
    public SealnoteCardHeader(Context context) {
        super(context);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        if (view != null){
            TextView titleView = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
            if (titleView != null) {
                titleView.setTypeface(FontCache.getFont(getContext(), "RobotoSlab-Bold.ttf"));
            }
        }
    }
}

public class SealnoteCard extends Card {
    protected Note mNote;

    public SealnoteCard(Context context) {
        super(context, R.layout.cardcontent);
    }

    public void init() {
        this.setCheckable(true);

        if (mNote != null && !mNote.getTitle().equals("")) {
            CardHeader header = new SealnoteCardHeader(getContext());
            header.setTitle(mNote.getTitle());
            addCardHeader(header);
        }
    }

    public static void startNoteActivity(Context con, int id) {
        Intent intent = new Intent(con, NoteActivity.class);
        intent.putExtra("NOTE_ID", id);
        con.startActivity(intent);
    }

    public void setNote(Note note) {
        this.mNote = note;
    }

    @Override
    public void setId(String id) {
        this.mNote.setId(Integer.parseInt(id));
    }

    @Override
    public String getId() {
        return Integer.toString(this.mNote.getId());
    }

    public Note getNote() {
        return this.mNote;
    }

    private float getBigFontSize(String str) {
        // TODO: Make it not dumb
        int length = str.length();

        if ( length > 50) {
            return 18;
        } else if (length < 50 && length > 20) {
            return 24;
        } else if (length < 20 && length > 15) {
            return 26;
        }
        return 30;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        TextView textView = (TextView) view.findViewById(R.id.cardcontent_note);
        String text = this.mNote.getNote();

        if (text != null && !text.equals("")) {
            textView.setVisibility(View.VISIBLE);
            String trimmedText = this.mNote.getNote().trim();
            final int MAX_LENGTH = 400; //FIXME
            if (trimmedText.length() > MAX_LENGTH) {
                trimmedText = trimmedText.substring(0, MAX_LENGTH) + "...";
            }
            textView.setText(trimmedText);
            if (PreferenceHandler.isDynamicFontSizeEnabled(getContext())) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getBigFontSize(text));
            }
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
        }

        textView.setTypeface(FontCache.getFont(getContext(), "RobotoSlab-Light.ttf"));

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
        }
    }

    public void setMultichoiceEnabled() {
        this.mMultiChoiceEnabled = true;
    }
}
