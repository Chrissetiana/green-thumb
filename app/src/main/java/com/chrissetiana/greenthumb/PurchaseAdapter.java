package com.chrissetiana.greenthumb;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;
import com.chrissetiana.greenthumb.data.PlantCartHelper;

/**
 * {@link PurchaseAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of purchased plant data as its data source,
 * displaying a plant's name and purchased quantity.
 */
public class PurchaseAdapter extends RecyclerView.Adapter {

    private Cursor mCursor;

    public void swapCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_purchase, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mCursor == null) {
            return;
        }
        mCursor.moveToPosition(position);

        ViewHolder view = (ViewHolder) holder;
        view.itemId = mCursor.getInt(mCursor.getColumnIndexOrThrow(PlantEntry._ID));
        view.itemName = mCursor.getString(mCursor.getColumnIndexOrThrow(PlantEntry.COLUMN_NAME));
        view.itemQuantity = mCursor.getString(mCursor.getColumnIndexOrThrow(PlantEntry.COLUMN_PURCHASED_QUANTITY));

        view.mTextViewItemName.setText(view.itemName);
        view.mTextViewQuantity.setText(view.itemQuantity);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public int itemId;
        public String itemName;
        public String itemQuantity;
        public TextView mTextViewItemName;
        public TextView mTextViewQuantity;
        public ImageButton mButtonRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewItemName = itemView.findViewById(R.id.text_view_item_name);
            mTextViewQuantity = itemView.findViewById(R.id.text_view_item_quantity);
            mButtonRemove = itemView.findViewById(R.id.imagebutton_remove_purchase);

            mButtonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlantCartHelper.removePurchase(v.getContext(), itemId);
                }
            });
        }
    }
}
