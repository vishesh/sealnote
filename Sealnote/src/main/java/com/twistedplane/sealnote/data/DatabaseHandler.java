package com.twistedplane.sealnote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.twistedplane.sealnote.utils.EasyDate;
import net.sqlcipher.CursorIndexOutOfBoundsException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.*;

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

    public static final String TABLE_TAG_NAMES = "tags";
    public static final String TABLE_NOTE_TAG = "note_tag";
    public static final String COL_TAG_NAME = "name";
    public static final String COL_NOTE_ID = "noteid";
    public static final String COL_TAG_ID = "tagid";

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
     * @return Current password in use or null is nothing is set
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
     * @param cursor Cursor object from which note data is to be retrieved
     * @return Note object with data set
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
        // The main table with all notes
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

        // Table to store tag names
        query = "CREATE TABLE " + TABLE_TAG_NAMES + " ( " +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                COL_TAG_NAME + " TEXT UNIQUE " +
                ") ";
        db.execSQL(query);

        // Table to map notes with tags
        query = "CREATE TABLE " + TABLE_NOTE_TAG + " ( " +
                COL_NOTE_ID + " INTEGER, " +
                COL_TAG_ID + " INTEGER, " +
                String.format("FOREIGN KEY(%s) REFERENCES %s(%s), ", COL_NOTE_ID, TABLE_NAME, COL_ID) +
                String.format("FOREIGN KEY(%s) REFERENCES %s(%s), ", COL_TAG_ID, TABLE_TAG_NAMES, COL_ID) +
                String.format("PRIMARY KEY (%s, %s)", COL_NOTE_ID, COL_TAG_ID) +
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
         *     + Tag support
         */
        if (oldVersion <= 2) {
            Log.i(TAG, "Upgrading database from Version 2 to 3");

            /* Note Type support */
            String query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_TYPE + " TEXT NOT NULL DEFAULT '" +
                    Note.Type.TYPE_GENERIC.name() + "'";
            db.execSQL(query);

            query = "ALTER TABLE " + TABLE_NAME +
                    " ADD " + COL_NOTE_EXTRA + " TEXT ";
            db.execSQL(query);

            /* Tag support */
            query = "CREATE TABLE " + TABLE_TAG_NAMES + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    COL_TAG_NAME + " TEXT UNIQUE " +
                    ") ";
            db.execSQL(query);

            query = "CREATE TABLE " + TABLE_NOTE_TAG + " ( " +
                    COL_NOTE_ID + " INTEGER, " +
                    COL_TAG_ID + " INTEGER, " +
                    String.format("FOREIGN KEY(%s) REFERENCES %s(%s), ", COL_NOTE_ID, TABLE_NAME, COL_ID) +
                    String.format("FOREIGN KEY(%s) REFERENCES %s(%s), ", COL_TAG_ID, TABLE_TAG_NAMES, COL_ID) +
                    String.format("PRIMARY KEY (%s, %s)", COL_NOTE_ID, COL_TAG_ID) +
                    ") ";
            db.execSQL(query);
        }
    }

    /**
     * Add a note to database
     */
    public int addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        EasyDate date = EasyDate.now();

        values.put(COL_POSITION, note.getPosition());
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_NOTE, note.getNote().toString());
        values.put(COL_COLOR, note.getColor());
        values.put(COL_CREATED, date.toString());
        values.put(COL_EDITED, date.toString());
        values.put(COL_ARCHIVED, note.getIsArchived() ? 1 : 0);
        values.put(COL_DELETED, note.getIsDeleted() ? 1 : 0);
        values.put(COL_TYPE, note.getType().name());
        values.put(COL_NOTE_EXTRA, note.getNote().getCardString());

        db.beginTransaction();

        int result = (int) db.insert(TABLE_NAME, null, values);
        clearNoteTags(result);
        addTagsToNote(result, note.getTags());

        db.setTransactionSuccessful();
        db.endTransaction();

        return result;
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
        values.put(COL_ARCHIVED, note.getIsArchived() ? 1 : 0);
        values.put(COL_DELETED, note.getIsDeleted() ? 1 : 0);
        values.put(COL_NOTE_EXTRA, note.getNote().getCardString());

        db.beginTransaction();

        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{Integer.toString(note.getId())});
        clearNoteTags(note.getId());
        addTagsToNote(note.getId(), note.getTags());

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Get note object of given id from database
     *
     * @param id Id of note
     * @return Note object
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
        values.put(COL_ARCHIVED, archive ? 1 : 0);
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
        values.put(COL_DELETED, trash ? 1 : 0);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{Integer.toString(id)});
    }

    /**
     * Delete a note of given id from database.
     */
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        clearNoteTags(id);
        db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{Integer.toString(id)});

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Create a new tag and store it in database
     */
    public int newTag(String tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TAG_NAME, tag);
        return (int) db.insert(TABLE_TAG_NAMES, null, values);
    }

    /**
     * Delete tag from table
     */
    public void deleteTag(int tagid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        // Delete this tag from all notes
        db.delete(TABLE_NOTE_TAG, COL_TAG_ID + " = ?", new String[]{Integer.toString(tagid)});

        // Delete the tag itself from database
        db.delete(TABLE_TAG_NAMES, COL_ID + " = ?", new String[]{Integer.toString(tagid)});

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Rename tag
     */
    public void renameTag(int tagid, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TAG_NAME, newName);
        db.update(TABLE_TAG_NAMES, values, COL_ID + " = ?", new String[]{Integer.toString(tagid)});
    }

    /**
     * Get tags attached to this note
     */
    public Set<String> getNoteTags(int noteid) {
        Set<String> set = new HashSet<String>();
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format(
                "SELECT %s.%s AS tag FROM %s, %s WHERE %s.%s = ? AND %s.%s = %s.%s",
                TABLE_TAG_NAMES, COL_TAG_NAME,
                TABLE_TAG_NAMES, TABLE_NOTE_TAG,
                TABLE_NOTE_TAG, COL_NOTE_ID,
                TABLE_NOTE_TAG, COL_TAG_ID,
                TABLE_TAG_NAMES, COL_ID
        );
        Cursor cursor = db.rawQuery(query, new String[] {String.format("%d", noteid)});

        if(cursor.moveToFirst()) {
            do {
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                set.add(tag);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return set;
    }

    /**
     * Add tags to note.
     *
     * NOTE: It is duty of caller to make sure this routine is
     *       called inside transaction, otherwise exception
     *       will be thrown
     */
    public void addTagsToNote(int noteid, Set<String> tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!db.inTransaction()) {
            throw new SQLiteException("This method should always be called inside transaction!");
        }

        ContentValues value = new ContentValues();
        Map<String, Integer> allTags = getAllTags();

        for (String tag : tags) {
            value.clear();

            int tagid;
            if (!allTags.containsKey(tag)) {
                tagid = newTag(tag);
            } else {
                tagid = allTags.get(tag);
            }
            value.put(COL_TAG_ID, tagid);
            value.put(COL_NOTE_ID, noteid);
            db.insert(TABLE_NOTE_TAG, null, value);
        }
    }

    /**
     * Delete all tags from Note
     */
    public void clearNoteTags(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE_TAG, COL_NOTE_ID + " = ?", new String[]{Integer.toString(id)});
    }


//    /**
//     * Remove tags from note.
//     *
//     * NOTE: It is duty of caller to make sure this routine
//     *       is called inside transaction, otherwise exception
//     *       will be thrown
//     */
//    public void removeTagsFromNote(int noteid, Set<String> tags) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        if (!db.inTransaction()) {
//            throw new SQLiteException("This method should always be called inside transaction!");
//        }
//
//        ContentValues value = new ContentValues();
//        Map<String, Integer> allTags = getAllTags();
//
//        for (String tag : tags) {
//            value.clear();
//
//            int tagid = allTags.get(tag);
//            db.delete(TABLE_NOTE_TAG, COL_TAG_ID + " = ? AND " + COL_NOTE_ID + " = ?",
//                    new String[]{Integer.toString(tagid, noteid)});
//        }
//    }

    /**
     * Get a map of all <tag, id> stored in database
     */
    public Map<String, Integer> getAllTags() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TAG_NAMES, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String tag = cursor.getString(cursor.getColumnIndex(COL_TAG_NAME));
                map.put(tag, id);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return map;
    }

    /**
     * Get a map of all <tag, id> stored in database
     */
    public Map<Integer, Integer> getAllTagsCount() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        SQLiteDatabase db = getReadableDatabase();

        String query = String.format(
                "SELECT %s, COUNT(*) FROM %s GROUP BY %s",
                COL_TAG_ID, TABLE_NOTE_TAG, COL_TAG_ID
        );
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int tagid = cursor.getInt(0);
                int count = cursor.getInt(1);
                map.put(tagid, count);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return map;
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
     * Get a cursor object pointing to all live notes with given tag in database.
     */
    public Cursor getNotesCursor(int tagid) {
        if (tagid == -1) {
            throw new IllegalArgumentException("tagid = -1 not allowed. Use getNotesCursor()");
        }

        String qformat = "SELECT * FROM %s, %s " +
                          "    WHERE %s.%s = '0' AND " +
                          "          %s.%s = '0' AND " +
                          "          %s.%s = '%d' AND " +
                          "          %s.%s = %s.%s " +
                          "     ORDER BY %s.%s DESC";
        String query = String.format(
                qformat,
                TABLE_NAME, TABLE_NOTE_TAG,
                TABLE_NAME, COL_ARCHIVED,
                TABLE_NAME, COL_DELETED,
                TABLE_NOTE_TAG, COL_TAG_ID, tagid,
                TABLE_NOTE_TAG, COL_NOTE_ID,
                TABLE_NAME, COL_ID,
                TABLE_NAME, COL_EDITED
        );

        return getReadableDatabase().rawQuery(query, null);
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

    public List<Note> searchNotes(String searchString) {
        List<Note> list = new ArrayList<Note>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_NOTE + " like '%" + searchString +  "%' or " + COL_TITLE + " like %" + searchString +  "%", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}