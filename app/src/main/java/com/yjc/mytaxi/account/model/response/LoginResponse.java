package com.yjc.mytaxi.account.model.response;

import com.yjc.mytaxi.account.model.bean.Account;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;

/**
 * Created by Administrator on 2017/11/6/006.
 * 登录响应
 */

public class LoginResponse extends BaseBizResponse{

    Account data;

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }
}
