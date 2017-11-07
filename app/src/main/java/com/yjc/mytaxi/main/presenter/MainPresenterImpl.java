package com.yjc.mytaxi.main.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.model.LoginResponse;
import com.yjc.mytaxi.account.presenter.CreatePasswordDialogPresenterImpl;
import com.yjc.mytaxi.common.dataBus.RegisterBus;
import com.yjc.mytaxi.main.view.IMainView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class MainPresenterImpl implements IMainPresenter {

    private IMainView view;
    private IAccountManager accountManager;

    public MainPresenterImpl(IMainView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse response){
        switch (response.getCode()){
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.TOKEN_INVALID:
                view.showError(IAccountManager.TOKEN_INVALID,"");
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(IAccountManager.SERVER_FAIL,"");
                break;
        }
    }
    @Override
    public void loginByToken() {
        accountManager.loginByToken();
    }
}
