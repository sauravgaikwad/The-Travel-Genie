package com.example.jigya.travelgenie7;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jigya on 31/3/18.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="my_db";
    private static final String TABLE_NAME="user";
    private static final String COLUMN_USERNO="userno";
    private static final String COLUMN_PREFONE="prefone";
    private static  final String COLUMN_PREFTWO="preftwo";
    MyDBHelper(Context c)
    {
        super(c,DB_NAME,null,1);

    }
    @Override

    public void onCreate(SQLiteDatabase db) {
        String query="CREATE TABLE "+TABLE_NAME+" ( "+COLUMN_USERNO+" VARCHAR(12) PRIMARY KEY, "+
                COLUMN_PREFONE+" VARCHAR(12), "+COLUMN_PREFTWO+" VARCHAR(12) );";
        db.execSQL(query);



    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public  boolean add(String uno,String pone,String ptwo)
    {
        ContentValues values=new ContentValues();
        values.put(COLUMN_USERNO,uno);
        values.put(COLUMN_PREFONE,pone);
        values.put(COLUMN_PREFTWO,ptwo);
        SQLiteDatabase db=getWritableDatabase();
        try {
            db.insert(TABLE_NAME,null,values);
            db.close();
        }
        catch (Exception e)
        {
            db.close();
            return false;
        }
        return true;
    }
    public Cursor retrieveAll()
    {
        SQLiteDatabase db=getWritableDatabase();
        Cursor cr=db.rawQuery("select * from "+TABLE_NAME,null);
        return  cr;
    }
    public boolean update(String uno,String pone,String ptwo)
    {
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_USERNO,uno);
        values.put(COLUMN_PREFONE,pone);
        values.put(COLUMN_PREFTWO,ptwo);
        db.update(TABLE_NAME,values,"userno =?",new String[] { uno });
        return true;
    }
}
