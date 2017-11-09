package com.yjc.mytaxi.main.model;

import com.yjc.mytaxi.common.lbs.LocationInfo;

/**
 * Created by Administrator on 2017/11/8/008.
 */

public interface IMainManager {

    void fetchNearDrivers(double latitude, double longtitude);

    void updateLocationToServer(LocationInfo locationInfo);

    void callDriver(String pushKey, float cost, LocationInfo startLocation, LocationInfo endLocation);
}
