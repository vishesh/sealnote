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
    private int mId;
    private int mPosition;
    private String mNoteTitle;
    private String mNote;
    private EasyDate mEditedDate;
    private int mColor;

    public Note() {
        this.mId = -1;
        this.mColor = 0;
    }

    public Note(int id, int position, String title, String content) {
        this.mId = id;
        this.mPosition = position;
        this.mNote = content;
        this.mNoteTitle = title;
        this.mColor = -1;
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
            Log.e("DEBUG", "Error parsing date retrieved from database!");
        }
        mColor = inParcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeInt(mId);
        outParcel.writeInt(mPosition);
        outParcel.writeString(mNoteTitle);
        outParcel.writeString(mNote);
        outParcel.writeString(mEditedDate.toString());
        outParcel.writeInt(mColor);
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