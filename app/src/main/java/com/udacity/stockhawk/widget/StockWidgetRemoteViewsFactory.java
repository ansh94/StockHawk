package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by ANSHDEEP on 12-04-2017.
 */

public class StockWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;
    private Cursor data;


    final private DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    final private DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    final private DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    public StockWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // Nothing to do
        Timber.d(" StockWidgetRemoteViewsService on create method called!");
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }

        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission

        final long identityToken = Binder.clearCallingIdentity();

        data = mContext.getContentResolver().query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                null,
                null);

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_detail_list_item);


        String stockName = data.getString(Contract.Quote.POSITION_NAME);
        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        String price = dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));
        String history = data.getString(Contract.Quote.POSITION_HISTORY);
        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            views.setInt(R.id.widgetAbsolut, "setBackgroundResource", R.drawable.percent_change_pill_green);
            views.setInt(R.id.widgetChange, "setBackgroundResource", R.drawable.percent_change_pill_green);

        } else {
            views.setInt(R.id.widgetAbsolut, "setBackgroundResource", R.drawable.percent_change_pill_red);
            views.setInt(R.id.widgetChange, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }


        views.setTextViewText(R.id.widgetSymbol, symbol);
        views.setTextViewText(R.id.widgetPrice, price);


        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);


        views.setTextViewText(R.id.widgetAbsolut, change);

        views.setTextViewText(R.id.widgetChange, percentage);


        final Intent fillInIntent = new Intent();

        Bundle extras = new Bundle();
        extras.putString("SYMBOL", symbol);
        extras.putString("HISTORY", history);
        extras.putString("NAME", stockName);
        fillInIntent.putExtras(extras);

        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;

    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position))
            return data.getLong(Contract.Quote.POSITION_ID);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


}
