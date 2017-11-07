package com.yjc.mytaxi.main.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.presenter.CreatePasswordDialogPresenterImpl;
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
        accountManager.setHandler(new MyHandler(this));
    }

    private static class MyHandler extends Handler {
        WeakReference<MainPresenterImpl> refContext;

        public MyHandler(MainPresenterImpl context) {
            refContext=new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            MainPresenterImpl presenter=refContext.get();
            switch (msg.what){
                case IAccountManager.LOGIN_SUC:
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.TOKEN_INVALID:
                    presenter.view.showError(IAccountManager.TOKEN_INVALID,"");
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL,"");
                    break;
            }
        }
    }
    @Override
    public void loginByToken() {
        accountManager.loginByToken();
    }
}
