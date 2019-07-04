package com.nico60.infraprise.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nico60.infraprise.Utils.FtDbUtils;

import java.util.ArrayList;
import java.util.List;

public class FtDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ft_manager";
    private static final String TABLE_FT = "ft_pole";
    private static final String KEY_ID = "id";
    private static final String KEY_SHEET_NAME = "sheetName";
    private static final String KEY_NRO = "nroNumber";
    private static final String KEY_PM = "pmNumber";
    private static final String KEY_GFTN = "gFtNumber";
    private static final String KEY_FTBEN = "ftBeNumber";
    private static final String KEY_ADRESS = "adressName";

    public FtDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BT_TABLE = "CREATE TABLE " + TABLE_FT + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SHEET_NAME + " TEXT," + KEY_NRO + " TEXT,"
                + KEY_PM + " TEXT," + KEY_GFTN + " TEXT," + KEY_FTBEN + " TEXT,"
                + KEY_ADRESS + " TEXT" + ")";
        db.execSQL(CREATE_BT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FT);
        onCreate(db);
    }

    public void addFtDbUtils(FtDbUtils ftDbUtils) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, ftDbUtils.getSheetName());
        values.put(KEY_NRO, ftDbUtils.getNroNumber());
        values.put(KEY_PM, ftDbUtils.getPmNumber());
        values.put(KEY_GFTN, ftDbUtils.getGftNumber());
        values.put(KEY_FTBEN, ftDbUtils.getFtBeNumber());
        values.put(KEY_ADRESS, ftDbUtils.getAdressName());

        db.insert(TABLE_FT, null, values);
        db.close();
    }

    public FtDbUtils getFtDbUtils(String str) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FT, new String[] {KEY_ID, KEY_SHEET_NAME,
                        KEY_NRO, KEY_PM, KEY_GFTN, KEY_FTBEN, KEY_ADRESS}, KEY_SHEET_NAME + " = ?",
                new String[] {str}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        FtDbUtils ftDbUtils = new FtDbUtils(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6));
        cursor.close();

        return ftDbUtils;
    }

    public List<FtDbUtils> getAllFtDbUtils() {
        List<FtDbUtils> ftDbUtilsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FtDbUtils ftDbUtils = new FtDbUtils();
                ftDbUtils.setID(Integer.parseInt(cursor.getString(0)));
                ftDbUtils.setSheetName(cursor.getString(1));
                ftDbUtils.setNroNumber(cursor.getString(2));
                ftDbUtils.setPmNumber(cursor.getString(3));
                ftDbUtils.setGftNumber(cursor.getString(4));
                ftDbUtils.setFtBeNumber(cursor.getString(5));
                ftDbUtils.setAdressName(cursor.getString(6));
                ftDbUtilsList.add(ftDbUtils);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return ftDbUtilsList;
    }

    public void updateFtDbUtils(FtDbUtils ftDbUtils, String str) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, ftDbUtils.getSheetName());
        values.put(KEY_NRO, ftDbUtils.getNroNumber());
        values.put(KEY_PM, ftDbUtils.getPmNumber());
        values.put(KEY_GFTN, ftDbUtils.getGftNumber());
        values.put(KEY_FTBEN, ftDbUtils.getFtBeNumber());
        values.put(KEY_ADRESS, ftDbUtils.getAdressName());

        db.update(TABLE_FT, values, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public void deleteFtDbUtils(String str) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FT, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public int getFtDbUtilsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
}