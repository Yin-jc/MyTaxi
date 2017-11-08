package com.yjc.mytaxi.common.lbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import java.util.List;

/**
 * Created by Administrator on 2017/11/8/008.
 * 定义地图服务通用抽象接口
 */

public interface ILbsLayer {

    /**
     * 获取地图
     * @return
     */
    View getMapView();

    /**
     * 设置位置变化监听
     * @param locationChangeListener
     */
    void setLocationChangeListener(CommonLocationChangeListener locationChangeListener);

    /**
     * 设置定位图标
     * @param res
     */
    void setLocationRes(int res);

    /**
     * 添加，更新标记点，包括位置角度（通过id识别）
     * @param locationInfo
     * @param bitmap
     */
    void addOrUpdateMarker(LocationInfo locationInfo, Bitmap bitmap);

    /**
     * 获取当前城市
     */
    String getCity();

    /**
     * 联动搜索附近的位置
     * @param key
     * @param listener
     */
    void poiSearch(String key,OnSearchedListener listener);

    /**
     * 生命周期函数
     */
    void onCreate(Bundle state);
    void onResume();
    void onSaveInstanceState(Bundle outState);
    void onPause();
    void onDestroy();
 
    interface CommonLocationChangeListener{
        void onLocationChange(LocationInfo locationInfo);
        //第一次定位回调
        void onLocation(LocationInfo locationInfo);
    }

    // TODO: 2017/11/8/008 路径绘制

    //  POI搜索结果监听器
    interface OnSearchedListener{
        void onSearched(List<LocationInfo> results);
        void onError(int rCode);
    }
}
