package com.chenhaowang.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chenhaowang.inventoryapp.data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wang.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_SOLD + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB);";

        db.execSQL(CREATE_PRODUCT_TABLE);
    }

    /*now there is no need to upgrade database*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
