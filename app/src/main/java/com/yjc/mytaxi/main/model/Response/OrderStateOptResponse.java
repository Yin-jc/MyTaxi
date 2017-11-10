package com.yjc.mytaxi.main.model.Response;

import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.main.model.bean.Order;

/**
 * Created by Administrator on 2017/11/9/009.
 * 订单操作状态
 */

public class OrderStateOptResponse extends BaseBizResponse{
    //创建订单
    public static final int ORDER_STATE_CREATE= 0;
    //取消订单
    public static final int ORDER_STATE_CANCEL= -1;
    //司机接单
    public static final int ORDER_STATE_ACCEPT = 1;
    //司机到达上车点
    public static final int ORDER_STATE_ARRIVE_START = 2;
    //开始行程
    public static final int ORDER_STATE_START_DRIVE = 3;
    //到达终点
    public static final int ORDER_STATE_ARRIVE_END = 4;
    //支付
    public static final int PAY = 5;

    private int state;
    //携带操作订单
    private Order data;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Order getData() {
        return data;
    }

    public void setData(Order data) {
        this.data = data;
    }
}
