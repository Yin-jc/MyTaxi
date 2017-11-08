package com.yjc.mytaxi.main.view;

import com.yjc.mytaxi.account.view.IView;
import com.yjc.mytaxi.common.lbs.LocationInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface IMainView extends IView{
    void showLoginSuc();

    void showNears(List<LocationInfo> data);
}
