package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by garywhite61 on 20/07/2017.
 * Database helper for Products table. Manages database creation and version management.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    public static final String DATABASE_NAME = "warehouse.db";

    /** Database version */
    public static final int DATABASE_VERSION = 1;

    /**
     * Default Constructor
     * @param context
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Function - CREATE table
     * This method is called when the database is created for the first time
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    // Create a String that contains the SQL statement to create the table
    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                    ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProductContract.ProductEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, " +
                    ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE + " FLOAT NOT NULL DEFAULT 0, " +
                    ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL, " +
                    ProductContract.ProductEntry.COLUMN_INVENTORY_SALES + " INTEGER NOT NULL DEFAULT 0, " +
                    ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
