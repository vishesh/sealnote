package com.twistedplane.sealnote.data;

public abstract class NoteContent {
    protected String mContent;
    protected String mCardString;
    protected boolean mUpdated = false;

    protected NoteContent() {
        mUpdated = true;
    }

    protected NoteContent(String content) {
        mContent = content;
        mUpdated = false;
    }

    abstract public String  getCardString();
    abstract public void    update();
    abstract public String  toString();

    public void setCardString(String cardString) {
        mCardString = cardString;
    }

    public String getCardStringCached() {
        return mCardString;
    }

    public static NoteContent fromString(Note.Type type, String content) {
        switch (type) {
            case TYPE_GENERIC:
                return new NoteContentGeneric(content);
            case TYPE_CARD:
                return new NoteContentCard(content);
            case TYPE_LOGIN:
                return new NoteContentLogin(content);
            default:
                throw new IllegalArgumentException("Invalid Note.Type '" + type + "' provided");
        }
    }
}
