package com.journaldev.maproutebetweenmarkers;


import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by 5022378 on 26-02-2018.
 */

public class BaseApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
