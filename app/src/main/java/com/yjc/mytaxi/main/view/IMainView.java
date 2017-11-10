package com.yjc.mytaxi.main.view;

import com.yjc.mytaxi.account.view.IView;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.main.model.Order;

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
     * @param order
     */
    void showCallDriverSuc(Order order);

    /**
     * 显示呼叫司机失败
     */
    void showCallDriverFail();

    /**
     * 显示订单取消成功
     */
    void showCancelSuc();

    /**
     * 显示订单取消失败
     */
    void showCancelFail();

    /**
     * 显示司机接单
     * @param mCurrentOrder
     */
    void showDriverAcceptOrder(Order mCurrentOrder);

    /**
     * 显示司机到达上车地点
     * @param mCurrentOrder
     */
    void showDriverArriveStart(Order mCurrentOrder);

    /**
     * 显示开始行程
     */
    void showStartDrive(Order mCurrentOrder);

    /**
     * 显示到达终点
     * @param mCurrentOrder
     */
    void showArriveEnd(Order mCurrentOrder);

    /**
     * 更新司机到上车点的路径
     * @param locationInfo
     * @param order
     */
    void updateDriverToStartRoute(LocationInfo locationInfo, Order order);

    /**
     * 更新司机到终点的路径
     * @param locationInfo
     * @param order
     */
    void updateDriverToEndRoute(LocationInfo locationInfo, Order order);

    /**
     * 支付成功
     * @param order
     */
    void showPaySuc(Order order);

    /**
     * 支付失败
     */
    void showPayFail();
}
