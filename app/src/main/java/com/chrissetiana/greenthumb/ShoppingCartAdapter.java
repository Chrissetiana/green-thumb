package com.chrissetiana.greenthumb;

import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;
import com.chrissetiana.greenthumb.data.PlantCartHelper;
import com.google.android.material.snackbar.Snackbar;

/**
 * {@link ShoppingCartAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of plant data as its data source,
 * displaying a plant's name and shopping cart quantity.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter {

    private Cursor mCursor;

    public void swapCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_shopping_cart, parent, false);
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
        int quantity = mCursor.getInt(mCursor.getColumnIndexOrThrow(PlantEntry.COLUMN_CART_QUANTITY));
        int totalPrice = price * quantity;

        ViewHolder view = (ViewHolder) holder;
        Resources resources = view.itemView.getContext().getResources();
        String itemPrice = resources.getQuantityString(R.plurals.number_of_credits, price, price);
        String totalPriceString = resources.getQuantityString(R.plurals.number_of_credits, totalPrice, totalPrice);
        view.itemId = mCursor.getInt(id);
        view.mTextViewItemName.setText(name);
        view.mTextViewItemPrice.setText(itemPrice);
        view.mTextViewItemQuantity.setText(String.valueOf(quantity));
        view.mTextViewItemTotalPrice.setText(totalPriceString);
        view.updateUi();
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public int itemId;
        public TextView mTextViewItemName;
        public TextView mTextViewItemPrice;
        public TextView mTextViewItemQuantity;
        public TextView mTextViewItemTotalPrice;
        public Button mButtonSubtract;
        public Button mButtonAdd;
        public ImageButton mButtonRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewItemName = itemView.findViewById(R.id.text_view_item_name);
            mTextViewItemPrice = itemView.findViewById(R.id.text_view_item_price);
            mTextViewItemQuantity = itemView.findViewById(R.id.text_view_item_quantity);
            mTextViewItemTotalPrice = itemView.findViewById(R.id.text_view_total_price);
            mButtonSubtract = itemView.findViewById(R.id.button_quantity_subtract);
            mButtonAdd = itemView.findViewById(R.id.button_quantity_add);
            mButtonRemove = itemView.findViewById(R.id.imagebutton_remove_cart);

            mButtonSubtract.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlantCartHelper.subtractCartQuantity(v.getContext(), itemId, 1);
                    updateUi();
                }
            });

            mButtonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlantCartHelper.addCartQuantity(v.getContext(), itemId, 1);
                    updateUi();
                }
            });

            mButtonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Save quantity for undo
                    final int previousQuantity = Integer.parseInt(mTextViewItemQuantity.getText().toString());

                    PlantCartHelper.removeFromCart(v.getContext(), itemId);
                    notifyItemRemoved(getAdapterPosition());

                    Snackbar.make(v, String.format("'%s' removed from cart", mTextViewItemName.getText()), Snackbar.LENGTH_SHORT)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PlantCartHelper.addCartQuantity(v.getContext(), itemId, previousQuantity);
                                }
                            })
                            .show();
                }
            });
        }

        /**
         * Updates the UI after first view binding after each user interaction.
         */
        private void updateUi() {
            // Disable the subtraction button if there's only one quantity for the item.
            // Enable otherwise.
            if (1 == Integer.parseInt(mTextViewItemQuantity.getText().toString())) {
                mButtonSubtract.setEnabled(false);
            } else {
                mButtonSubtract.setEnabled(true);
            }
        }
    }
}
