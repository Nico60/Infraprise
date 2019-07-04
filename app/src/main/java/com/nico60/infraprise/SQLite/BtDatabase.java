package com.nico60.infraprise.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nico60.infraprise.Utils.BtDbUtils;

import java.util.ArrayList;
import java.util.List;

public class BtDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bt_manager";
    private static final String TABLE_BT = "bt_pole";
    private static final String KEY_ID = "id";
    private static final String KEY_SHEET_NAME = "sheetName";
    private static final String KEY_NRO = "nroNumber";
    private static final String KEY_PM = "pmNumber";
    private static final String KEY_BTN = "btNumber";
    private static final String KEY_ADRESS = "adressName";

    public BtDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BT_TABLE = "CREATE TABLE " + TABLE_BT + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SHEET_NAME + " TEXT," + KEY_NRO + " TEXT,"
                + KEY_PM + " TEXT," + KEY_BTN + " TEXT," + KEY_ADRESS + " TEXT" + ")";
        db.execSQL(CREATE_BT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BT);
        onCreate(db);
    }

    public void addBtDbUtils(BtDbUtils btDbUtils) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, btDbUtils.getSheetName());
        values.put(KEY_NRO, btDbUtils.getNroNumber());
        values.put(KEY_PM, btDbUtils.getPmNumber());
        values.put(KEY_BTN, btDbUtils.getBtNumber());
        values.put(KEY_ADRESS, btDbUtils.getAdressName());

        db.insert(TABLE_BT, null, values);
        db.close();
    }

    public BtDbUtils getBtDbUtils(String str) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BT, new String[] {KEY_ID, KEY_SHEET_NAME,
                        KEY_NRO, KEY_PM, KEY_BTN, KEY_ADRESS}, KEY_SHEET_NAME + " = ?",
                new String[] {str}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        BtDbUtils btDbUtils = new BtDbUtils(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5));
        cursor.close();

        return btDbUtils;
    }

    public List<BtDbUtils> getAllBtDbUtils() {
        List<BtDbUtils> btDbUtilsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_BT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BtDbUtils btDbUtils = new BtDbUtils();
                btDbUtils.setID(Integer.parseInt(cursor.getString(0)));
                btDbUtils.setSheetName(cursor.getString(1));
                btDbUtils.setNroNumber(cursor.getString(2));
                btDbUtils.setPmNumber(cursor.getString(3));
                btDbUtils.setBtNumber(cursor.getString(4));
                btDbUtils.setAdressName(cursor.getString(5));
                btDbUtilsList.add(btDbUtils);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return btDbUtilsList;
    }

    public void updateBtDbUtils(BtDbUtils btDbUtils, String str) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, btDbUtils.getSheetName());
        values.put(KEY_NRO, btDbUtils.getNroNumber());
        values.put(KEY_PM, btDbUtils.getPmNumber());
        values.put(KEY_BTN, btDbUtils.getBtNumber());
        values.put(KEY_ADRESS, btDbUtils.getAdressName());

        db.update(TABLE_BT, values, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public void deleteBtDbUtils(String str) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BT, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public int getBtDbUtilsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_BT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
}