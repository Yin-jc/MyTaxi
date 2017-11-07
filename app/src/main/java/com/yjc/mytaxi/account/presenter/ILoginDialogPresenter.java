package com.yjc.mytaxi.account.presenter;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface ILoginDialogPresenter {
    /**
     * 登录
     * @param phone
     * @param password
     */
    void requestLogin(String phone,String password);
}
