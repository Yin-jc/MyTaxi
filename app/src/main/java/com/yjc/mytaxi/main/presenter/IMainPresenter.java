package com.yjc.mytaxi.main.presenter;

import com.yjc.mytaxi.common.lbs.LocationInfo;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface IMainPresenter {
    void loginByToken();

    /**
     * 获取附近司机
     * @param latitude
     * @param longtitude
     */
    void fetchNearDrivers(double latitude, double longtitude);

    /**
     * 上报当前位置
     * @param locationInfo
     */
    void updateLocationToServer(LocationInfo locationInfo);
}
