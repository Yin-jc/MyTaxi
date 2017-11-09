package com.yjc.mytaxi.main.view;

import com.yjc.mytaxi.account.view.IView;
import com.yjc.mytaxi.common.lbs.LocationInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface IMainView extends IView{
    void showLoginSuc();

    /**
     * 显示附近司机
     * @param data
     */
    void showNears(List<LocationInfo> data);

    /**
     * 显示位置变化
     * @param locationInfo
     */
    void showLocationChange(LocationInfo locationInfo);

    /**
     * 显示呼叫司机成功
     */
    void showCallDriverSuc();

    /**
     * 显示呼叫司机失败
     */
    void showCallDriverFail();
}
