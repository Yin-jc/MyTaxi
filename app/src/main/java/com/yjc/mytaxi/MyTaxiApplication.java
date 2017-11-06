package com.yjc.mytaxi;

import android.app.Application;

/**
 * Created by Administrator on 2017/11/6/006.
 */

public class MyTaxiApplication extends Application {
    private static MyTaxiApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }
    public static MyTaxiApplication getInstance(){
        return instance;
    }
}
