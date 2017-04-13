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

    //Bar Entry class represents a single entry in the bar chart with x- and y-coordinate
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

    protected void extractHistoricalQuotes() {
        Float close;
        historicalQuoteEntries = new ArrayList<>();

        //Our history data in the form of a list
        List<String> historyQuotesList = Arrays.asList(history.split("\n"));

        int totalQuotes = historyQuotesList.size();
        datesArray = new Calendar[totalQuotes];

        int i = totalQuotes - 1;
        for (String quote : historyQuotesList) {
            String[] quoteItems = quote.split(",");

            datesArray[i] = Calendar.getInstance();
            datesArray[i].setTimeInMillis(Long.valueOf(quoteItems[0]));

            close = Float.valueOf(quoteItems[1]);

            // turning our data into BarEntry objects
            historicalQuoteEntries.add(new BarEntry(i, close));

            i--;

        }


    }


    protected void displayChart() {
        // BarDataSet objects hold data which belongs together, and allow individual styling of that data.

        // adding entries to data set
        BarDataSet set = new BarDataSet(historicalQuoteEntries, "Stock Closing Value");

        // setting the color
        set.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(set);

        // setting the bar width
        data.setBarWidth(0.9f);

        // The XAxis class is the data and information container for everything related to the the horizontal axis.
        XAxis xAxis = mChart.getXAxis();

        // set a custom value formatter
        xAxis.setValueFormatter(new XAxisValueFormatter(datesArray));
        xAxis.setGranularity(1f);

        YAxis left = mChart.getAxisLeft();
        left.setValueFormatter(new YAxisValueFormatter());

        YAxis right = mChart.getAxisRight();
        right.setValueFormatter(new YAxisValueFormatter());


        mChart.setData(data);


        // make the x-axis fit exactly all bars
        mChart.setFitBars(true);

        // animate x and y axes
        mChart.animateXY(1000, 1000);

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
