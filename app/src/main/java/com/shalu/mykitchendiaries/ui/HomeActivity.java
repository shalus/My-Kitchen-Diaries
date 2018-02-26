package com.shalu.mykitchendiaries.ui;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.ui.RecipeAdapter;
import com.shalu.mykitchendiaries.ui.RecipeAdapter.OnRecipeItemClickListener;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.RecipeContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnRecipeItemClickListener {

    private static final String INDEX = "index";
    private String TAG = HomeActivity.class.getSimpleName();
    private static final int RECIPE_LOADER_ID = 15;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_loading_indicator)
    public ProgressBar mProgressbar;

    @BindView(R.id.tv_empty_diary)
    TextView mEmptyDiary;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view)  NavigationView navigationView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefereshLayout;
    Cursor mRecipeData;
    RecipeAdapter mRecipeAdapter;
    GridLayoutManager layoutManager;
    int mPosition;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddRecipe.class);
                startActivity(intent);
            }
        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.view_recipe);

        layoutManager = new GridLayoutManager(this, numberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecipeAdapter = new RecipeAdapter(this);
        mRecyclerView.setAdapter(mRecipeAdapter);
        showRecipes();

    }

    private void showRecipes() {
        Loader<Cursor> loader = getSupportLoaderManager().getLoader(RECIPE_LOADER_ID);
        if(loader == null)
            getSupportLoaderManager().initLoader(RECIPE_LOADER_ID,null,cursorLoader);
        else
            getSupportLoaderManager().restartLoader(RECIPE_LOADER_ID,null,cursorLoader);
        showRecipesView();
    }

    private void showRecipesView() {
        mProgressbar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyRecipeView() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressbar.setVisibility(View.INVISIBLE);
        mSwipeRefereshLayout.setVisibility(View.INVISIBLE);
        mEmptyDiary.setVisibility(View.VISIBLE);

    }
    LoaderManager.LoaderCallbacks<Cursor> cursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(HomeActivity.this) {

                @Override
                protected void onStartLoading() {

                    if (mRecipeData != null) {
                        deliverResult(mRecipeData);

                    } else {
                        mProgressbar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(KitchenProvider.Recipes.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to asynchronously load data.");
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(Cursor data) {
                    mRecipeData = data;
                    super.deliverResult(data);
                }
            };

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mProgressbar.setVisibility(View.INVISIBLE);
            if (data != null) {
                if(data.getCount()!=0) {
                    showRecipesView();
                    if(mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                    mRecyclerView.smoothScrollToPosition(mPosition);
                }
                else
                    showEmptyRecipeView();
                mRecipeAdapter.swapCursor(data);
                Log.d(TAG, "Recipes present");
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mRecipeAdapter.swapCursor(null);
        }
    };
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_recipe) {
            Intent intent = new Intent(this, AddRecipe.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.view_recipe) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.manage_shopping) {
            Intent intent = new Intent(this, ShoppingActivity.class);
            startActivity(intent);

        }
        else if(id == R.id.about) {
            final Dialog dialog= new Dialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.about_dialog,null);
            dialog.setContentView(view);
            dialog.setTitle("About");
            dialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(int position, ImageView sharedView) {

        Intent intent = new Intent(HomeActivity.this, ViewRecipeActivity.class);
        mRecipeData.moveToPosition(position);
        String recipeTitle = mRecipeData.getString(mRecipeData.getColumnIndex(RecipeContract.TITLE));
        String imagePath = mRecipeData.getString(mRecipeData.getColumnIndex(RecipeContract.IMAGE_PATH));
        intent.putExtra(Intent.EXTRA_TEXT,recipeTitle);
        intent.putExtra("IMAGE",imagePath);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this, sharedView, sharedView.getTransitionName()).toBundle();
            startActivity(intent, bundle);
        }
        else
            startActivity(intent);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INDEX, layoutManager.findFirstCompletelyVisibleItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPosition = savedInstanceState.getInt(INDEX);
    }
}
