package com.shalu.mykitchendiaries.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.shalu.mykitchendiaries.provider.StepContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddRecipe extends AppCompatActivity {

    @BindView(R.id.lv_ingredients) ExpandableHeightListView mListViewIngredients;
    @BindView(R.id.lv_steps) ExpandableHeightListView mListViewSteps;
    @BindView(R.id.button_add_step) ImageButton mAddStepButton;
    @BindView(R.id.button_add_ingredient) ImageButton mAddIngredientButton;
    @BindView(R.id.et_ingredient) EditText mEditTextIngredient;
    @BindView(R.id.et_step) EditText mEditTextStep;
    @BindView(R.id.ib_image) ImageButton mImageButton;
    @BindView(R.id.iv_pic) public ImageView mImageView;
    @BindView(R.id.et_title) EditText mEditTextTitle;

    public static final int PICK_IMAGE = 2;
    private static final String INGREDIENTS_KEY = "ingredients";
    private static final String STEPS_KEY = "steps";
    private static final String IMAGE_KEY = "image";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String ING_INDEX = "i_index";
    private static final String STEP_INDEX = "s_index";
    private String mTempPhotoPath;

    private Bitmap mResultsBitmap;
    private String mImagePath;
    private static final String FILE_PROVIDER_AUTHORITY = "com.shalu.fileprovider";

    public ArrayList<String> ingArray;
    public ArrayList<String> stepArray;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ingArray = new ArrayList<String>();
        stepArray = new ArrayList<String>();
        if(savedInstanceState !=null) {
            ingArray = savedInstanceState.getStringArrayList(INGREDIENTS_KEY);
            stepArray = savedInstanceState.getStringArrayList(STEPS_KEY);
            mTempPhotoPath = savedInstanceState.getString(IMAGE_KEY);
            setImage();
        }
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.recipe_name)))
            mEditTextTitle.setText(intent.getStringExtra(getString(R.string.recipe_name)));
        if(intent.hasExtra(getString(R.string.image_path))) {
            String path = intent.getStringExtra(getString(R.string.image_path));
            if(path!=null && path.length() > 0) {
                mTempPhotoPath = path;
                setImage();
            }
        }
        if(intent.hasExtra(getString(R.string.ingredient_list)))
            ingArray = intent.getStringArrayListExtra(getString(R.string.ingredient_list));
        if(intent.hasExtra(getString(R.string.step_list)))
            stepArray = intent.getStringArrayListExtra(getString(R.string.step_list));

        class IngredientAdapter extends ArrayAdapter<String > {

            public IngredientAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
                super(context, resource, objects);
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String ing = getItem(position);
                if(convertView == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    convertView = inflater.inflate(R.layout.list_item_ingredient, parent, false);
                }
                ImageButton delIngredient = (ImageButton) convertView.findViewById(R.id.delete_ingredient);
                TextView ingText = (TextView) convertView.findViewById(R.id.tv_ingredient_item);
                ingText.setText(ingArray.get(position));
                delIngredient.setTag(position);
                delIngredient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        ingArray.remove(getItem(pos));
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        }

        final IngredientAdapter adapter = new IngredientAdapter(this, R.layout.list_item_ingredient, ingArray);
        mListViewIngredients.setAdapter(adapter);
        mListViewIngredients.setExpanded(true);
        if(savedInstanceState != null) {
            int position = savedInstanceState.getInt(ING_INDEX);
            if(position == ListView.INVALID_POSITION) position = 0;
            mListViewIngredients.smoothScrollToPosition(position);
        }
        mAddIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditTextIngredient.getText()!=null && mEditTextIngredient.getText().length() > 0) {
                    ingArray.add(mEditTextIngredient.getText().toString());
                    adapter.notifyDataSetChanged();
                    mEditTextIngredient.setText("");
                }
            }
        });

        class StepAdapter extends ArrayAdapter<String > {

            public StepAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
                super(context, resource, objects);
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String ing = getItem(position);
                if(convertView == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    convertView = inflater.inflate(R.layout.list_item_step, parent, false);
                }
                ImageButton delStep = (ImageButton) convertView.findViewById(R.id.delete_step);
                TextView stepText = (TextView) convertView.findViewById(R.id.tv_step_item);
                stepText.setText(stepArray.get(position));
                delStep.setTag(position);
                delStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        stepArray.remove(getItem(pos));
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        }

        final StepAdapter adapterSteps = new StepAdapter(this, R.layout.list_item_step, stepArray);
        mListViewSteps.setAdapter(adapterSteps);
        mListViewSteps.setExpanded(true);
        if(savedInstanceState != null) {
            int position = savedInstanceState.getInt(STEP_INDEX);
            if(position == ListView.INVALID_POSITION) position = 0;
            mListViewSteps.smoothScrollToPosition(position);
        }
        mAddStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditTextStep.getText()!=null && mEditTextStep.getText().length() > 0) {
                    stepArray.add(mEditTextStep.getText().toString());
                    adapterSteps.notifyDataSetChanged();
                    mEditTextStep.setText("");
                }
            }
        });
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AddRecipe.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // If you do not have permission, request it
                    ActivityCompat.requestPermissions(AddRecipe.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipe.this);
                    builder.setTitle(R.string.image_action)
                            .setItems(R.array.image_action_array, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            openGallery();
                                            break;
                                        case 1:
                                            launchCamera();
                                            break;
                                    }
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipe.this);
                builder.setTitle(R.string.image_action)
                        .setItems(R.array.set_image_action_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        BitmapUtils.shareImage(AddRecipe.this, mTempPhotoPath);
                                        break;
                                    case 1:
                                        BitmapUtils.deleteImageFile(AddRecipe.this, mTempPhotoPath);
                                        mResultsBitmap = null;
                                        mImageView.setVisibility(View.INVISIBLE);
                                        mImageButton.setVisibility(View.VISIBLE);
                                        break;
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

   private void openGallery() {
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode!=RESULT_OK)
            return;
        if (requestCode == PICK_IMAGE && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mTempPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            setImage();

        }
        if(requestCode == REQUEST_IMAGE_CAPTURE) {
            setImage();
        }

    }

    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

       public void saveRecipe(View view) {

            saveImg();
            ContentValues values = new ContentValues();
            String title = mEditTextTitle.getText().toString().trim();
            if(title == null || title.length() == 0) {
                Toast.makeText(this, R.string.enter_title, Toast.LENGTH_SHORT).show();
                mEditTextTitle.setFocusable(true);
                return;
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            values.put(RecipeContract.TITLE, title);
            values.put(RecipeContract.IMAGE_PATH, mImagePath);
            values.put(RecipeContract.COLUMN_DATE, timeStamp);
            Uri uri = getContentResolver().insert(KitchenProvider.Recipes.CONTENT_URI, values);
            if(ingArray != null && ingArray.size() > 0) {
                ContentValues[] ingredientValues = new ContentValues[ingArray.size()];
                for (int i = 0; i < ingArray.size(); i++) {
                    ingredientValues[i] = new ContentValues();
                    ingredientValues[i].put(IngredientContract.ING, ingArray.get(i));
                    ingredientValues[i].put(IngredientContract.RECIPE, title);
                }
                getContentResolver().bulkInsert(KitchenProvider.Ingredients.CONTENT_URI,ingredientValues);
            }

            if(stepArray != null && stepArray.size() > 0) {
                ContentValues[] stepValues = new ContentValues[stepArray.size()];
                for (int i = 0; i < stepArray.size(); i++) {
                    stepValues[i] = new ContentValues();
                    stepValues[i].put(StepContract.STEP, stepArray.get(i));
                    stepValues[i].put(StepContract.RECIPE, title);
                }
                getContentResolver().bulkInsert(KitchenProvider.Steps.CONTENT_URI,stepValues);
            }

            if(uri != null) {
                Toast.makeText(this, R.string.recipe_added, Toast.LENGTH_SHORT).show();
                launchHomeActivity();
            }


        }

    private void launchHomeActivity() {
        Intent intent = new Intent(AddRecipe.this, HomeActivity.class);
        startActivity(intent);
    }

    private void setImage(){
        mImageButton.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        if(targetH != 0 && targetW != 0)
            mResultsBitmap = BitmapUtils.setPic(targetW, targetH, mTempPhotoPath);
        else
            mResultsBitmap = BitmapFactory.decodeFile(mTempPhotoPath);
        mImageView.setImageBitmap(mResultsBitmap);
    }

    public void saveImg() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {

            if(mTempPhotoPath != null && mTempPhotoPath.length() > 0) {
                // Delete the temporary image file
                BitmapUtils.deleteImageFile(this, mTempPhotoPath);
                Log.d("TAG", "temp file deleted");
            }

            if(mResultsBitmap !=null) {
                // Save the image
                mImagePath = BitmapUtils.saveImage(this, mResultsBitmap);
                Log.d("TAG", "saved");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera

                    // Delete the temporary image file
                    BitmapUtils.deleteImageFile(this, mTempPhotoPath);

                    // Save the image
                    BitmapUtils.saveImage(this, mResultsBitmap);
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_exit)
                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        launchHomeActivity();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(INGREDIENTS_KEY, ingArray);
        outState.putStringArrayList(STEPS_KEY, stepArray);
        outState.putString(IMAGE_KEY, mTempPhotoPath);
        outState.putInt(ING_INDEX, mListViewIngredients.getFirstVisiblePosition());
        outState.putInt(STEP_INDEX, mListViewSteps.getFirstVisiblePosition());
    }
}
