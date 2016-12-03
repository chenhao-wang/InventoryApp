package com.chenhaowang.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.chenhaowang.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentUri;

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mQuantityByEditText;
    private EditText mSoldQuantityEditText;
    private ImageView mImageView;

    private float mPrice;
    private int mQuantity = 0;
    private int mQuantityBy = 1;
    private int mSoldQuantity = 0;
    private Bitmap mBitmap;

    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        if (mCurrentUri == null) {
            setTitle(getString(R.string.editor_activity_title_add_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_exist_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityByEditText = (EditText) findViewById(R.id.edit_quantity_by);
        mSoldQuantityEditText = (EditText) findViewById(R.id.sold_view);
        mQuantityByEditText.setText("" + mQuantityBy);
        mImageView = (ImageView) findViewById(R.id.image_view);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Button decreaseQuantity = (Button) findViewById(R.id.quantity_decrease_button);
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQuantity();
                getQuantityBy();
                if (mQuantity >= mQuantityBy) {
                    mQuantity -= mQuantityBy;
                    mQuantityEditText.setText("" + mQuantity);
                }
            }
        });

        Button increaseQuantity = (Button) findViewById(R.id.quantity_increase_button);
        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQuantity();
                getQuantityBy();
                mQuantity += mQuantityBy;
                mQuantityEditText.setText("" + mQuantity);
            }
        });

        Button saleButton = (Button) findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQuantity();
                getQuantityBy();
                getSoldQuantity();
                if (mQuantity >= 1) {
                    mQuantity--;
                    mSoldQuantity++;

                    mQuantityEditText.setText("" + mQuantity);
                    mSoldQuantityEditText.setText("" + mSoldQuantity);
                }
            }
        });

        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");

                getQuantity();
                getSoldQuantity();
                String priceString = mPriceEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(priceString)) {
                    mPrice = Float.parseFloat(priceString);
                }

                String subject = getString(R.string.order_more) + "\t" + mNameEditText.getText().toString().trim();
                String attachment = getString(R.string.order_quantity_available) + " : " + mQuantity
                        + "\n" + getString(R.string.order_product_price) + " : " + priceString
                        + "\n" + getString(R.string.order_product_sold_quantity) + mSoldQuantity;

                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, attachment);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSoldQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
    }

    static final int REQUEST_IMAGE_GET = 1;

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            try {
                mBitmap = decodeUri(fullPhotoUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mImageView.setImageBitmap(mBitmap);
        }
    }

    private void getQuantity() {
        String quantityString = mQuantityEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)) {
            mQuantity = Integer.parseInt(quantityString);
        }
    }

    private void getQuantityBy() {
        String quantityByString = mQuantityByEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityByString)) {
            mQuantityBy = Integer.parseInt(quantityByString);
        }
    }

    private void getSoldQuantity() {
        String soldQuantityString = mSoldQuantityEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(soldQuantityString)) {
            mSoldQuantity = Integer.parseInt(soldQuantityString);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete_product:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:

                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String soldString = mSoldQuantityEditText.getText().toString().trim();

        // all fields are required
        if (mCurrentUri == null &&
                (TextUtils.isEmpty(nameString) ||
                        TextUtils.isEmpty(quantityString) ||
                        TextUtils.isEmpty(priceString) ||
                        TextUtils.isEmpty(soldString) ||
                        mBitmap == null)) {
            Toast.makeText(EditorActivity.this, "INSERT FAILED :( \nall fields are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);

        if (!TextUtils.isEmpty(quantityString)) {
            mQuantity = Integer.parseInt(quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        }

        if (!TextUtils.isEmpty(priceString)) {
            mPrice = Float.parseFloat(priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, mPrice);
        }

        if (!TextUtils.isEmpty(soldString)) {
            mSoldQuantity = Integer.parseInt(soldString);
            values.put(ProductEntry.COLUMN_PRODUCT_SOLD, mSoldQuantity);
        }

        if (mBitmap != null) {
            byte[] imageByteArray = getBitmapAsByteArray(mBitmap);
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);
        }

        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
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

    private void deleteProduct() {
        if (mCurrentUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SOLD,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            Integer quantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
            Float price = data.getFloat(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
            Integer sold = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SOLD));
            byte[] imageByteArray = data.getBlob(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
            Bitmap bitmap = null;
            if (imageByteArray != null && imageByteArray.length != 0) {
                bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            }

            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Float.toString(price));
            mSoldQuantityEditText.setText(Integer.toString(sold));
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSoldQuantityEditText.setText("");
        mImageView.setImageBitmap(null);
    }
}
