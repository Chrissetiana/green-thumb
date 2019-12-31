package com.chrissetiana.greenthumb;

import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;

/**
 * {@link PlantAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of plant data as its data source,
 * displaying a plant's name, description, and price for each row.
 */
public class PlantAdapter extends RecyclerView.Adapter {

    private Cursor mCursor;

    public PlantAdapter(Cursor cursor) {
        this.mCursor = cursor;
    }

    public void swapCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_plant, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mCursor == null) {
            return;
        }
        mCursor.moveToPosition(position);

        int id = mCursor.getColumnIndexOrThrow(PlantEntry._ID);
        String name = mCursor.getString(mCursor.getColumnIndexOrThrow(PlantEntry.COLUMN_NAME));
        int price = mCursor.getInt(mCursor.getColumnIndexOrThrow(PlantEntry.COLUMN_PRICE));

        ViewHolder view = (ViewHolder) holder;
        Resources resources = view.itemView.getContext().getResources();
        String priceString = resources.getQuantityString(R.plurals.number_of_credits, price, price);
        view.itemId = mCursor.getInt(id);
        view.mTextViewItemName.setText(name);
        view.mTextViewItemPrice.setText(priceString);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public int itemId;
        public TextView mTextViewItemName;
        public TextView mTextViewItemPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewItemName = itemView.findViewById(R.id.text_view_item_name);
            mTextViewItemPrice = itemView.findViewById(R.id.text_view_item_price);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlantDetailActivity.startActivity(v.getContext(), itemId);
                }
            });
        }
    }
}
