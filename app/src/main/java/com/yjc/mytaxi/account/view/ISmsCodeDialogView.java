package com.yjc.mytaxi.account.view;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public interface ISmsCodeDialogView extends IView{

    /**
     * 显示倒计时
     */
    void showCountDownTimer();

    /**
     * 显示错误区
     * @param code
     * @param msg
     */
    void showError(int code,String msg);


    /**
     * 显示验证状态
     * @param b
     */
    void showSmsCodeCheckState(boolean b);

    /**
     * 显示用户存在状态
     * @param b
     */
    void showUserExist(boolean b);
}
