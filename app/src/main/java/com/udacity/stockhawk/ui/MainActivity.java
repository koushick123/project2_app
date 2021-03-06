package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements StockFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean tablet_mode = false;
    private String STOCK_DETAIL_TAG = "STOCK_DETAIL_FRAGMENT_TAG";

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d(LOG_TAG,"OnStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG,"inside Oncreate");
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG,"after setcontentview");

        if(findViewById(R.id.stockFragmentDetail) != null){

            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stockFragmentDetail, new StockFragmentDetail(), STOCK_DETAIL_TAG)
                        .commit();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stockFragment, new StockFragment(), null)
                        .commit();
            }
            tablet_mode=true;
        }
        else{
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stockFragment, new StockFragment(), null)
                        .commit();
            }
        }
    }

    public void button(View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
    StockFragment fragment = (StockFragment) getSupportFragmentManager().findFragmentById(R.id.stockFragment);
        Timber.d(LOG_TAG,"fragment == "+fragment);
        if(fragment != null){
            fragment.addStock(symbol);
        }
        else{
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stockFragment,new StockFragment(),null)
                    .commit();
            fragment = (StockFragment) getSupportFragmentManager().findFragmentById(R.id.stockFragment);
            fragment.addStock(symbol);
        }
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            //adapter.notifyDataSetChanged();
            StockFragment fragment = (StockFragment) getSupportFragmentManager().findFragmentById(R.id.stockFragment);
            Timber.d(LOG_TAG,"fragment for setting == "+fragment);
            if(fragment != null){
                fragment.onSettingChange();
            }
            else{
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.stockFragment,new StockFragment(),null)
                        .commit();
                fragment = (StockFragment) getSupportFragmentManager().findFragmentById(R.id.stockFragment);
                fragment.onSettingChange();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDelete()
    {
        if(tablet_mode) {
            Log.d(LOG_TAG, "inside OnDelete");
            StockFragmentDetail detail = (StockFragmentDetail) getSupportFragmentManager().findFragmentByTag(STOCK_DETAIL_TAG);
            Log.d(LOG_TAG, "Detail fragment == " + detail);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(detail);
            fragmentTransaction.add(R.id.stockFragmentDetail, new StockFragmentDetail(), STOCK_DETAIL_TAG)
                    .commit();
        }
    }

    @Override
    public void onItemClick(String symbol, String history)
    {
        if(tablet_mode){
            StockFragmentDetail stockFragmentDetail = new StockFragmentDetail();
            Bundle args = new Bundle();
            args.putString("symbol",symbol);
            args.putString("history",history);
            stockFragmentDetail.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stockFragmentDetail,stockFragmentDetail,STOCK_DETAIL_TAG)
                    .commit();
        }
        else{
            Intent intent = new Intent(this, StockDetailActivity.class);
            intent.putExtra("symbol",symbol);
            intent.putExtra("history",history);
            startActivity(intent);
        }
    }
}
