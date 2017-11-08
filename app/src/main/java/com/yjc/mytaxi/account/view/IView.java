package com.yjc.mytaxi.account.view;

import com.yjc.mytaxi.common.lbs.LocationInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface IView {
    /**
     * 显示loading
     */
    void showLoading();

    /**
     * 显示错误
     * @param code
     * @param msg
     */
    void showError(int code,String msg);


}
