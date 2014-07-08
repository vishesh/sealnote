package com.twistedplane.sealnote.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.twistedplane.sealnote.utils.EasyDate;

import java.text.ParseException;

/**
 * Note contains all the data and helper functions related to a particular
 * note. Acts as a map between an entry in storage database and Java.
 */
public class Note implements Parcelable{
    public static final String TAG = "Note";

    public static enum Folder {
        FOLDER_LIVE,        /* Notes that are alive that are note deleted and archived */
        FOLDER_ARCHIVE,     /* Archived and undeleted notes */
        FOLDER_TRASH        /* Deleted notes */
    };

    public static enum FolderAction {
        NOTE_ARCHIVE,
        NOTE_UNARCHIVE,
        NOTE_DELETE,
        NOTE_RESTORE
    };

    private int mId;
    private int mPosition;
    private String mNoteTitle;
    private String mNote;
    private EasyDate mEditedDate;
    private int mColor;
    private boolean mArchived;
    private boolean mDeleted;

    public Note() {
        this.mId = -1;
        this.mColor = 0;
        this.mArchived = false;
        this.mDeleted = false;
    }

    public Note(int id, int position, String title, String content) {
        this.mId = id;
        this.mPosition = position;
        this.mNote = content;
        this.mNoteTitle = title;
        this.mColor = -1;
        this.mArchived = false;
        this.mDeleted = false;
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
        mNote = inParcel.readString();
        try {
            mEditedDate = EasyDate.fromIsoString(inParcel.readString());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date retrieved from database!");
        }
        mColor = inParcel.readInt();
        mArchived = inParcel.readInt() > 0;
        mDeleted = inParcel.readInt() > 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(mId);
        outParcel.writeInt(mPosition);
        outParcel.writeString(mNoteTitle);
        outParcel.writeString(mNote);
        outParcel.writeString(mEditedDate.toString());
        outParcel.writeInt(mColor);
        outParcel.writeInt(mArchived ?1 :0);
        outParcel.writeInt(mDeleted ?1 :0);
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

    public String getNote() {
        return this.mNote;
    }

    public void setNote(String content) {
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