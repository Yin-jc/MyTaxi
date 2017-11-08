package com.yjc.mytaxi.main.presenter;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface IMainPresenter {
    void loginByToken();

    void fetchNearDrivers(double latitude, double longtitude);
}
