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
import android.widget.RemoteViews;
import android.widget.TextView;

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
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_stock_small);

            //Add data to the remote widget view
            views.setImageViewResource(R.id.currencyWidget,R.drawable.ic_dollar);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, "Stock");
            }

            Cursor stockVals = getContentResolver().query(Uri.parse(Contract.BASE_URI+"/quote/APPL"),null,null,null,null);
            float updatedPrice = 0.0f;
            if(stockVals.getCount() != 0){
                stockVals.moveToFirst();
                updatedPrice = stockVals.getFloat(stockVals.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            }
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
}
