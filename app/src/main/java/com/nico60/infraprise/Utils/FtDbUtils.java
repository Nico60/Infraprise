package com.nico60.infraprise.Utils;

public class FtDbUtils {

    private int id;
    private String mSheetName;
    private String mNroNumber;
    private String mPmNumber;
    private String mGftNumber;
    private String mFtBetNumber;
    private String mAdressName;
    private String mLatLoc;
    private String mLongLoc;

    public FtDbUtils() {
        //
    }

    public FtDbUtils(int id, String sheetName, String nroNumber, String pmNumber, String gFtNumber,
                     String ftBeNumber, String adressName, String latLoc, String longLoc) {
        this.id = id;
        this.mSheetName = sheetName;
        this.mNroNumber = nroNumber;
        this.mPmNumber = pmNumber;
        this.mGftNumber = gFtNumber;
        this.mFtBetNumber = ftBeNumber;
        this.mAdressName = adressName;
        this.mLatLoc = latLoc;
        this.mLongLoc = longLoc;
    }

    public FtDbUtils(String sheetName, String nroNumber, String pmNumber, String gFtNumber,
                     String ftBeNumber, String adressName, String latLoc, String longLoc) {
        this.mSheetName = sheetName;
        this.mNroNumber = nroNumber;
        this.mPmNumber = pmNumber;
        this.mGftNumber = gFtNumber;
        this.mFtBetNumber = ftBeNumber;
        this.mAdressName = adressName;
        this.mLatLoc = latLoc;
        this.mLongLoc = longLoc;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getSheetName() {
        return this.mSheetName;
    }

    public void setSheetName(String sheetName) {
        this.mSheetName = sheetName;
    }

    public String getNroNumber() {
        return this.mNroNumber;
    }

    public void setNroNumber(String nroNumber) {
        this.mNroNumber = nroNumber;
    }

    public String getPmNumber() {
        return this.mPmNumber;
    }

    public void setPmNumber(String pmNumber) {
        this.mPmNumber = pmNumber;
    }

    public String getGftNumber() {
        return this.mGftNumber;
    }

    public void setGftNumber(String gFtNumber) {
        this.mGftNumber = gFtNumber;
    }

    public String getFtBeNumber() {
        return this.mFtBetNumber;
    }

    public void setFtBeNumber(String ftBeNumber) {
        this.mFtBetNumber = ftBeNumber;
    }

    public String getAdressName() {
        return this.mAdressName;
    }

    public void setAdressName(String adressName) {
        this.mAdressName = adressName;
    }

    public String getLatLoc() {
        return this.mLatLoc;
    }

    public void setLatLoc(String latLoc) {
        this.mLatLoc = latLoc;
    }

    public String getLongLoc() {
        return this.mLongLoc;
    }

    public void setLongLoc(String longLoc) {
        this.mLongLoc = longLoc;
    }
}