package com.shalu.mykitchendiaries.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.RecipeContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sarum on 2/7/2018.
 */

class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
    private static final String TAG = RecipeAdapter.class.getSimpleName();

    private OnRecipeItemClickListener mClickHandler;
    private Cursor mCursor;

    RecipeAdapter(OnRecipeItemClickListener clickListener) {
        mClickHandler = clickListener;
    }

    public interface OnRecipeItemClickListener {
        void onClick(int position, ImageView imageView);
    }
    @Override
    public RecipeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        RecipeHolder holder = new RecipeHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecipeHolder holder, int position) {
        if(mCursor!=null) {
            mCursor.moveToPosition(position);
            int imgIndex = mCursor.getColumnIndex(RecipeContract.IMAGE_PATH);
            String path = mCursor.getString(imgIndex);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            holder.mRecipeThumbnail.setImageBitmap(bitmap);
            holder.mRecipeName.setText(mCursor.getString(mCursor.getColumnIndex(RecipeContract.TITLE)));
        }
    }

    @Override
    public int getItemCount() {
        if(mCursor!=null)
            return mCursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor data){
        mCursor = data;
        notifyDataSetChanged();
    }

    public class RecipeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.iv_recipe_thumbnail) public ImageView mRecipeThumbnail;
        @BindView(R.id.recipe_name) TextView mRecipeName;

        public RecipeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mClickHandler.onClick(position, mRecipeThumbnail);
        }
    }
}
