package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.sync.StockWidgetUpdateService;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Koushick on 18-02-2017.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int appWidget : appWidgetIds)
        {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stock_small);

            //Add data to the remote widget view
            views.setImageViewResource(R.id.currencyWidget,R.drawable.ic_dollar);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, "Stock");
            }

            Cursor stockVals = context.getContentResolver().query(Uri.parse(Contract.BASE_URI+"/quote/AAPL"),new String[]{Contract.Quote.COLUMN_PRICE},null,null,null);
            Log.d(this.getClass().getName(),"Cursor count value == "+stockVals.getCount());
            float updatedPrice = 0.0f;
            if(stockVals.getCount() != 0){
                stockVals.moveToFirst();
                updatedPrice = stockVals.getFloat(stockVals.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            }
            stockVals.close();
            views.setTextViewText(R.id.stockWidget,updatedPrice+"");
            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.stockHawkWidget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidget, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.symbolWidget, description);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.d(this.getClass().getName(),"in onReceive === "+intent.getAction());
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equalsIgnoreCase(intent.getAction())) {
            context.startService(new Intent(context,StockWidgetUpdateService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context,StockWidgetUpdateService.class));
    }
}
