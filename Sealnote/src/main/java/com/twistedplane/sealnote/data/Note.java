package com.twistedplane.sealnote.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.twistedplane.sealnote.SealnoteApplication;
import com.twistedplane.sealnote.utils.EasyDate;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Note contains all the data and helper functions related to a particular
 * note. Acts as a map between an entry in storage database and Java.
 */
public class Note implements Parcelable{
    public static final String TAG = "Note";

    public static enum Folder {
        FOLDER_NONE,        /* No folder selected */
        FOLDER_LIVE,        /* Notes that are alive that are note deleted and archived */
        FOLDER_ARCHIVE,     /* Archived and undeleted notes */
        FOLDER_TRASH,       /* Deleted notes */
        FOLDER_TAG,         /* A tag is currently selected */
    }

    public static enum FolderAction {
        NOTE_ARCHIVE,       /* Move note to Archive folder */
        NOTE_UNARCHIVE,     /* Move note from Archive to its previous folder */
        NOTE_DELETE,        /* Move note to Trash, or delete permanently if already in Trash */
        NOTE_RESTORE        /* Move note from Trash to its original folder */
    }

    public static enum Type {
        TYPE_GENERIC,
        TYPE_LOGIN,
        TYPE_CARD,
    }

    private int         mId;            /* Unique note id */
    private int         mPosition;      /* Position of note */
    private String      mNoteTitle;     /* Note title */
    private NoteContent mNote;          /* Note content */
    private EasyDate    mEditedDate;    /* Last write date */
    private int         mColor;         /* Note color code, 0-7 */
    private boolean     mArchived;      /* Is note archived */
    private boolean     mDeleted;       /* Is note in Trash folder */
    private Type        mType;          /* Type of note eg. Credit Card, Password, Text */
    private Set<String> mTags;          /* Tags attached to this note */

    public Note() {
        this.mId = -1;
        this.mColor = 0;
        this.mArchived = false;
        this.mDeleted = false;
        this.mType = Type.TYPE_GENERIC;
    }

    public Note(int id, int position, String title, String content, Type type) {
        this.mId = id;
        this.mPosition = position;
        this.mNote = NoteContent.fromString(type, content);
        this.mNoteTitle = title;

        this.mColor = -1;
        this.mArchived = false;
        this.mDeleted = false;
        this.mType = Type.TYPE_GENERIC;
    }

    /**
     * Constructor to recreate object for Parcel
     */
    public Note(Parcel inParcel) {
        readFromParcel(inParcel);
    }

    /**
     * Helper method called by constructor to read from parcel
     */
    private void readFromParcel(Parcel inParcel) {
        mId = inParcel.readInt();
        mPosition = inParcel.readInt();
        mNoteTitle = inParcel.readString();
        String note = inParcel.readString();
        try {
            mEditedDate = EasyDate.fromIsoString(inParcel.readString());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date retrieved from database!");
        }
        mColor = inParcel.readInt();
        mArchived = inParcel.readInt() > 0;
        mDeleted = inParcel.readInt() > 0;
        mType = Type.valueOf(inParcel.readString());
        mNote = NoteContent.fromString(mType, inParcel.readString());
        mTags = convertToTagSet(inParcel.readString());
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(mId);
        outParcel.writeInt(mPosition);
        outParcel.writeString(mNoteTitle);
        outParcel.writeString(mNote.toString());
        outParcel.writeString(mEditedDate.toString());
        outParcel.writeInt(mColor);
        outParcel.writeInt(mArchived ?1 :0);
        outParcel.writeInt(mDeleted ?1 :0);
        outParcel.writeString(mType.name());
        outParcel.writeString(convertTagSetToString(mTags));
    }

    /**
     * Converts given space separated tag string to a Set collection
     */
    public static Set<String> convertToTagSet(String tagString) {
        String tags[] = tagString.split(" ");
        HashSet<String> tagSet = new HashSet<String>();
        for (String tag : tags) {
            String trimmed = tag.trim();
            if (trimmed.equals("")) {
                continue;
            }
            tagSet.add(trimmed);
        }
        return tagSet;
    }

    /**
     * Convert given tag set to space separated tag string
     */
    public static String convertTagSetToString(Set<String> tagSet) {
        StringBuilder builder = new StringBuilder();

        for (String tag : tagSet) {
            builder.append(tag);
            builder.append(" ");
        }

        return builder.toString();
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getPosition() {
        return this.mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public String getTitle() {
        return this.mNoteTitle;
    }

    public void setTitle(String title) {
        this.mNoteTitle = title;
    }

    public NoteContent getNote() {
        return this.mNote;
    }

    public void setNote(NoteContent content) {
        this.mNote = content;
    }

    public EasyDate getEditedDate() {
        return this.mEditedDate;
    }

    public void setEditedDate(EasyDate date) {
        this.mEditedDate = date;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setIsArchived(boolean archived) {
        this.mArchived = archived;
    }

    public boolean getIsArchived() {
        return this.mArchived;
    }

    public void setIsDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public boolean getIsDeleted() {
        return this.mDeleted;
    }

    public boolean getIsLive() {
        return !(this.mDeleted || mArchived);
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    /**
     * NOTE: This may return null, if loadGetTags() has not been
     * called earlier, as tags are not loaded from database with note.
     * They have to be explicitly loaded
     */
    public Set<String> getTags() {
        return mTags;
    }

    public void setTags(Set<String> tagSet) {
        mTags = tagSet;
    }

    /**
     * Note by default doesn't load tags. Call this method to load
     * tags from database and return them. Use getTags() later
     */
    public Set<String> loadGetTags() {
        final DatabaseHandler handler = SealnoteApplication.getDatabase();
        mTags = handler.getNoteTags(mId);
        return mTags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}