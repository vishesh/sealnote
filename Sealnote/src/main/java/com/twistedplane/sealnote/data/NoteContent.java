package com.twistedplane.sealnote.data;

/**
 * Abstract class for different types of Note Contents
 */
public abstract class NoteContent {
    protected String    mContent;               /* Raw unparsed content */
    protected String    mCardString;            /* String to show in cards */
    protected boolean   mUpdated = false;       /* Is mContent parsed yet? */

    protected NoteContent() {
        mUpdated = true;
    }

    protected NoteContent(String content) {
        mContent = content;
        mUpdated = false;
    }

    /**
     * Returns string to be shown in Cards from latest valuess
     */
    abstract public String  getCardString();

    /**
     * Parse mContent and update all fields
     */
    abstract public void    update();

    /**
     * Returns raw string for storage from latest values
     */
    abstract public String  toString();

    /**
     * Assign card string to given string. This value is used as
     * cache to improve performance when showing all notes.
     *
     * This method should be called only by the DatabaseHandler
     * when saving or updating note
     */
    public void setCardString(String cardString) {
        mCardString = cardString;
    }

    /**
     * Return the cached value of card string which will be used to
     * show in cards when listing all notes
     * @return
     */
    public String getCardStringCached() {
        return mCardString;
    }

    /**
     * Create a NoteContent object of appropriate type from given content
     * @param type      Type of NoteContent
     * @param content   Raw string of NoteContent
     * @return          Object of a derived class of NoteContent of Note.Type=type
     */
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
