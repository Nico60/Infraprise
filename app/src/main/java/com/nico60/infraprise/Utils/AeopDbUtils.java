package com.nico60.infraprise.Utils;

public class AeopDbUtils {

    private int id;
    private String mSheetName;
    private String mNroNumber;
    private String mPmNumber;
    private String mAeopNumber;
    private String mBtNumber;
    private String mHtNumber;
    private String mDrcNumber;
    private String mAdressName;

    public AeopDbUtils() {
        //
    }

    public AeopDbUtils(int id, String sheetName, String nroNumber, String pmNumber, String aeopNumber,
                       String btNumber, String htNumber, String drcNumber, String adressName) {
        this.id = id;
        this.mSheetName = sheetName;
        this.mNroNumber = nroNumber;
        this.mPmNumber = pmNumber;
        this.mAeopNumber = aeopNumber;
        this.mBtNumber = btNumber;
        this.mHtNumber = htNumber;
        this.mDrcNumber = drcNumber;
        this.mAdressName = adressName;
    }

    public AeopDbUtils(String sheetName, String nroNumber, String pmNumber, String aeopNumber,
                       String btNumber, String htNumber, String drcNumber, String adressName) {
        this.mSheetName = sheetName;
        this.mNroNumber = nroNumber;
        this.mPmNumber = pmNumber;
        this.mAeopNumber = aeopNumber;
        this.mBtNumber = btNumber;
        this.mHtNumber = htNumber;
        this.mDrcNumber = drcNumber;
        this.mAdressName = adressName;
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

    public String getAeopNumber() {
        return this.mAeopNumber;
    }

    public void setAeopNumber(String aeopNumber) {
        this.mAeopNumber = aeopNumber;
    }

    public String getBtNumber() {
        return this.mBtNumber;
    }

    public void setBtNumber(String btNumber) {
        this.mBtNumber = btNumber;
    }

    public String getHtNumber() {
        return this.mHtNumber;
    }

    public void setHtNumber(String htNumber) {
        this.mHtNumber = htNumber;
    }

    public String getDrcNumber() {
        return this.mDrcNumber;
    }

    public void setDrcNumber(String drcNumber) {
        this.mDrcNumber = drcNumber;
    }

    public String getAdressName() {
        return this.mAdressName;
    }

    public void setAdressName(String adressName) {
        this.mAdressName = adressName;
    }
}