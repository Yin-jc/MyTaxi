package com.yjc.mytaxi.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.model.LoginResponse;
import com.yjc.mytaxi.account.view.ISmsCodeDialogView;
import com.yjc.mytaxi.common.dataBus.RegisterBus;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class SmsCodeDialogPresenterImpl implements ISMSCodeDialogPresenter{

    private ISmsCodeDialogView view;
    private IAccountManager accountManager;


    @RegisterBus
    public void onSmsCodeResponse(BaseBizResponse response){
        switch (response.getCode()){
            case IAccountManager.SMS_SEND_SUC:
                view.showCountDownTimer();
                break;
            case IAccountManager.SMS_SEND_FAIL:
                view.showError(IAccountManager.SMS_SEND_FAIL,"");
                break;
            case IAccountManager.SMS_CHECK_SUC:
                view.showSmsCodeCheckState(true);
                break;
            case IAccountManager.SMS_CHECK_FAIL:
                view.showError(IAccountManager.SMS_CHECK_FAIL,"");
                break;
            case IAccountManager.USER_EXIST:
                view.showUserExist(true);
                break;
            case IAccountManager.USER_NOT_EXIST:
                view.showUserExist(false);
                break;
        }
    }

    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view,
                                      IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    /**
     * 获取验证码
     * @param phone
     */
    @Override
    public void requestSendSmsCode(String phone) {
        accountManager.fetchSMSCode(phone);
    }

    /**
     * 校验验证码
     * @param phone
     * @param smsCode
     */
    @Override
    public void requestCheckSmsCode(String phone, String smsCode) {
        accountManager.checkSMSCode(phone,smsCode);
    }

    /**
     * 检查用户是否存在
     * @param phone
     */
    @Override
    public void requestCheckUserExist(String phone) {
        accountManager.checkUserExist(phone);
    }
}
