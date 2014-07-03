package com.twistedplane.sealnote.data;

import com.twistedplane.sealnote.utils.EasyDate;

/**
 * Note contains all the data and helper functions related to a particular
 * note. Acts as a map between an entry in storage database and Java.
 */
public class Note {
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
}
