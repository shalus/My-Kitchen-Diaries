package com.shalu.mykitchendiaries.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.shalu.mykitchendiaries.BitmapUtils;
import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.IngredientContract;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.RecipeContract;
import com.shalu.mykitchendiaries.provider.ShoppingContract;
import com.shalu.mykitchendiaries.provider.StepContract;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.shalu.mykitchendiaries.ui.ShoppingActivity.updateWidgets;

public class ViewRecipeActivity extends AppCompatActivity {

    private static final int INGREDIENT_LOADER_ID = 11;
    private static final int STEP_LOADER_ID = 12;

    @BindView(R.id.lv_steps)
    ExpandableHeightListView mListSteps;

    @BindView(R.id.lv_ingredients)
    ExpandableHeightListView mListIngredient;

    @BindView(R.id.image)
    ImageView mImageView;
    Cursor mIngredientCursor, mStepCursor;
    IngredientAdapter ingredientAdapter;
    StepAdapter stepAdapter;
    String title, image;
    ArrayList<String> ingredientList, stepList, shoppingList;

    private static final String ING_INDEX = "i_index";
    private static final String STEP_INDEX = "s_index";
    int ingPos, stepPos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        title = "";
        if (getIntent().hasExtra(Intent.EXTRA_TEXT))
            title = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        getSupportActionBar().setTitle(title);
        //mTitle.setText(title);

        if (getIntent().hasExtra("IMAGE"))
            image = getIntent().getStringExtra("IMAGE");
        if(image!=null)
            mImageView.setImageBitmap(BitmapFactory.decodeFile(image));
        shoppingList = new ArrayList<String>();
        showIngredientAndSteps();
        ingredientAdapter = new IngredientAdapter(this, mIngredientCursor);
        mListIngredient.setAdapter(ingredientAdapter);
        mListIngredient.setExpanded(true);
        stepAdapter = new StepAdapter(this, mStepCursor);
        mListSteps.setAdapter(stepAdapter);
        mListSteps.setExpanded(true);
        ingredientList = new ArrayList<String>();
        stepList = new ArrayList<String>();
        if(savedInstanceState != null) {
            ingPos = savedInstanceState.getInt(ING_INDEX);
            stepPos = savedInstanceState.getInt(STEP_INDEX);
        }
    }

    private void showIngredientAndSteps() {
        Loader<Cursor> loader = getSupportLoaderManager().getLoader(INGREDIENT_LOADER_ID);
        if(loader == null)
            getSupportLoaderManager().initLoader(INGREDIENT_LOADER_ID,null,ingredientLoader);
        else
            getSupportLoaderManager().restartLoader(INGREDIENT_LOADER_ID,null,ingredientLoader);

        Loader<Cursor> loader2 = getSupportLoaderManager().getLoader(STEP_LOADER_ID);
        if(loader == null)
            getSupportLoaderManager().initLoader(STEP_LOADER_ID,null,stepLoader);
        else
            getSupportLoaderManager().restartLoader(STEP_LOADER_ID,null,stepLoader);
    }

    class IngredientAdapter extends CursorAdapter {
        public IngredientAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_ingredient, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ImageButton shopIngredient = (ImageButton) view.findViewById(R.id.delete_ingredient);
            TextView ingText = (TextView) view.findViewById(R.id.tv_ingredient_item);
            final String ingredient = cursor.getString(cursor.getColumnIndex(IngredientContract.ING));
            ingText.setText(ingredient);
            Cursor c = getContentResolver().query(KitchenProvider.Shopping.CONTENT_URI,null,ShoppingContract.ITEM+"=?",new String[] {ingredient},null);
            if(c.getCount() == 0) {
                shopIngredient.setImageResource(R.drawable.ic_add_shopping_cart);
                shopIngredient.setTag(R.drawable.ic_add_shopping_cart);
            }
            else {
                shopIngredient.setImageResource(R.drawable.ic_added_shopping);
                shopIngredient.setTag(R.drawable.ic_added_shopping);
            }
            shopIngredient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (Integer) view.getTag();
                    if(tag == R.drawable.ic_add_shopping_cart) {
                        ContentValues values = new ContentValues();
                        values.put(ShoppingContract.ITEM, ingredient);
                        getContentResolver().insert(KitchenProvider.Shopping.CONTENT_URI, values);
                        shoppingList.add(ingredient);
                        shopIngredient.setTag(R.drawable.ic_added_shopping);
                        shopIngredient.setImageResource(R.drawable.ic_added_shopping);
                        Toast.makeText(ViewRecipeActivity.this, getString(R.string.added_to_cart),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        shoppingList.remove(ingredient);
                        getContentResolver().delete(KitchenProvider.Shopping.CONTENT_URI, ShoppingContract.ITEM+"=?", new String[]{ingredient});
                        shopIngredient.setTag(R.drawable.ic_add_shopping_cart);
                        shopIngredient.setImageResource(R.drawable.ic_add_shopping_cart);
                        Toast.makeText(ViewRecipeActivity.this, getString(R.string.removed_from_cart),Toast.LENGTH_SHORT).show();
                    }
                    updateWidgets(getBaseContext());
                }
            });
        }
    }

    class StepAdapter extends CursorAdapter {
        public StepAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_step, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageButton delStep = (ImageButton) view.findViewById(R.id.delete_step);
            TextView stepText = (TextView) view.findViewById(R.id.tv_step_item);
            String step = cursor.getString(cursor.getColumnIndex(StepContract.STEP));
            stepText.setText(step);
            delStep.setVisibility(View.GONE);
        }
    }
    LoaderManager.LoaderCallbacks<Cursor> ingredientLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(ViewRecipeActivity.this) {

                @Override
                protected void onStartLoading() {

                    if (mIngredientCursor != null) {
                        deliverResult(mIngredientCursor);

                    } else {
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(KitchenProvider.Ingredients.CONTENT_URI,
                                null,
                                IngredientContract.RECIPE+"=?",
                                new String[] {title},
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(Cursor data) {
                    mIngredientCursor = data;
                    super.deliverResult(data);
                }
            };

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {

                ingredientAdapter.changeCursor(data);
                if(ingPos == ListView.INVALID_POSITION)
                    ingPos = 0;
                mListIngredient.smoothScrollToPosition(ingPos);
                for (mIngredientCursor.moveToFirst(); !mIngredientCursor.isAfterLast(); mIngredientCursor.moveToNext()) {
                    ingredientList.add(mIngredientCursor.getString(mIngredientCursor.getColumnIndex(IngredientContract.ING)));
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ingredientAdapter.changeCursor(null);
        }
    };

    LoaderManager.LoaderCallbacks<Cursor> stepLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(ViewRecipeActivity.this) {

                @Override
                protected void onStartLoading() {

                    if (mStepCursor != null) {
                        deliverResult(mStepCursor);

                    } else {
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(KitchenProvider.Steps.CONTENT_URI,
                                null,
                                StepContract.RECIPE+"=?",
                                new String[] {title},
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(Cursor data) {
                    mStepCursor = data;
                    super.deliverResult(data);
                }
            };

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {

                stepAdapter.changeCursor(data);

                if(stepPos == ListView.INVALID_POSITION)
                    stepPos = 0;
                mListSteps.smoothScrollToPosition(ingPos);
                for (mStepCursor.moveToFirst(); !mStepCursor.isAfterLast(); mStepCursor.moveToNext()) {
                    stepList.add(mStepCursor.getString(mStepCursor.getColumnIndex(StepContract.STEP)));
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            stepAdapter.changeCursor(null);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.action_delete: {
                deleteRecipe();
                BitmapUtils.deleteImageFile(this, image);
                BitmapUtils.refreshGallery(this, image);
                Intent homeActivityIntent = new Intent(this, HomeActivity.class);
                startActivity(homeActivityIntent);
                break;
            }
            case R.id.action_edit: {
                Intent openAddRecipeForEdit = new Intent(this, AddRecipe.class);
                openAddRecipeForEdit.putExtra(getString(R.string.recipe_name), title);
                openAddRecipeForEdit.putExtra(getString(R.string.image_path), image);
                openAddRecipeForEdit.putStringArrayListExtra(getString(R.string.ingredient_list), ingredientList);
                openAddRecipeForEdit.putStringArrayListExtra(getString(R.string.step_list), stepList);
                startActivity(openAddRecipeForEdit);
                deleteRecipe();
                break;
            }
            case R.id.action_share: {
                StringBuilder sbIngredients = new StringBuilder();
                for(String ingredient : ingredientList) {
                    sbIngredients.append(ingredient);
                    sbIngredients.append("\n");
                }
                StringBuilder sbSteps = new StringBuilder();
                for(String step: stepList) {
                    sbSteps.append(step);
                    sbSteps.append("\n");
                }
                String text = title+"\n\nIngredients\n"+sbIngredients.toString()+"\n\nSteps\n"+sbSteps.toString();
                shareRecipe(text);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteRecipe() {
        getContentResolver().delete(KitchenProvider.Steps.CONTENT_URI,StepContract.RECIPE+"=?",new String[] {title});
        getContentResolver().delete(KitchenProvider.Ingredients.CONTENT_URI, IngredientContract.RECIPE+"=?",new String[]{title});
        int n = getContentResolver().delete(KitchenProvider.Recipes.CONTENT_URI,RecipeContract.TITLE+"=?",new String[] {title});
        if(n > 0) {
            Toast.makeText(this,getResources().getString(R.string.delete_recipe), Toast.LENGTH_SHORT).show();
        }

    }

    public void shareRecipe(String textToShare) {
        String mimeType = "text/plain";
        String title = "Check this recipe out!";
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(textToShare)
                .startChooser();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ING_INDEX, mListIngredient.getFirstVisiblePosition());
        outState.putInt(STEP_INDEX, mListSteps.getFirstVisiblePosition());
    }
}
