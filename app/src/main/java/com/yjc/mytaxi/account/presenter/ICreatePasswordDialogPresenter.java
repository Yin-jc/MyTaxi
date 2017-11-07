package com.yjc.mytaxi.account.presenter;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface ICreatePasswordDialogPresenter {
    /**
     * 校验密码输入合法性
     * @param pw
     * @param pwl
     */
//    void checkPw(String pw,String pwl);

    /**
     * 提交注册
     * @param phone
     * @param pw
     */
    void requestRegister(String phone,String pw);

    /**
     * 登录
     * @param phone
     * @param pw
     */
    void requestLogin(String phone,String pw);
}
