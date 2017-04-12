package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";

    //period that the job will run
    private static final int PERIOD = 300000;


    private static final int INITIAL_BACKOFF = 10000;

    //identifier of the job that you will run
    private static final int PERIODIC_ID = 1;

    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }


            //Sends a basic quotes request to Yahoo Finance.
            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();

                Stock stock = quotes.get(symbol);

                Timber.d("Stock = " + stock);

                StockQuote quote = stock.getQuote();
                Timber.d("Stock quote = " + quote);

                if (stock.isValid()) {

                    String name = stock.getName();
                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();

                    try{
                        // WARNING! Don't request historical data for a stock that doesn't exist!
                        // The request will hang forever X_x


                        /*
                        Requests the historical quotes for this stock with the following characteristics.
                        from: start date of the historical data
                        to: end date of the historical data
                        interval: the interval of the historical data
                         */
                        List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                        StringBuilder historyBuilder = new StringBuilder();

                        for (HistoricalQuote it : history) {
                            historyBuilder.append(it.getDate().getTimeInMillis());
                            historyBuilder.append(", ");
                            historyBuilder.append(it.getClose());
                            historyBuilder.append("\n");
                        }

                        //Timber.d("Historical quote = "+historyBuilder.toString());


                        ContentValues quoteCV = new ContentValues();
                        quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                        quoteCV.put(Contract.Quote.COLUMN_NAME, name);
                        quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                        quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                        quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                        quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                        quoteCVs.add(quoteCV);

                    } catch (FileNotFoundException e){
                        Timber.d("Invalid stock with no stock history : " + stock.getSymbol() + " removed");
                        PrefUtils.removeStock(context, stock.getSymbol());
                        sendBroadcast(context, stock.getSymbol());
                    }


                } else {
                    Timber.d("Invalid stock : " + stock.getSymbol() + " removed");
                    PrefUtils.removeStock(context, stock.getSymbol());
                    sendBroadcast(context, stock.getSymbol());
                }

            }


            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    // Send an Intent with an action named "MY_CUSTOM_EVENT". The Intent sent should
    // be received by the ReceiverActivity.
    private static void sendBroadcast(Context context, String symbol) {
        Timber.d("Broadcasting unknown stock symbol");
        Intent intent = new Intent("MY_CUSTOM_EVENT");
        intent.putExtra("UNKNOWN_STOCK", symbol);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");

        //use the JobInfo.Builder to construct a JobInfo object that gets passed to your service.
        //To create a JobInfo object, JobInfo.Builder accepts two parameters. The first is the identifier
        //of the job that you will run and the second is the component name of the service that you will use with the JobScheduler API.
        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        //Creating a job scheduler object
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }


}
