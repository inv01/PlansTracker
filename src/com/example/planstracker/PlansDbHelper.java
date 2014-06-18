package com.example.planstracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;

public class PlansDbHelper extends SQLiteOpenHelper {
    
    /* Inner class that defines the table contents */
    public static abstract class DbEn implements BaseColumns {
        public static final String TABLE_TPLAN = "TPlan";
        public static final String CN_DATE = "event_datetime";
        public static final String CN_PERSON = "person_name";
        public static final String CN_PHONE = "phone_number";
        public static final String CN_EMAIL = "email_address";
        public static final String CN_MONEY = "money";
        public static final String CN_HOURS = "hours";
        public static final String CN_NOTE = "note";
        public static final String CN_PICTURE = "pic";
        public static final String CN_LOC_LNG = "loc_longitude";
        public static final String CN_LOC_LAT = "loc_latitude";
        public static final String CN_LOC_NAME = "loc_str";
    }
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PlansTr.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private Context mContext;
    
    private static final String SQL_CREATE_TPLAN =
        "CREATE TABLE " + DbEn.TABLE_TPLAN + " (" +
        DbEn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        DbEn.CN_DATE + " INTEGER" + COMMA_SEP +
        DbEn.CN_PERSON + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_PHONE + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_EMAIL + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_HOURS + REAL_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_MONEY + REAL_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_NOTE + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_PICTURE + BLOB_TYPE + COMMA_SEP +
        DbEn.CN_LOC_LNG + REAL_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_LOC_LAT + REAL_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_LOC_NAME + TEXT_TYPE +
        " )";
    private static final String SQL_DELETE_TABLES =
        "DROP TABLE IF EXISTS " + DbEn.TABLE_TPLAN;

    public PlansDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TPLAN);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void onRemoveRecord(SQLiteDatabase db, int selectedID){
        db.execSQL("DELETE FROM " + DbEn.TABLE_TPLAN + " WHERE " + DbEn._ID + "=" + selectedID);
    }
    
    public SparseArray<String> getAllTasks(SQLiteDatabase db){
        SharedPreferences app_preferences = 
                PreferenceManager.getDefaultSharedPreferences(mContext);
        long sel_date = app_preferences.getLong("date", 0);
        long plus1_day = sel_date + (1000 * 60 * 60 * 24);
        // || sel_date == (new Date()).getTime()
        String where = (sel_date == 0) ? ""
                : (" WHERE " + DbEn.CN_DATE + " <= " + plus1_day);

        SparseArray<String> sa = new SparseArray<String>();
        sa.put(0, mContext.getResources().getString(R.string.add_newtask));
        String select = "SELECT " + DbEn._ID + COMMA_SEP +
                DbEn.CN_DATE + COMMA_SEP + "ifnull(" + DbEn.CN_PERSON + ",'')" + DbEn.CN_PERSON +
                " FROM " + DbEn.TABLE_TPLAN + 
                where +
                " order by " + DbEn.CN_DATE + " desc";
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            SimpleDateFormat sf = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm", Locale.getDefault());
            while (cursor.moveToNext()){
                String ui_date = sf.format(cursor.getLong(cursor.getColumnIndex(DbEn.CN_DATE)));
                String s = ui_date + " " + cursor.getString(cursor.getColumnIndex(DbEn.CN_PERSON));
                sa.put(cursor.getInt(cursor.getColumnIndex(DbEn._ID)), s);
             }
        }
        cursor.close();
        return sa;
    }
    
    public PlanEvent getPlanEventById(SQLiteDatabase db, int id){
        String select = "SELECT * FROM " + DbEn.TABLE_TPLAN + " WHERE " + DbEn._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()){
            int _id = c.getInt(c.getColumnIndex(DbEn._ID));
            Date event_datetime = new Date(c.getLong(c.getColumnIndex(DbEn.CN_DATE)));
            double loc_lng = c.getDouble(c.getColumnIndex(DbEn.CN_LOC_LNG));
            double loc_lat = c.getDouble(c.getColumnIndex(DbEn.CN_LOC_LAT));
            double money = c.getDouble(c.getColumnIndex(DbEn.CN_MONEY));
            double hours = c.getDouble(c.getColumnIndex(DbEn.CN_HOURS));
            String phone = c.getString(c.getColumnIndex(DbEn.CN_PHONE));
            String email = c.getString(c.getColumnIndex(DbEn.CN_EMAIL));
            String note = c.getString(c.getColumnIndex(DbEn.CN_NOTE));
            String name = c.getString(c.getColumnIndex(DbEn.CN_PERSON));
            String loc_str = c.getString(c.getColumnIndex(DbEn.CN_LOC_NAME));
            byte[] pic = c.getBlob(c.getColumnIndex(DbEn.CN_PICTURE));
            EventLocation event_location = new EventLocation(loc_lng, loc_lat, loc_str);
            c.close();
            return new PlanEvent(_id, event_datetime, name, phone, email, money,
                    pic, hours, note, event_location);
        }
        else {
            c.close();
            return new PlanEvent();
        }
    }
    
    public PlanEvent savePlanEvent(SQLiteDatabase db, PlanEvent pe){
        ContentValues values = new ContentValues();
        values.put(DbEn.CN_DATE, pe.getDate().getTime());
        values.put(DbEn.CN_EMAIL, pe.getEmail_address());
        values.put(DbEn.CN_PHONE, pe.getPhone_number());
        values.put(DbEn.CN_HOURS, pe.getHours());
        values.put(DbEn.CN_LOC_LAT, pe.getEvent_location().getLoc_latitude());
        values.put(DbEn.CN_LOC_LNG, pe.getEvent_location().getLoc_longitude());
        values.put(DbEn.CN_LOC_NAME, pe.getEvent_location().getLoc_str());
        values.put(DbEn.CN_MONEY, pe.getMoney());
        values.put(DbEn.CN_NOTE, pe.getNote());
        values.put(DbEn.CN_PERSON, pe.getPerson_name());
        values.put(DbEn.CN_PICTURE, pe.getPic());
        
        if(pe.getId() == 0){
            long newRowId = db.insert(DbEn.TABLE_TPLAN, null, values);
            if (newRowId == -1){Log.e("Empty new row not inserted ", "");
            } else pe.setId((int)newRowId);
            return pe;
        }
        String selection = DbEn._ID + " = ?";
        String[] selectionArgs = { String.valueOf(pe.getId())};
        db.update(DbEn.TABLE_TPLAN, values, selection, selectionArgs);
        return pe;
    }

}
