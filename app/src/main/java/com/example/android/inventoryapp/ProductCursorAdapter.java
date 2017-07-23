package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import butterknife.ButterKnife;

/**
 * Created by garywhite61 on 20/07/2017.
 * * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of products data as its data source.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     * @param context - Activity context
     * @param cursor - Cursor containing data loaded from table
     * @param parent - Parent view
     * @return new list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * This method binds the data in the current row pointed to by cursor to the given
     * list item layout.
     * @param view - ListView
     * @param context - Activity context
     * @param cursor - Cursor containing data loaded from table
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        final TextView salesTextView = (TextView) view.findViewById(R.id.sold);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.btn_sell);

        final String idColumn = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        final String nameColumn = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_INVENTORY_NAME));
        int quantityColumn = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_INVENTORY_QUANTITY));
        int salesColumn = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_INVENTORY_SALES));
        final double priceColumn = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_INVENTORY_PRICE));
        final String photoColumn = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_INVENTORY_PHOTO));

        Log.e("DATA", nameColumn);

        nameTextView.setText(nameColumn);
        quantityTextView.setText(Integer.toString(quantityColumn));
        salesTextView.setText(Integer.toString(salesColumn));
        priceTextView.setText(Double.toString(priceColumn));

        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, Long.parseLong(idColumn));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int sales;
                if (salesTextView.getText().toString().isEmpty()) {
                    sales = 0;
                } else {
                    sales = Integer.parseInt(salesTextView.getText().toString());
                }

                int quantity;
                if (quantityTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityTextView.getText().toString());
                }

                if (quantity > 0) {
                    sales = sales + 1;
                    quantity = quantity - 1;
                    salesTextView.setText(String.valueOf(sales));
                    quantityTextView.setText(String.valueOf(quantity));

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_INVENTORY_NAME, nameColumn);
                    values.put(ProductEntry.COLUMN_INVENTORY_QUANTITY, quantity);
                    values.put(ProductEntry.COLUMN_INVENTORY_SALES, sales);
                    values.put(ProductEntry.COLUMN_INVENTORY_PRICE, priceColumn);
                    values.put(ProductEntry.COLUMN_INVENTORY_PHOTO, photoColumn);

                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Sale Product " + nameColumn, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Order Product", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
