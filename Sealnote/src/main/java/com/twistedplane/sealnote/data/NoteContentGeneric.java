package com.twistedplane.sealnote.data;

/**
 * Note Content for plain text notes
 */
public class NoteContentGeneric extends NoteContent {
    public final static String TAG = "NoteContentGeneric";

    private String mContent;

    protected NoteContentGeneric(String content) {
        mContent = content;
    }

    @Override
    public String getCardString() {
        return mContent;
    }

    @Override
    public String getCardStringCached() {
        // Cache not required here as we use whole note to show
        return mContent;
    }

    @Override
    public void setCardString(String string) {
        // no implementation required
    }

    @Override
    public void update() {
        // no implementation required
    }

    @Override
    public String toString() {
        return mContent;
    }
}


