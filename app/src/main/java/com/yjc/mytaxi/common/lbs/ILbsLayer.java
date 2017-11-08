package com.yjc.mytaxi.common.lbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

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
    // TODO: 2017/11/8/008 POI搜索
}
