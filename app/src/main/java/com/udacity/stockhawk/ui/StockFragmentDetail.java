package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import timber.log.Timber;

/**
 * Created by Koushick on 24-02-2017.
 */
public class StockFragmentDetail extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_detail,container,false);

        TextView textView = (TextView)rootView.findViewById(R.id.noStockText);
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.stockChart);

        if(getArguments() != null){
            if(textView != null) {
                textView.setVisibility(View.INVISIBLE);
            }
            linearLayout.setVisibility(View.VISIBLE);
            String symbol = getArguments().getString("symbol");
            String history = getArguments().getString("history");
            Timber.d("symbol == " + symbol);
            Timber.d("history == " + history);

            List<Entry> xAxisTime = new ArrayList<Entry>();
            StringTokenizer token = new StringTokenizer(history, "\n");
            float index = 0;
            ArrayList<String> dates = new ArrayList<String>();
            ArrayList<String> closeValues = new ArrayList<String>();
            while (token.hasMoreTokens()) {
                StringTokenizer stock = new StringTokenizer(token.nextToken(), ",");
                Calendar endDate = Calendar.getInstance();
                String stockDates = stock.nextToken();
                String closeValue = stock.nextToken();
                endDate.setTimeInMillis(new Long(stockDates).longValue());
                String month = "";
                switch (endDate.get(Calendar.MONTH)) {
                    case 0:
                        month = "JAN";
                        break;
                    case 1:
                        month = "FEB";
                        break;
                    case 2:
                        month = "MAR";
                        break;
                    case 3:
                        month = "APR";
                        break;
                    case 4:
                        month = "MAY";
                        break;
                    case 5:
                        month = "JUN";
                        break;
                    case 6:
                        month = "JUL";
                        break;
                    case 7:
                        month = "AUG";
                        break;
                    case 8:
                        month = "SEP";
                        break;
                    case 9:
                        month = "OCT";
                        break;
                    case 10:
                        month = "NOV";
                        break;
                    case 11:
                        month = "DEC";
                        break;
                }
                dates.add(endDate.get(Calendar.DATE) + "-" + month);
                Timber.d("Stock history == " + endDate.get(Calendar.DATE) + ", " + endDate.get(Calendar.MONTH) + ", " + endDate.get(Calendar.YEAR));
                Timber.d("Stock history close value== " + closeValue);
                closeValues.add(new String(closeValue));
                Timber.d("Time in millis == " + endDate.getTimeInMillis() + ", " + (float) endDate.getTimeInMillis());
                xAxisTime.add(new Entry(index++, Float.parseFloat(closeValue)));
            }

            // programmatically create a LineChart
            LineChart chart = (LineChart) rootView.findViewById(R.id.chart);

            TextView chartHeader = (TextView) rootView.findViewById(R.id.headingText);
            chartHeader.setText(chartHeader.getText() + " " + symbol);

            LineDataSet setComp1 = new LineDataSet(xAxisTime, symbol);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setFillColor(getResources().getColor(R.color.material_green_700));
            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp1);
            XAxis xAxis = chart.getXAxis();
            xAxis.setAxisLineColor(getResources().getColor(R.color.material_red_700));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisLineWidth(2f);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawLabels(true);
            xAxis.setDrawGridLines(true);
            xAxis.setGranularity(1f);

            YAxis left = chart.getAxisLeft();
            left.setDrawAxisLine(true);
            left.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            left.setAxisLineColor(getResources().getColor(R.color.material_red_700));
            left.setDrawLabels(true);
            left.setAxisLineWidth(2f);
            left.setDrawGridLines(true); // no grid lines
            left.setDrawZeroLine(true); // draw a zero line
            chart.getAxisRight().setEnabled(false); // no right axis

            final String[] closeDates = dates.toArray(new String[dates.size()]);
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    Timber.d("Index dates == " + value);
                    return closeDates[(int) value];
                }
            });
            LineData data = new LineData(dataSets);
            chart.setData(data);
            chart.invalidate(); // refresh*/
        }
        else{
            if(textView != null) {
                textView.setVisibility(View.VISIBLE);
            }
            linearLayout.setVisibility(View.GONE);
        }
        return rootView;
    }
}
