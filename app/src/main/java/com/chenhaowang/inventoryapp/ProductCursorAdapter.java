package com.chenhaowang.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.chenhaowang.inventoryapp.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        String nameString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        String quantityString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        final Integer quantity = Integer.parseInt(quantityString);
        String priceString = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        final Integer soldQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SOLD));
        long id = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));

        final Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        nameTextView.setText(nameString);
        quantityTextView.setText(quantityString);
        priceTextView.setText(priceString);

        Button trackButton = (Button) view.findViewById(R.id.track_button);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                if (quantity <= 0) {
                    Toast.makeText(context, "sold out!!! \nThe sold quantity = " + soldQuantity, Toast.LENGTH_SHORT).show();
                } else {
                    int curtQuantityAvailable = quantity - 1;
                    int curtSoldQuantity = soldQuantity + 1;
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, curtQuantityAvailable);
                    values.put(ProductEntry.COLUMN_PRODUCT_SOLD, curtSoldQuantity);
                    context.getContentResolver().update(currentUri, values, null, null);

                    String display = "successfully sold one!" +
                            "\nsold quantity = " + curtSoldQuantity;
                    Toast.makeText(context, display, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
