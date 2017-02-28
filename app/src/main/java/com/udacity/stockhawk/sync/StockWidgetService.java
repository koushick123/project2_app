package com.udacity.stockhawk.sync;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by koushick on 28-Feb-17.
 */
public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(this.getClass().getName(),"inside StockWidgetService...");
        StockWidgetDataProvider stockWidgetDataProvider = new StockWidgetDataProvider(getApplicationContext(),intent);
        return stockWidgetDataProvider;
    }
}
