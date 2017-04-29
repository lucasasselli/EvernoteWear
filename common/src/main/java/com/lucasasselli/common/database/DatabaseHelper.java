package com.lucasasselli.common.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "EVERNOTEWEAR_DB";
    public final static int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NotebooksTable.TABLE_CREATE);
        db.execSQL(NotesTable.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NotebooksTable.TABLE_CREATE);
        db.execSQL(NotesTable.TABLE_CREATE);
    }
}