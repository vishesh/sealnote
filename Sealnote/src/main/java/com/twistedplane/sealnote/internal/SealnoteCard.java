package com.twistedplane.sealnote.internal;

import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.Note;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.PreferenceHandler;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;


/**
 * CardHeader for SealNote Card. Just changes typeface of header title.
 * Not using XML for API Level 14 compatibility
 */
class SealnoteCardHeader extends CardHeader {
    public static final String TAG = "SealnoteCardHeader";
    public SealnoteCardHeader(Context context) {
        super(context);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        if (view != null){
            TextView titleView = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
            if (titleView != null) {
                titleView.setTypeface(FontCache.getFont(getContext(), PreferenceHandler.getFontBold()));
            }
        }
    }
}

/**
 * Extends Card to glue with Note class. Uses Note for card content,
 * and adds note background to Card.
 */
public class SealnoteCard extends Card {
    public static final String TAG = "SealnoteCard";

    protected Note mNote;

    public SealnoteCard(Context context) {
        super(context, R.layout.cardcontent);
    }

    /**
     * Takes a note, create a SealnoteCard and initialize it.
     * @param context   Context to use
     * @param note      Note to attach with card
     */
    public SealnoteCard(Context context, Note note) {
        super(context, R.layout.cardcontent);
        setNote(note);
        init();
    }

    /**
     * Initialize Card using custom header and Note data.
     */
    public void init() {
        this.setCheckable(true);

        if (mNote != null && !mNote.getTitle().equals("")) {
            CardHeader header = new SealnoteCardHeader(getContext());
            header.setTitle(mNote.getTitle());
            addCardHeader(header);
        }
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

    /**
     * Try to guess a good font size as per the given string that
     * fits well and looks nice on card content view.
     *
     * @param str   String for which font size is to be calculated
     * @return      Font size in sp
     */
    private int getBigFontSize(String str) {
        // TODO: Make it not dumb
        int length = str.length();

        if (length >= 150) {
            return 16;
        } else if (length >= 80 && length < 150) {
            return 18;
        } else if (length >= 20 && length < 80) {
            return 24;
        } else if (length >= 15 && length < 20) {
            return 26;
        }
        return 30;
    }

    /**
     * Setup card content views and backgrounds
     *
     * @param parent    Parent view
     * @param view      Card view
     */
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        TextView textView = (TextView) view.findViewById(R.id.cardcontent_note);
        String text = this.mNote.getNote().getCardStringCached();
        int textSize = -1;

        // We make text view gone in content is there is no text show to avoid them
        // taking space that shows nothing
        if (text != null && !text.equals("")) {
            textView.setVisibility(View.VISIBLE);

            // Trim and set note
            if (mNote.getType() != Note.Type.TYPE_GENERIC) {
                //FIXME: Clean this shit up
                // Since only TYPE_LOGIN gives HTML content. For rest it would eat the
                // newlines and show everything together, which sucks.
                textView.setText(Html.fromHtml(text.trim()));
            } else {
                //
                textView.setText(text.trim());
            }

            // Dynamic Text Size
            if (PreferenceHandler.isDynamicFontSizeEnabled(getContext())) {
                textSize = getBigFontSize(text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            }
        } else {
            textView.setText("");
            textView.setVisibility(View.GONE);
        }

        if (textSize > 18) {
            textView.setTypeface(FontCache.getFont(getContext(), PreferenceHandler.getFontLight()));
        } else {
            textView.setTypeface(FontCache.getFont(getContext(), PreferenceHandler.getFontDefault()));
        }

        // set background for card as per given
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
            default:
                this.setBackgroundResourceId(R.drawable.card_selector_color0);
                break;
        }
    }
}
