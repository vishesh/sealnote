package com.twistedplane.sealnote.data;

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


