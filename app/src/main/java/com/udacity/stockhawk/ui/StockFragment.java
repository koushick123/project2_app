package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Koushick on 24-02-2017.
 */
public class StockFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler{

    private static final int STOCK_LOADER = 0;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    RecyclerView stockRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView error;
    private StockAdapter adapter;
    BroadcastReceiver mReceiver;
    private final String LOG_TAG = StockFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock,container,false);
        adapter = new StockAdapter(getActivity().getApplicationContext(),this);
        stockRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        error = (TextView)rootView.findViewById(R.id.error);
        onRefresh();

        QuoteSyncJob.initialize(getActivity());
        getLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                        @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                        PrefUtils.removeStock(getActivity(), symbol);
                        getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                            ((Callback)getActivity()).onDelete();
                        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
                        getContext().sendBroadcast(dataUpdatedIntent);
                    }
            }).attachToRecyclerView(stockRecyclerView);
        return rootView;
    }

    public interface Callback
    {
        void onItemClick(String symbol, String history);
        void onDelete();
    }

    public void onSettingChange(){
        if(adapter != null && adapter.getItemCount() > 0) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(String symbol, String history) {
        Timber.d("Symbol clicked: %s", symbol);
        Timber.d("History of stock: %s", history);
        ((Callback)getActivity()).onItemClick(symbol,history);
    }

    @Override
    public void onResume() {

        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                QuoteSyncJob.ACTION_NO_DATA);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (QuoteSyncJob.ACTION_NO_DATA.equalsIgnoreCase(intent.getAction())) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    HashSet<String> errors = (HashSet<String>) prefs.getStringSet("errors", null);
                    if (errors != null && errors.size() > 0) {
                        ErrorMessageDialog dialog = new ErrorMessageDialog();
                        Bundle error = new Bundle();
                        String[] msgs = errors.toArray(new String[errors.size()]);
                        error.putString("noSymbol",msgs[0]);
                        dialog.setArguments(error);
                        if(swipeRefreshLayout.isRefreshing()){
                            Timber.d(LOG_TAG,"Stock Values Refreshing.....Need to stop");
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        dialog.show(getFragmentManager(), "ErrorFragment");
                    }
                }
            }
        };

        getActivity().registerReceiver(mReceiver,intentFilter);
        Timber.d(LOG_TAG,"onPostResume");
    }



    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(getActivity().getApplicationContext());

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(getActivity().getApplicationContext()).size() == 0) {
            Timber.d("WHYAREWEHERE");
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(getActivity(), symbol);
            QuoteSyncJob.syncImmediately(getActivity());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }
}
