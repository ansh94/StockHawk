package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by ANSHDEEP on 12-04-2017.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //return remote view factory here
        return new StockWidgetRemoteViewsFactory(this,intent);
    }
}
