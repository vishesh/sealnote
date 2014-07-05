package com.twistedplane.sealnote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.twistedplane.sealnote.utils.EasyDate;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage database creation and upgrade, and manage notes.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String DBNAME = "sealnote.sqlite";
    public static final int VERSION = 1;

    // table and column names names
    public static final String TABLE_NAME = "notes";
    public static final String COL_ID = "_id";
    public static final String COL_POSITION = "position";
    public static final String COL_TITLE = "title";
    public static final String COL_NOTE = "content";
    public static final String COL_COLOR = "color";
    public static final String COL_CREATED = "created";
    public static final String COL_EDITED = "edited";

    /**
     * Keep only one instance of database throughout application for performace
     */
    private SQLiteDatabase mDatabase = null;

    /**
     * Store current password used. Password expires after timeouts.
     */
    private String mPassword = null;

    public DatabaseHandler(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    /**
     * Set the password used to unlock database.
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    /**
     * Get current password in use. This function is used extensively in
     * application. Note that password can be null due to timeouts which
     * resets the password for security.
     *
     * @return  Current password in use or null is nothing is set
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Update the cached static database instance using current password.
     */
    public void update() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            if (getPassword() == null || getPassword().equals("")) {
                Log.w("DEBUG", "Password expired yet we are trying to access database");
                throw new IllegalArgumentException("Password null or not acceptable");
            }
            mDatabase = this.getWritableDatabase(getPassword());
        }
    }

    /**
     * Reset cached database and password.
     */
    public void recycle() {
        close();
        mDatabase = null;
        mPassword = null;
    }

    @Override
    public synchronized void close() {
        super.close();
        if (mDatabase != null) {
            if (!mDatabase.isOpen())
            mDatabase.close();
            mDatabase = null;
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        update();
        return mDatabase;
    }

    public SQLiteDatabase getReadableDatabase() {
        return getWritableDatabase();
    }

    /**
     * Convert row at given cursor position to Note object.
     *
     * @param cursor    Cursor object from which note data is to be retrieved
     * @return          Note object with data set
     */
    public static Note cursorToNote(Cursor cursor) {
        Note note = new Note();
        try {
            note.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COL_ID))));
            note.setPosition(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COL_POSITION))));
            note.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            note.setNote(cursor.getString(cursor.getColumnIndex(COL_NOTE)));
            note.setEditedDate(EasyDate.fromIsoString(cursor.getString(cursor.getColumnIndex(COL_EDITED))));
            note.setColor(cursor.getInt(cursor.getColumnIndex(COL_COLOR)));
        } catch (Exception e) {
            return null;
        }
        return note;
    }

    /**
     * Create a new database. Runs on first install of application.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ( " +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                COL_POSITION + " INTEGER, " +
                COL_TITLE + " TEXT, " +
                COL_NOTE + " TEXT, " +
                COL_COLOR + " INTEGER, " +
                COL_CREATED + " TEXT, " +
                COL_EDITED + " TEXT " + " ) ";
        db.execSQL(query);
    }

    /**
     * Upgrade database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing for now
    }

    /**
     * Add a note to database
     */
    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        EasyDate date = EasyDate.now();

        values.put(COL_POSITION, note.getPosition());
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_NOTE, note.getNote());
        values.put(COL_COLOR, note.getColor());
        values.put(COL_CREATED, date.toString());
        values.put(COL_EDITED, date.toString());
        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * Update given note. Note id is used to identify the database tuple.
     */
    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID, note.getId());
        values.put(COL_POSITION, note.getPosition());
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_NOTE, note.getNote());
        values.put(COL_COLOR, note.getColor());
        values.put(COL_EDITED, EasyDate.now().toString());
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{Integer.toString(note.getId())});
    }

    /**
     * Get note object of given id from database
     *
     * @param id    Id of note
     * @return      Note object
     */
    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COL_ID + " = ?", new String[]{Integer.toString(id)},
                null, null, null, null);
        Note note = null;

        if (cursor.moveToFirst()) {
            note = cursorToNote(cursor);
            cursor.close();
        }

        return note;
    }

    /**
     * Delete a note of given id from database.
     */
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{Integer.toString(id)});
    }

    /**
     * Get a list of all notes stored in database
     */
    public List<Note> getAllNotes() {
        List<Note> list = new ArrayList<Note>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    /**
     * Get a cursor object pointing to all notes in database.
     */
    public Cursor getAllNotesCursor() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_CREATED + " DESC", null
            );
    }

}