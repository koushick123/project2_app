package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.sync.StockWidgetService;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Koushick on 18-02-2017.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = StockWidgetProvider.class.getName();
    private static final String STOCK_ACTION = "STOCK_ACTION";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG,"inside OnUpdate");
        for(int appWidget : appWidgetIds)
        {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, "Stock");
            }
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidget);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(appWidget, R.id.stockWidgetList, intent);

            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent clickPI = PendingIntent.getActivity(context, 0,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.stockWidgetList, clickPI);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidget, views);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.stockWidgetList, description);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.d(this.getClass().getName(),"in onReceive === "+intent.getAction());
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equalsIgnoreCase(intent.getAction()))
        {
            //context.startService(new Intent(context,StockWidgetUpdateService.class));
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
            for(int appWidget : appWidgetIds)
            {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);

                // Content Descriptions for RemoteViews were only added in ICS MR1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, "Stock");
                }

                // Tell the AppWidgetManager to perform an update on the current app widget
                Log.d(LOG_TAG,"Update widget manager == "+appWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidget,R.id.stockWidgetList);
            }
        }
        super.onReceive(context, intent);
    }

    /*@Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context,StockWidgetUpdateService.class));
    }*/
}
