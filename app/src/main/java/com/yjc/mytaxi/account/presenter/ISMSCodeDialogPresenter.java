package com.yjc.mytaxi.account.presenter;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface ISMSCodeDialogPresenter {
    /**
     * 请求下发验证码
     * @param phone
     */
    void requestSendSmsCode(String phone);

    /**
     * 请求校验验证码
     * @param phone
     * @param smsCode
     */
    void requestCheckSmsCode(String phone,String smsCode);

    /**
     * 请求校验用户是否存在
     * @param phone
     */
    void requestCheckUserExist(String phone);
}
