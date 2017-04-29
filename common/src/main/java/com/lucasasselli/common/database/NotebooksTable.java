package com.lucasasselli.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.edam.type.Notebook;

import java.util.ArrayList;
import java.util.List;

public class NotebooksTable {

    private static final String ID = "ID";
    private static final String GUID = "GUID";
    public static String NAME = "NAME";
    public static String USN = "USN";
    private static final String[] COLUMNS = {ID, GUID, NAME, USN};

    private static final String TABLE_NAME = "Notebooks";
    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + ID + " integer primary key autoincrement, "
            + GUID + " text not null, "
            + NAME + " text not null, "
            + USN + " integer not null);";

    private final DatabaseHelper mDatabaseHelper;

    public NotebooksTable(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public void add(Notebook notebook) {
        SQLiteDatabase wDb = mDatabaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GUID, notebook.getGuid());
        cv.put(NAME, notebook.getName());
        cv.put(USN, notebook.getUpdateSequenceNum());
        wDb.insert(TABLE_NAME, null, cv);
        wDb.close();
    }

    public void update(String guid, Notebook notebook) {
        SQLiteDatabase wDb = mDatabaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GUID, notebook.getGuid());
        cv.put(NAME, notebook.getName());
        cv.put(USN, notebook.getUpdateSequenceNum());
        wDb.update(TABLE_NAME, cv, GUID+ "=?" , new String[]{String.valueOf(guid)});
        wDb.close();
    }

    public Notebook getNotebook(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();

        Cursor c = rDb.query(TABLE_NAME, COLUMNS, GUID + "=?", new String[]{guid}, null, null, null, null);

        Notebook notebook = new Notebook();

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            try {
                notebook.setGuid(c.getString(1));;
                notebook.setName(c.getString(2));
                notebook.setUpdateSequenceNum(c.getInt(3));
            } finally {
                c.close();
            }
        }

        rDb.close();

        return notebook;
    }

    public void deleteDatabase() {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        rDb.delete(TABLE_NAME, null, null);
        rDb.close();
    }

    public void deleteNotebook(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        rDb.delete(TABLE_NAME, GUID + " =?", new String[]{guid});
        rDb.close();
    }

    public List<Notebook> fetchNotebooks() {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        Cursor c = rDb.query(TABLE_NAME, null, null, null, null, null, ID + " COLLATE NOCASE");

        List<Notebook> data = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Notebook notebook = new Notebook();
                notebook.setGuid(c.getString(1));;
                notebook.setName(c.getString(2));
                notebook.setUpdateSequenceNum(c.getInt(3));
                data.add(notebook);
            } while (c.moveToNext());
        }
        c.close();
        rDb.close();

        return data;
    }

    public int findGuid(String guid) {
        SQLiteDatabase rDb = mDatabaseHelper.getReadableDatabase();
        String Query = "Select * from " + TABLE_NAME + " where " + GUID + "= ?";
        Cursor cursor = rDb.rawQuery(Query, new String[]{guid});
        int items = cursor.getCount();
        cursor.close();
        rDb.close();

        return items;
    }
}
