package com.udacity.stockhawk.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by ANSHDEEP on 11-04-2017.
 */

public class XAxisValueFormatter implements IAxisValueFormatter {

    private Calendar[] mValues;

    public XAxisValueFormatter(Calendar[] values) {
        this.mValues = values;
        for(Calendar c: values){
            Timber.d(c.toString());
        }
        Timber.d("Hello  " +mValues[2].toString());
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        Calendar date = mValues[(int) value];
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        String formattedXaxisValue = String.valueOf(dayOfMonth)+ getFormattedDay(dayOfMonth) + " " +date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
        Timber.d(value + " : "+formattedXaxisValue);
        return formattedXaxisValue;
    }

    public String getFormattedDay(int dayOfMonth){
        String appendix = "th";

        switch (dayOfMonth) {
            case 1:
                appendix = "st";
                break;
            case 2:
                appendix = "nd";
                break;
            case 3:
                appendix = "rd";
                break;
            case 21:
                appendix = "st";
                break;
            case 22:
                appendix = "nd";
                break;
            case 23:
                appendix = "rd";
                break;
            case 31:
                appendix = "st";
                break;
        }

        return appendix;
    }
}
