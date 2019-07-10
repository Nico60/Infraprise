package com.nico60.infraprise.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nico60.infraprise.Utils.AeopDbUtils;

import java.util.ArrayList;
import java.util.List;

public class AeopDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aeop_manager";
    private static final String TABLE_AEOP = "aeop_pole";
    private static final String KEY_ID = "id";
    private static final String KEY_SHEET_NAME = "sheetName";
    private static final String KEY_NRO = "nroNumber";
    private static final String KEY_PM = "pmNumber";
    private static final String KEY_AEOPN = "aeopNumber";
    private static final String KEY_BTN = "btNumber";
    private static final String KEY_HTN = "htNumber";
    private static final String KEY_DRCN = "drcNumber";
    private static final String KEY_ADRESS = "adressName";
    private static final String KEY_LATLOC = "latLoc";
    private static final String KEY_LONGLOC = "longLoc";

    public AeopDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BT_TABLE = "CREATE TABLE " + TABLE_AEOP + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SHEET_NAME + " TEXT," + KEY_NRO + " TEXT,"
                + KEY_PM + " TEXT," + KEY_AEOPN + " TEXT," + KEY_BTN + " TEXT," + KEY_HTN + " TEXT,"
                + KEY_DRCN + " TEXT," + KEY_ADRESS + " TEXT," + KEY_LATLOC + " TEXT," + KEY_LONGLOC
                + " TEXT" + ")";
        db.execSQL(CREATE_BT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AEOP);
        onCreate(db);
    }

    public void addAeopDbUtils(AeopDbUtils aeopDbUtils) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, aeopDbUtils.getSheetName());
        values.put(KEY_NRO, aeopDbUtils.getNroNumber());
        values.put(KEY_PM, aeopDbUtils.getPmNumber());
        values.put(KEY_AEOPN, aeopDbUtils.getAeopNumber());
        values.put(KEY_BTN, aeopDbUtils.getBtNumber());
        values.put(KEY_HTN, aeopDbUtils.getHtNumber());
        values.put(KEY_DRCN, aeopDbUtils.getDrcNumber());
        values.put(KEY_ADRESS, aeopDbUtils.getAdressName());
        values.put(KEY_LATLOC, aeopDbUtils.getLatLoc());
        values.put(KEY_LONGLOC, aeopDbUtils.getLongLoc());

        db.insert(TABLE_AEOP, null, values);
        db.close();
    }

    public AeopDbUtils getAeopDbUtils(String str) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_AEOP, new String[] {KEY_ID, KEY_SHEET_NAME,
                        KEY_NRO, KEY_PM, KEY_AEOPN, KEY_BTN, KEY_HTN, KEY_DRCN,
                        KEY_ADRESS, KEY_LATLOC, KEY_LONGLOC}, KEY_SHEET_NAME + " = ?",
                new String[] {str}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        AeopDbUtils aeopDbUtils = new AeopDbUtils(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getString(6),
                cursor.getString(7), cursor.getString(8),
                cursor.getString(9), cursor.getString(10));
        cursor.close();

        return aeopDbUtils;
    }

    public List<AeopDbUtils> getAllAeopDbUtils() {
        List<AeopDbUtils> aeopDbUtilsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_AEOP;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AeopDbUtils aeopDbUtils = new AeopDbUtils();
                aeopDbUtils.setID(Integer.parseInt(cursor.getString(0)));
                aeopDbUtils.setSheetName(cursor.getString(1));
                aeopDbUtils.setNroNumber(cursor.getString(2));
                aeopDbUtils.setPmNumber(cursor.getString(3));
                aeopDbUtils.setAeopNumber(cursor.getString(4));
                aeopDbUtils.setBtNumber(cursor.getString(5));
                aeopDbUtils.setHtNumber(cursor.getString(6));
                aeopDbUtils.setDrcNumber(cursor.getString(7));
                aeopDbUtils.setAdressName(cursor.getString(8));
                aeopDbUtils.setLatLoc(cursor.getString(9));
                aeopDbUtils.setLongLoc(cursor.getString(10));
                aeopDbUtilsList.add(aeopDbUtils);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return aeopDbUtilsList;
    }

    public void updateAeopDbUtils(AeopDbUtils aeopDbUtils, String str) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_NAME, aeopDbUtils.getSheetName());
        values.put(KEY_NRO, aeopDbUtils.getNroNumber());
        values.put(KEY_PM, aeopDbUtils.getPmNumber());
        values.put(KEY_AEOPN, aeopDbUtils.getAeopNumber());
        values.put(KEY_BTN, aeopDbUtils.getBtNumber());
        values.put(KEY_HTN, aeopDbUtils.getHtNumber());
        values.put(KEY_DRCN, aeopDbUtils.getDrcNumber());
        values.put(KEY_ADRESS, aeopDbUtils.getAdressName());
        values.put(KEY_LATLOC, aeopDbUtils.getLatLoc());
        values.put(KEY_LONGLOC, aeopDbUtils.getLongLoc());

        db.update(TABLE_AEOP, values, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public void deleteAeopDbUtils(String str) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AEOP, KEY_SHEET_NAME + " = ?",
                new String[] {str});
        db.close();
    }

    public int getAeopDbUtilsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_AEOP;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
}