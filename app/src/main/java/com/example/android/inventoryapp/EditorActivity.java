package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

/**
 * Created by garywhite61 on 20/07/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.et_product_name) EditText mNameEditText;
    @BindView(R.id.et_price) EditText mPriceEditText;
    @BindView(R.id.et_quantity) EditText mQuantityEditText;
    @BindView(R.id.tv_sales) TextView mSalesTextView;
    @BindView(R.id.photo) ImageView mImageView;
    @BindView(R.id.btn_gallery) Button btnGallery;
    @BindView(R.id.btn_photo) Button btnPhoto;

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST = 2;

    private boolean isGalleryPicture = false;

    private static final String STATE_URI = "STATE_URI";

    // Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
    private boolean mInventoryHasChanged = false;

    Uri mCurrentInventoryUri;
    private Uri mUri;
    private Bitmap mBitmap;
    String mCurrentPhotoPath;

    Context context;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        mCurrentInventoryUri = getIntent().getData();

        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImageView.setImageBitmap( Utils.getBitmapFromUri(mImageView,EditorActivity.this,mUri));
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mSalesTextView.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            btnPhoto.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    btnPhoto.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void openImageSelector(View view) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void takePicture(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try{
            File file =  createImageFile();

            mUri = FileProvider.getUriForFile(getApplication().getApplicationContext(),
                    "com.example.android.inventoryapp.fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mBitmap =  Utils.getBitmapFromUri(mImageView,EditorActivity.this,mUri);
                mImageView.setImageBitmap(mBitmap);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            mBitmap = Utils.getBitmapFromUri(mImageView,EditorActivity.this,mUri);;
            mImageView.setImageBitmap(mBitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            isGalleryPicture = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String salesString = mSalesTextView.getText().toString().trim();
        String photoString;

        if (mUri != null) {
            photoString = mUri.toString();
        } else {
            photoString = "";
        }

        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "You must enter data", Toast.LENGTH_SHORT).show();
            Intent i = getIntent();
            finish();
            startActivity(i);
            return;
        }

        int sales = 0;
        if (!TextUtils.isEmpty(salesString)) {
            sales = Integer.parseInt(salesString);
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
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_SALES, sales);
        values.put(ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO, photoString);

        if (mCurrentInventoryUri == null) {
            Uri uri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.error_saving_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String [] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_INVENTORY_NAME,
                ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE,
                ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY,
                ProductContract.ProductEntry.COLUMN_INVENTORY_SALES,
                ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO};

        return new CursorLoader(this, mCurrentInventoryUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_QUANTITY);
            int salesColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_SALES);
            int photoColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_INVENTORY_PHOTO);

            String name = data.getString(nameColumnIndex);
            int sales = data.getInt(salesColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            double price = data.getDouble(priceColumnIndex);
            String photo = data.getString(photoColumnIndex);

            mNameEditText.setText(name);
            mSalesTextView.setText(Integer.toString(sales));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));

            if (!photo.isEmpty()) {
                mUri = Uri.parse(photo);
                mBitmap = Utils.getBitmapFromUri(mImageView,EditorActivity.this,mUri);
                mImageView.setImageBitmap(mBitmap);
            }
            hideSoftKeyboard();
        }
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.clearComposingText();
        mSalesTextView.clearComposingText();
        mQuantityEditText.clearComposingText();
        mPriceEditText.clearComposingText();
    }
}
