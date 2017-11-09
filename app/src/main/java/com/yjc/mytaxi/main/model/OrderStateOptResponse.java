package com.yjc.mytaxi.main.model;

import com.yjc.mytaxi.common.http.biz.BaseBizResponse;

/**
 * Created by Administrator on 2017/11/9/009.
 * 订单操作状态
 */

public class OrderStateOptResponse extends BaseBizResponse{
    public static final int ORDER_STATE_CREATE=0;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
