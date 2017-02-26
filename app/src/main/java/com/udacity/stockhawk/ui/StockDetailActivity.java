package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.udacity.stockhawk.R;

public class StockDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        Log.d(StockDetailActivity.class.getName(),"SAVED INSTANCE STATE == "+getIntent().getStringExtra("symbol"));
        //getFragmentManager().findFragmentById(R.id.movie_detail_container);
        if(savedInstanceState == null)
        {
            StockFragmentDetail stockDetailFragment = new StockFragmentDetail();
            Bundle stock = new Bundle();
            stock.putString("symbol",getIntent().getStringExtra("symbol"));
            stock.putString("history",getIntent().getStringExtra("history"));
            stockDetailFragment.setArguments(stock);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stockDetails, stockDetailFragment, null)
                    .commit();
        }
    }
}
