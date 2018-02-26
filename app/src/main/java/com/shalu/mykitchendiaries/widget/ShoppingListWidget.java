package com.shalu.mykitchendiaries.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.shalu.mykitchendiaries.R;
import com.shalu.mykitchendiaries.provider.KitchenProvider;
import com.shalu.mykitchendiaries.provider.ShoppingContract;
import com.shalu.mykitchendiaries.ui.ShoppingActivity;

/**
 * Implementation of App Widget functionality.
 */
public class ShoppingListWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String widgetText = context.getString(R.string.appwidget_text);
        Cursor c = context.getContentResolver().query(KitchenProvider.Shopping.CONTENT_URI, null, null, null, null);
        int i = 1;
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext(),++i) {
            widgetText+="\n"+i+". "+c.getString(c.getColumnIndex(ShoppingContract.ITEM));
        }
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_list_widget);
        views.setTextViewText(R.id.list, widgetText);
        Intent shoppingListIntent = new Intent(context, ShoppingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, shoppingListIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.list, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

