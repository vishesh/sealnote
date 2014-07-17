package com.twistedplane.sealnote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.twistedplane.sealnote.utils.EasyDate;
import net.sqlcipher.CursorIndexOutOfBoundsException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage database creation and upgrade, and manage notes.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHandler";

    public static final String DBNAME = "sealnote.sqlite";
    public static final int VERSION = 4;

    // table and column names names
    public static final String TABLE_NAME = "notes";
    public static final String COL_ID = "_id";
    public static final String COL_POSITION = "position";
    public static final String COL_TITLE = "title";
    public static final String COL_NOTE = "content";
    public static final String COL_NOTE_EXTRA = "content_extra";
    public static final String COL_COLOR = "color";
    public static final String COL_CREATED = "created";
    public static final String COL_EDITED = "edited";
    public static final String COL_ARCHIVED = "archived";
    public static final String COL_DELETED = "deleted";
    public static final String COL_TYPE = "type";

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
                Log.d(TAG, "Password expired yet we are trying to access database");
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
            note.setEditedDate(EasyDate.fromIsoString(cursor.getString(cursor.getColumnIndex(COL_EDITED))));
            note.setColor(cursor.getInt(cursor.getColumnIndex(COL_COLOR)));
            note.setIsArchived(cursor.getInt(cursor.getColumnIndex(COL_ARCHIVED)) > 0);
            note.setIsDeleted(cursor.getInt(cursor.getColumnIndex(COL_DELETED)) > 0);
            note.setType(Note.Type.valueOf(cursor.getString(cursor.getColumnIndex(COL_TYPE))));
            note.setNote(NoteContent.fromString(note.getType(),
                    cursor.getString(cursor.getColumnIndex(COL_NOTE))));
            note.getNote().setCardString(cursor.getString(cursor.getColumnIndex(COL_NOTE_EXTRA)));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date retrieved from database!");
            return null;
        } catch (CursorIndexOutOfBoundsException e) {
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
                COL_NOTE_EXTRA + " TEXT, " +
                COL_COLOR + " INTEGER, " +
                COL_CREATED + " TEXT, " +
                COL_EDITED + " TEXT, " +
                COL_ARCHIVED + " INTEGER NOT NULL DEFAULT '0'," +
                COL_DELETED + " INTEGER NOT NULL DEFAULT '0'," +
                COL_TYPE + " TEXT NOT NULL DEFAULT '" + Note.Type.TYPE_GENERIC.name() + "'" +
                ") ";
        db.execSQL(query);
    }

    /**
     * Upgrade database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * Version 1 to 2
         *     + Add archived and deleted column for notes
         */
        if (oldVersion <= 1) {
            Log.i(TAG, "Upgrading database from Version 1 to 2");
            String query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_ARCHIVED + " INTEGER NOT NULL DEFAULT '0'";
            db.execSQL(query);

            query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_DELETED + " INTEGER NOT NULL DEFAULT '0'";
            db.execSQL(query);
        }

        /**
         * Version 2 to 3
         *     + Add COL_TYPE which maps to Note.Type
         *     + Add COL_NOTE_EXTRA which keeps content to shown on CardView
         *       for non-generic notes
         */
        if (oldVersion <= 2) {
            Log.i(TAG, "Upgrading database from Version 2 to 3");
            String query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_TYPE + " TEXT NOT NULL DEFAULT '" +
                    Note.Type.TYPE_GENERIC.name() + "'";
            db.execSQL(query);

            query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_NOTE_EXTRA + " TEXT ";
            db.execSQL(query);
        }
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
        values.put(COL_NOTE, note.getNote().toString());
        values.put(COL_COLOR, note.getColor());
        values.put(COL_CREATED, date.toString());
        values.put(COL_EDITED, date.toString());
        values.put(COL_ARCHIVED, note.getIsArchived() ?1 :0);
        values.put(COL_DELETED, note.getIsDeleted() ?1 :0);
        values.put(COL_TYPE, note.getType().name());
        values.put(COL_NOTE_EXTRA, note.getNote().getCardString());
        return db.insert(TABLE_NAME, null, values);
    }

    /**
     * Update given note. Note id is used to identify the database tuple.
     */
    public void updateNote(Note note, boolean updateTimestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID, note.getId());
        values.put(COL_POSITION, note.getPosition());
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_NOTE, note.getNote().toString());
        values.put(COL_COLOR, note.getColor());
        if (updateTimestamp) {
            values.put(COL_EDITED, EasyDate.now().toString());
        }
        values.put(COL_ARCHIVED, note.getIsArchived() ?1 :0);
        values.put(COL_DELETED, note.getIsDeleted() ?1 :0);
        values.put(COL_NOTE_EXTRA, note.getNote().getCardString());
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
     * Archive given note. Note id is used to identify the database tuple.
     */
    public void archiveNote(int id, boolean archive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ARCHIVED, archive ?1 :0);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{Integer.toString(id)});
    }

    /**
     * Trash given note. Note id is used to identify the database tuple.
     * Note that Trash moves to Trash folder while deleteNote() below
     * removes from database
     */
    public void trashNote(int id, boolean trash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DELETED, trash ?1 :0);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{Integer.toString(id)});
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
     * Get a cursor object pointing to all live notes in database.
     */
    public Cursor getNotesCursor() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_ARCHIVED + " = '0' AND " +
                        COL_DELETED + " = '0'" +
                " ORDER BY " + COL_EDITED + " DESC",
                null
            );
    }

    /**
     * Get a cursor object pointing to all archived notes in database
     */
    public Cursor getArchivedNotesCursor() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COL_ARCHIVED + " = '1' AND " +
                        COL_DELETED + " = '0'" +
                        " ORDER BY " + COL_EDITED + " DESC",
                null
        );
    }

    /**
     * Get a cursor object pointing to all archived notes in database
     */
    public Cursor getDeletedNotesCursor() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COL_DELETED + " = '1'" +
                        " ORDER BY " + COL_EDITED + " DESC",
                null
        );
    }
}