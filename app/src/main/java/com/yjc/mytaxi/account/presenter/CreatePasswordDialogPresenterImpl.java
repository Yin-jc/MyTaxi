package com.yjc.mytaxi.account.presenter;

import android.os.Handler;
import android.os.Message;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.model.LoginResponse;
import com.yjc.mytaxi.account.view.ICreatePasswordDialogView;
import com.yjc.mytaxi.common.dataBus.RegisterBus;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;


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
    }


    @RegisterBus
    public void onCreatePasswordResponse(BaseBizResponse response){
        switch (response.getCode()){
            case IAccountManager.REGISTER_SUC:
                view.showRegisterSuc();
                break;
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(IAccountManager.SERVER_FAIL,"");
                break;
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

//    /**
//     * 接收子线程消息的 Handler
//     */
//    static class MyHandler extends Handler {
//        // 软引用
//        WeakReference<CreatePasswordDialogPresenterImpl> codeDialogRef;
//        public MyHandler(CreatePasswordDialogPresenterImpl presenter) {
//            codeDialogRef =
//                    new WeakReference<CreatePasswordDialogPresenterImpl>(presenter);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            CreatePasswordDialogPresenterImpl presenter = codeDialogRef.get();
//            if (presenter == null) {
//                return;
//            }
//            // 处理UI 变化
//            switch (msg.what) {
//                case IAccountManager.REGISTER_SUC:
//                    // 注册成功
//                    presenter.view.showRegisterSuc();
//                    break;
//                case IAccountManager.LOGIN_SUC:
//                    // 登录成功
//                    presenter.view.showLoginSuc();
//                    break;
//                case IAccountManager.SERVER_FAIL:
//                    // 服务器错误
//                    presenter.view.showError(IAccountManager.SERVER_FAIL, "");
//                    break;
//            }
//        }
//    }

}
