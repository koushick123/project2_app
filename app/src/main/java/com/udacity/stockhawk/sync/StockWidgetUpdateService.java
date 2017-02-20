package com.udacity.stockhawk.sync;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.widget.StockWidgetProvider;

/**
 * Created by Koushick on 18-02-2017.
 */
public class StockWidgetUpdateService extends IntentService {

    public StockWidgetUpdateService(){
        super("StockWidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StockWidgetProvider.class));

        for(int appWidget : appWidgetIds)
        {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidget);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.stock_widget_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_stock;
            } else {
                layoutId = R.layout.widget_stock_small;
            }

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            //Add data to the remote widget view
            views.setImageViewResource(R.id.currencyWidget,R.drawable.ic_dollar);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, "Stock");
            }

            Cursor stockVals = getContentResolver().query(Uri.parse(Contract.BASE_URI+"/quote/AAPL"),new String[]{Contract.Quote.COLUMN_PRICE},null,null,null);
            Log.d(this.getClass().getName(),"stock widget service cursor count value == "+stockVals.getCount());
            float updatedPrice = 0.0f;
            if(stockVals.getCount() != 0){
                stockVals.moveToFirst();
                updatedPrice = stockVals.getFloat(stockVals.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            }
            Log.d(this.getClass().getName(),"Stock price === "+updatedPrice);
            stockVals.close();
            views.setTextViewText(R.id.stockWidget,updatedPrice+"");

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.stockHawkWidget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidget, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.stockWidget, description);
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager,int appWidgetId){
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widgetWidth);
        }

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
