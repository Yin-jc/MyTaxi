package com.yjc.mytaxi.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.view.ICreatePasswordDialogView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class CreatePasswordDialogPresenterImpl implements ICreatePasswordDialogPresenter{
    private ICreatePasswordDialogView view;
    private IAccountManager accountManager;

    public CreatePasswordDialogPresenterImpl(ICreatePasswordDialogView view,
                                             IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
        accountManager.setHandler(new MyHandler(this));
    }

    private static class MyHandler extends Handler {
        WeakReference<CreatePasswordDialogPresenterImpl> refContext;

        public MyHandler(CreatePasswordDialogPresenterImpl context) {
            refContext=new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatePasswordDialogPresenterImpl presenter=refContext.get();
            switch (msg.what){
                case IAccountManager.REGISTER_SUC:
                    presenter.view.showRegisterSuc();
                    break;
                case IAccountManager.LOGIN_SUC:
                    presenter.view.showLoginSuc();
                    break;
                case IAccountManager.SERVER_FAIL:
                    presenter.view.showError(IAccountManager.SERVER_FAIL,"");
                    break;
            }
        }
    }

//    @Override
//    public void checkPw(String pw, String pwl) {
//
//    }

    @Override
    public void requestRegister(String phone, String pw) {
        accountManager.register(phone,pw);
    }

    @Override
    public void requestLogin(String phone, String pw) {
        accountManager.login(phone,pw);
    }
}
