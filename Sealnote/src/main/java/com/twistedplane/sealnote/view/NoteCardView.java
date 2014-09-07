package com.twistedplane.sealnote.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.twistedplane.sealnote.R;
import com.twistedplane.sealnote.data.NoteContent;
import com.twistedplane.sealnote.data.NoteContentCard;
import com.twistedplane.sealnote.utils.FontCache;
import com.twistedplane.sealnote.utils.PreferenceHandler;

/**
 * View for editing content of Note.Type = CARD
 */
public class NoteCardView extends LinearLayout implements NoteView {
    private EditText mCardName;
    private EditText mCardNumber;
    private EditText mCardValidFrom;
    private EditText mCardValidUpto;
    private EditText mCardCvv;
    private EditText mCardNote;
    private ImageView mCardBrand;

    public NoteCardView(Context context) {
        super(context);
        init();
    }

    public NoteCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize references to views within, configure them and add listeners
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.note_type_card_layout, this);

        mCardName = (EditText) findViewById(R.id.note_card_name);
        mCardNumber = (EditText) findViewById(R.id.note_card_number);
        mCardValidFrom = (EditText) findViewById(R.id.note_card_valid_from);
        mCardValidUpto = (EditText) findViewById(R.id.note_card_valid_upto);
        mCardCvv = (EditText) findViewById(R.id.note_card_cvv);
        mCardNote = (EditText) findViewById(R.id.note_card_note);
        mCardBrand = (ImageView) findViewById(R.id.note_card_brand);

        mCardNote.setTypeface(FontCache.getFont(getContext(), PreferenceHandler.getFontDefault())); //LOOK
        mCardNumber.addTextChangedListener(new FourDigitCardFormatWatcher());
    }

    /**
     * Returns NoteContent object with latest values from view
     */
    @Override
    public NoteContent getNoteContent() {
        return new NoteContentCard(
                mCardName.getText().toString(),
                mCardNumber.getText().toString(),
                mCardValidFrom.getText().toString(),
                mCardValidUpto.getText().toString(),
                mCardCvv.getText().toString(),
                mCardNote.getText().toString()
        );
    }

    /**
     * Load values into views from given NoteContent object.
     */
    @Override
    public void setNoteContent(NoteContent noteContent) {
       NoteContentCard noteContentCard = (NoteContentCard) noteContent;

        mCardName.setText(noteContentCard.getName());
        mCardNumber.setText(noteContentCard.getNumber());
        mCardValidFrom.setText(noteContentCard.getValidFrom());
        mCardValidUpto.setText(noteContentCard.getValidUpto());
        mCardCvv.setText(noteContentCard.getCvv());
        mCardNote.setText(noteContentCard.getAdditionalNote());
    }

    /**
     * Set enable attribute for each input component inside
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mCardName.setEnabled(enabled);
        mCardNumber.setEnabled(enabled);
        mCardValidFrom.setEnabled(enabled);
        mCardValidUpto.setEnabled(enabled);
        mCardCvv.setEnabled(enabled);
        mCardNote.setEnabled(enabled);
    }

    /**
     * Add text listener to each input component inside
     */
    @Override
    public void addTextChangedListener(TextWatcher textWatcher) {
        mCardName.addTextChangedListener(textWatcher);
        mCardNumber.addTextChangedListener(textWatcher);
        mCardValidUpto.addTextChangedListener(textWatcher);
        mCardValidFrom.addTextChangedListener(textWatcher);
        mCardCvv.addTextChangedListener(textWatcher);
        mCardNote.addTextChangedListener(textWatcher);
    }

    /**
     * Formats the watched EditText to a credit card number
     */
    public class FourDigitCardFormatWatcher implements TextWatcher {
        // Change this to what you want... ' ', '-' etc..
        final private static char space = '-';

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Remove spacing char
            if (s.length() > 0) {
                final char c = s.charAt(s.length() - 1);
                if (c == space) {
                    s.delete(s.length() - 1, s.length());
                }
            }

            // Insert char where needed.
            if (s.length() > 0 && (s.length() % 5) == 0) {
                char c = s.charAt(s.length() - 1);
                // Only if its a digit where there should be a space we insert a space
                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                    s.insert(s.length() - 1, String.valueOf(space));
                }
            }

            // Update the brand
            String cardBrand = NoteContentCard.getBrand(mCardNumber.getText().toString());
            int brandDrawable = NoteContentCard.getBrandDrawableId(cardBrand);
            mCardBrand.setImageDrawable(getResources().getDrawable(brandDrawable));
        }
    }
}
