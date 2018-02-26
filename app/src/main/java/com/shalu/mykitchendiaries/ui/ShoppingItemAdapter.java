package com.shalu.mykitchendiaries.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.ShoppingContract;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.shalu.mykitchendiaries.ui.ShoppingActivity.updateWidgets;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ItemViewHolder> {
    private Cursor mCursor;
    private Context mContext;

    public ShoppingItemAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.shopping_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        // Move the mCursor to the position of the item to be displayed
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        // Update the view holder with the information needed to display
        final String name = mCursor.getString(mCursor.getColumnIndex(ShoppingContract.ITEM));
        long id = mCursor.getLong(mCursor.getColumnIndex(ShoppingContract._ID));

        holder.nameTextView.setText(name);
        holder.itemView.setTag(id);
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = (long) holder.itemView.getTag();
                mContext.getContentResolver().delete(KitchenProvider.Shopping.CONTENT_URI, ShoppingContract._ID+"="+id,null);
                Cursor c = mContext.getContentResolver().query(KitchenProvider.Shopping.CONTENT_URI,null,null,null,null);
                updateWidgets(mContext);
                swapCursor(c);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /**
     * Swaps the Cursor currently held in the adapter with a new one
     * and triggers a UI refresh
     *
     * @param newCursor the new cursor that will replace the existing one
     */
    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    /**
     * Inner class to hold the views needed to display a single item in the recycler-view
     */
    class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_shopping_item) TextView nameTextView;
        @BindView(R.id.button_remove) ImageButton removeButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}

