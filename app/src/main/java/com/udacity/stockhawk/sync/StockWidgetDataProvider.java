package com.udacity.stockhawk.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.StockWidgetItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koushick on 28-Feb-17.
 */
public class StockWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    List<StockWidgetItem> mCollections = new ArrayList<>();
    private static final String STOCK_ACTION = "STOCK_ACTION";
    private final String LOG_TAG = this.getClass().getName();
    Context mContext = null;

    public StockWidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCollections.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.stock_list_widget_item);
        Log.d(this.getClass().getName(),"Position  == "+position);
        remoteViews.setTextViewText(R.id.listSymbol,mCollections.get(position).getSymbol());
        remoteViews.setTextViewText(R.id.listPrice,mCollections.get(position).getPrice());
        Intent i = new Intent();
        Bundle extras = new Bundle();
        extras.putInt(STOCK_ACTION, position);
        i.putExtras(extras);
        remoteViews.setOnClickFillInIntent(R.id.stockWidgetListRoot,i);
        return remoteViews;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {

        refreshData();
    }

    @Override
    public void onDataSetChanged() {
        refreshData();
    }

    @Override
    public void onDestroy() {
    mCollections.clear();
    }

    private void refreshData()
    {
        final long identityToken = Binder.clearCallingIdentity();
        Cursor stockVals = null;
        try {
            stockVals = mContext.getContentResolver().query(Uri.parse(Contract.BASE_URI + "/quote"), new String[]{Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_PRICE}, null, null, null);
            Log.d(LOG_TAG, "Cursor count value == " + stockVals.getCount());
            if (stockVals.getCount() > 0) {
                if (stockVals.moveToFirst()) {
                    mCollections.clear();
                    Log.d(LOG_TAG, "Cleared previous data");
                    do {
                        StockWidgetItem stockWidgetItem = new StockWidgetItem();
                        stockWidgetItem.setPrice(stockVals.getFloat(stockVals.getColumnIndex(Contract.Quote.COLUMN_PRICE)) + "");
                        stockWidgetItem.setSymbol(stockVals.getString(stockVals.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                        mCollections.add(stockWidgetItem);
                    } while (stockVals.moveToNext());
                } else {
                    Log.d(LOG_TAG, "Unable to read from cursor");
                }
            }
        }
        finally {
            if(stockVals != null){
                stockVals.close();
            }
            Binder.restoreCallingIdentity(identityToken);
        }
    }
}
