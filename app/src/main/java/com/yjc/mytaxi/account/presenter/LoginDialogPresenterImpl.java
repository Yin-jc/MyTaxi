package com.yjc.mytaxi.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.view.ILoginView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class LoginDialogPresenterImpl implements ILoginDialogPresenter{
    private ILoginView view;
    private IAccountManager accountManager;

    public LoginDialogPresenterImpl(ILoginView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
        accountManager.setHandler(new MyHandler(this));
    }

    private static class MyHandler extends Handler {
        WeakReference<LoginDialogPresenterImpl> refContext;

        public MyHandler(LoginDialogPresenterImpl context) {
            refContext=new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialogPresenterImpl presenter=refContext.get();
            switch (msg.what){
                case IAccountManager.LOGIN_SUC:
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.PW_ERROR:
                    presenter.view.showError(IAccountManager.PW_ERROR,"");
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL,"");
                    break;
            }
        }
    }

    @Override
    public void requestLogin(String phone, String password) {
        accountManager.login(phone,password);
    }
}
