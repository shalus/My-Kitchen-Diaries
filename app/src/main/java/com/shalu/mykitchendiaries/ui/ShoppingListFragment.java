package com.shalu.mykitchendiaries.ui;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.ShoppingContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.shalu.mykitchendiaries.ui.ShoppingActivity.SHOPPING_LIST_INDEX;
import static com.shalu.mykitchendiaries.ui.ShoppingActivity.sposition;
import static com.shalu.mykitchendiaries.ui.ShoppingActivity.updateWidgets;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingListFragment extends Fragment {


    @BindView(R.id.et_shopping) EditText mItem;
    @BindView(R.id.button_add) ImageButton button;
    private ShoppingItemAdapter shoppingItemAdapter;
    private Cursor cursor;
    LinearLayoutManager mLayoutManager;
    @BindView(R.id.rv_list) RecyclerView recyclerView;
    private Unbinder unbinder;

    public ShoppingListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToList();
            }
        });
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        cursor = getAllItems();
        shoppingItemAdapter = new ShoppingItemAdapter(getContext(), cursor);
        recyclerView.setAdapter(shoppingItemAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            int pos = savedInstanceState.getInt(SHOPPING_LIST_INDEX);
            if(pos != 0 && pos != RecyclerView.NO_POSITION)
                sposition = pos;
        }
        recyclerView.smoothScrollToPosition(sposition);
    }

    public void addToList() {
        String item = mItem.getText().toString().trim();
        if(item.length() > 0) {
            addItem(item);
            shoppingItemAdapter.swapCursor(getAllItems());
            mItem.clearFocus();
            mItem.getText().clear();
            updateWidgets(getContext());

        }
    }
    public Cursor getAllItems() {
        return getContext().getContentResolver().query(KitchenProvider.Shopping.CONTENT_URI, null, null, null, null);
    }
    public void addItem(String item){
        ContentValues values = new ContentValues();
        values.put(ShoppingContract.ITEM, item);
        getContext().getContentResolver().insert(KitchenProvider.Shopping.CONTENT_URI, values);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mLayoutManager!=null) {
            outState.putInt(SHOPPING_LIST_INDEX, mLayoutManager.findFirstCompletelyVisibleItemPosition());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
