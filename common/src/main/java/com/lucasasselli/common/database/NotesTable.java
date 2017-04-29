package com.lucasasselli.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.edam.type.Note;

import java.util.ArrayList;
import java.util.List;


public class NotesTable {
    
    private static final String ID = "ID";
    private static final String GUID = "GUID";
    public static String TITLE = "TITLE";
    public static String CONTENT = "CONTENT";
    public static String NOTEBOOK = "NOTEBOOK";
    public static String USN = "USN";
    private static final String[] COLUMNS = {ID, GUID, TITLE, CONTENT, NOTEBOOK, USN};

    private static final String TABLE_NAME = "Notes";

    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + ID + " integer primary key autoincrement, "
            + GUID + " text not null, "
            + TITLE + " text not null, "
            + CONTENT + " text not null,"
            + NOTEBOOK + " text not null,"
            + USN + " integer not null);";

    private final DatabaseHelper mDatabaseHelper;

    public NotesTable(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public void add(Note note) {
        SQLiteDatabase wDb = mDatabaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GUID, note.getGuid());
        cv.put(TITLE, note.getTitle());
        cv.put(CONTENT, note.getContent());
        cv.put(NOTEBOOK, note.getNotebookGuid());
        cv.put(USN, note.getUpdateSequenceNum());
        wDb.insert(TABLE_NAME, null, cv);
        wDb.close();
    }

    public void update(String guid, Note note) {
        SQLiteDatabase wDb = mDatabaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GUID, note.getGuid());
        cv.put(TITLE, note.getTitle());
        cv.put(CONTENT, note.getContent());
        cv.put(NOTEBOOK, note.getNotebookGuid());
        cv.put(USN, note.getUpdateSequenceNum());
        wDb.update(TABLE_NAME, cv, GUID + "=?", new String[]{String.valueOf(guid)});
        wDb.close();
    }

    public Note getNote(int id) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();

        Cursor c = rDb.query(TABLE_NAME, COLUMNS, ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        Note note = new Note();

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            try {
                note.setGuid(c.getString(1));
                note.setTitle(c.getString(2));
                note.setContent(c.getString(3));
                note.setNotebookGuid(c.getString(4));
                note.setUpdateSequenceNum(c.getInt(5));
            } finally {
                c.close();
            }
        }

        rDb.close();

        return note;
    }

    public Note getNote(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();

        Cursor c = rDb.query(TABLE_NAME, COLUMNS, GUID + "=?", new String[]{guid}, null, null, null, null);

        Note note = new Note();

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            try {
                note.setGuid(c.getString(1));
                note.setTitle(c.getString(2));
                note.setContent(c.getString(3));
                note.setNotebookGuid(c.getString(4));
                note.setUpdateSequenceNum(c.getInt(5));
            } finally {
                c.close();
            }
        }

        rDb.close();

        return note;
    }

    public void delete() {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        rDb.delete(TABLE_NAME, null, null);
        rDb.close();
    }

    public void deleteNote(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        rDb.delete(TABLE_NAME, GUID + " = ?", new String[]{guid});
        rDb.close();
    }

    public List<Note> fetchNotes() {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        Cursor c = rDb.query(TABLE_NAME, null, null, null, null, null, ID + " COLLATE NOCASE");

        List<Note> data = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setGuid(c.getString(1));
                note.setTitle(c.getString(2));
                note.setContent(c.getString(3));
                note.setNotebookGuid(c.getString(4));
                note.setUpdateSequenceNum(c.getInt(5));
                data.add(note);
            } while (c.moveToNext());
        }
        c.close();
        rDb.close();

        return data;
    }

    public int findGuid(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        String query = "Select * from " + TABLE_NAME + " where " + GUID + "= ?";
        Cursor cursor = rDb.rawQuery(query, new String[]{guid});
        int items = cursor.getCount();
        cursor.close();
        rDb.close();

        return items;
    }

    public int count() {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();

        Cursor cursor = rDb.query(TABLE_NAME, null, null, null, null, null, ID + " COLLATE NOCASE");
        int items = cursor.getCount();
        cursor.close();
        rDb.close();

        return items;
    }

    public List<Note> search(String query) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        Cursor c = rDb.query(true, TABLE_NAME, COLUMNS , TITLE + " LIKE" + "'%" + query + "%' OR " + CONTENT + " LIKE" + "'%" + query + "%'", null, null, null, null, null);

        List<Note> data = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setGuid(c.getString(1));
                note.setTitle(c.getString(2));
                note.setContent(c.getString(3));
                note.setNotebookGuid(c.getString(4));
                note.setUpdateSequenceNum(c.getInt(5));
                data.add(note);
            } while (c.moveToNext());
        }
        c.close();
        rDb.close();

        return data;
    }
}
