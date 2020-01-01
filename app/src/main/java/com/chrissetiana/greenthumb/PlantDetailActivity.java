package com.chrissetiana.greenthumb;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;
import com.chrissetiana.greenthumb.data.Plant;
import com.chrissetiana.greenthumb.data.PlantCartHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * {@link PlantDetailActivity} displays a plant's name and description
 * and allows the user to check out the plant to the shopping cart.
 */
public class PlantDetailActivity extends AppCompatActivity
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String INTENT_EXTRA_ITEM = "item_id";
    private static final int PLANT_DETAIL_LOADER = 2;

    private int mItemId;
    private Plant mPlant;

    private Cursor mCursor;

    private Toolbar mToolbar;
    private TextView mItemDescription;
    private TextView mItemPrice;

    public static void startActivity(Context context, int itemPosition) {
        Intent i = new Intent(context, PlantDetailActivity.class);
        i.putExtra(INTENT_EXTRA_ITEM, itemPosition);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mItemDescription = findViewById(R.id.text_view_item_description);
        mItemPrice = findViewById(R.id.text_view_item_price);
        final FloatingActionButton fab = findViewById(R.id.fab);

        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        fab.setOnClickListener(this);

        mItemId = getIntent().getIntExtra(INTENT_EXTRA_ITEM, 0);

        getSupportLoaderManager().initLoader(PLANT_DETAIL_LOADER, null, this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mPlant == null)
                    return;

                int quantity = 1;
                PlantCartHelper.addCartQuantity(this, mPlant.id, quantity);
                Snackbar.make(v, R.string.shopping_cart_item_added, Snackbar.LENGTH_SHORT).show();

                Analytics.logEventAddToCart(this, mPlant, quantity);

                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PlantEntry._ID,
                PlantEntry.COLUMN_NAME,
                PlantEntry.COLUMN_DESCRIPTION,
                PlantEntry.COLUMN_PRICE
        };
        String selection = PlantEntry._ID + " = " + mItemId;
        return new CursorLoader(this,
                PlantEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        mCursor.moveToFirst();

        mPlant = new Plant(data);
        mToolbar.setTitle(mPlant.name);
        mItemDescription.setText(mPlant.description);
        mItemPrice.setText(getString(R.string.plant_credits, mPlant.price));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
}
