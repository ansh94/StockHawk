package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.charts.XAxisValueFormatter;
import com.udacity.stockhawk.charts.YAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.R.id.chart;


public class DetailActivity extends AppCompatActivity {

    private String symbol;
    private String history;
    private String name;

    @BindView(chart)
    BarChart mChart;

    private List<BarEntry> historicalQuoteEntries;

    Calendar[] datesArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);




        Bundle extras = getIntent().getExtras();
        symbol = extras.getString("SYMBOL");
        history = extras.getString("HISTORY");
        name = extras.getString("NAME");

        setTitle(name); //title of the action bar


        extractHistoricalQuotes();
        displayChart();
    }

    protected void extractHistoricalQuotes(){
        Float close;
        historicalQuoteEntries = new ArrayList<>();

        List<String> historyQuotesList = Arrays.asList(history.split("\n"));

        int totalQuotes = historyQuotesList.size();
        datesArray = new Calendar[totalQuotes];

        int i = totalQuotes-1;
        for(String quote : historyQuotesList){
            String[] quoteItems = quote.split(",");

            datesArray[i] = Calendar.getInstance();
            datesArray[i].setTimeInMillis(Long.valueOf(quoteItems[0]));

            close = Float.valueOf(quoteItems[1]);

            historicalQuoteEntries.add(new BarEntry(i, close));


            //Timber.d("i : "+i +"   Date: "+datesArray[i].toString());
            i--;

        }

        //Timber.d(historicalQuoteEntries.toString());
        //Timber.d(datesArray.toString());

    }


    protected  void displayChart(){
        BarDataSet set = new BarDataSet(historicalQuoteEntries, "Stock Closing Value");
        set.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new XAxisValueFormatter(datesArray));
        xAxis.setGranularity(1f);

        YAxis left = mChart.getAxisLeft();
        left.setValueFormatter(new YAxisValueFormatter());

        YAxis right = mChart.getAxisRight();
        right.setValueFormatter(new YAxisValueFormatter());


        mChart.setData(data);
        mChart.setFitBars(true); // make the x-axis fit exactly all bars
//        mChart.invalidate(); // refresh
        mChart.animateXY(1000,1000);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
