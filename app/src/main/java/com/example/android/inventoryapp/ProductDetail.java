package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by garywhite61 on 20/07/2017.
 */

public class ProductDetail extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    @BindView(R.id.detail_iv) ImageView mImageView;
    @BindView(R.id.detail_name) TextView mNameTextView;
    @BindView(R.id.detail_quantity) TextView mQuantityTextView;
    @BindView(R.id.detail_price) TextView mPriceTextView;
    @BindView(R.id.btn_order) Button buttonOrder;
    @BindView(R.id.plus_btn) Button buttonPlus;
    Uri mCurrentProductUri;
    Uri mUri;
    Context context;
    private boolean mProductHasChanged = false;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ButterKnife.bind(this);

        mCurrentProductUri = getIntent().getData();
        Log.v("TESTER","TESTURI" + mCurrentProductUri);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductDetail.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ProductDetail.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void orderProduct(View view){
        String nameProduct = mNameTextView.getText().toString().trim();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.putExtra(Intent.EXTRA_EMAIL,  new String[]{"Supplier <supplier@example.com>"});
        intent.setData(Uri.parse("mailto:"));// only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order " + nameProduct);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    public void addQuantity(View view){
        int quantity;
        String quantityString = mQuantityTextView.getText().toString();
        if(quantityString.isEmpty()){
            quantity = 0;
        }
        else {
            quantity = Integer.parseInt(quantityString);
        }
        quantity = quantity + 1;
        mQuantityTextView.setText(String.valueOf(quantity));
    }

    public void subtractQuantity(View view) {
        int quantity;
        String quantityString = mQuantityTextView.getText().toString();
        if (quantityString.isEmpty()) {
            quantity = 0;
        }
        else {
            quantity = Integer.parseInt(quantityString);
        }
        if (quantity > 0) {
            quantity = quantity - 1;
        }
        mQuantityTextView.setText(String.valueOf(quantity));
    }

    private void saveProduct() {
        String nameString = mNameTextView.getText().toString().trim();
        String priceString = mPriceTextView.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String photoString;

        if (mUri != null) {
            photoString = mUri.toString();
        } else {
            photoString = "";
        }

        if(TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString)){
            Toast.makeText(this, "You must enter data", Toast.LENGTH_SHORT).show();
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO, photoString);

        if (mCurrentProductUri == null) {
            Uri uri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.error_saving_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_success), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_INVENTORY_NAME,
                ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE,
                ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY,
                ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO};

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY);
            int photoColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String photo = cursor.getString(photoColumnIndex);

            mNameTextView.setText(name);
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceTextView.setText(Double.toString(price));

            if (!photo.isEmpty()) {
                mUri = Uri.parse(photo);
                mBitmap = Utils.getBitmapFromUri(mImageView,this,mUri);
                mImageView.setImageBitmap(mBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.clearComposingText();
        mQuantityTextView.clearComposingText();
        mPriceTextView.clearComposingText();
    }
}
