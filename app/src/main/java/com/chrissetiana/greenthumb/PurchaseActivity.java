package com.chrissetiana.greenthumb;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;

public class PurchaseActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PURCHASE_LOADER = 4;

    private RecyclerView mRecyclerView;
    private PurchaseAdapter mPurchaseAdapter;
    private TextView mTextViewPurchaseEmpty;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PurchaseActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.purchases_title);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        mRecyclerView = findViewById(R.id.recycler_view);
        mTextViewPurchaseEmpty = findViewById(R.id.text_view_empty_purchase);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mPurchaseAdapter = new PurchaseAdapter();
        mRecyclerView.setAdapter(mPurchaseAdapter);

        getSupportLoaderManager().initLoader(PURCHASE_LOADER, null, (androidx.loader.app.LoaderManager.LoaderCallbacks<Object>) this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PlantEntry._ID,
                PlantEntry.COLUMN_NAME,
                PlantEntry.COLUMN_PURCHASED_QUANTITY
        };
        String selection = PlantEntry.COLUMN_PURCHASED_QUANTITY + " > 0";
        return new CursorLoader(this,
                PlantEntry.CONTENT_URI,
                projection,
                selection,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPurchaseAdapter.swapCursor(data);

        boolean emptyPurchases = data.getCount() == 0;
        mTextViewPurchaseEmpty.setVisibility(emptyPurchases ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPurchaseAdapter.swapCursor(null);
    }
}
