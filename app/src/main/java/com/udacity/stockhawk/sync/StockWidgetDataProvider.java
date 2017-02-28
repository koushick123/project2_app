package com.udacity.stockhawk.sync;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
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
        Cursor stockVals = mContext.getContentResolver().query(Uri.parse(Contract.BASE_URI+"/quote"),new String[]{Contract.Quote.COLUMN_SYMBOL,Contract.Quote.COLUMN_PRICE},null,null,null);
        Log.d(this.getClass().getName(),"Cursor count value == "+stockVals.getCount());
        if(stockVals.getCount() > 0)
        {
            try {
                if(stockVals.moveToFirst()) {
                    do{
                        StockWidgetItem stockWidgetItem = new StockWidgetItem();
                        stockWidgetItem.setPrice(stockVals.getFloat(stockVals.getColumnIndex(Contract.Quote.COLUMN_PRICE)) + "");
                        stockWidgetItem.setSymbol(stockVals.getString(stockVals.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                        mCollections.add(stockWidgetItem);
                    }while (stockVals.moveToNext());
                }else{
                    Log.d(this.getClass().getName(),"Unable to read from cursor");
                }
            }
            finally{
                stockVals.close();
            }
        }
    }
}
