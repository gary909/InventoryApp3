package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by garywhite61 on 20/07/2017.
 * * API Contract for the Inventory app.
 */

public class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {
        throw new AssertionError("No ProductContract instances for you!");

    }

    public static final class ProductEntry implements BaseColumns {

        /** The content URI to access the data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /** MIME type of the {@link #CONTENT_URI} for a list of items */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** MIME type of the {@link #CONTENT_URI} for a single item */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        /** Name of database table */
        public static final String TABLE_NAME = "products";

        /** Unique ID */
        public static final String _ID = BaseColumns._ID;

        /** Inventory Name */
        public static final String COLUMN_INVENTORY_NAME = "name";

        /** Inventory Price */
        public static final String COLUMN_INVENTORY_PRICE = "price";

        /** Inventory Stock */
        public static final String COLUMN_INVENTORY_QUANTITY = "quantity";

        /** Inventory Sales */
        public static final String COLUMN_INVENTORY_SALES = "sale";

        /** Inventory Image */
        public static final String COLUMN_INVENTORY_PHOTO = "photo";
    }
}
