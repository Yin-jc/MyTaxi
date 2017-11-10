package com.yjc.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.presenter.ILoginDialogPresenter;
import com.yjc.mytaxi.account.presenter.LoginDialogPresenterImpl;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.ToastUtil;

/**
 * Created by Administrator on 2017/11/7/007.
 * 登录框
 */

public class LoginDialog extends Dialog implements ILoginView{

    private static final String TAG ="LoginDialog" ;
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private ILoginDialogPresenter mPresenter;

    public LoginDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        mPhoneStr=phone;
        IHttpClient httpClient=new OkHttpClientImpl();
        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        IAccountManager manager=new AccountManagerImpl(httpClient,dao);
        mPresenter=new LoginDialogPresenterImpl(this,manager);
    }

    public LoginDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected LoginDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater= (LayoutInflater) getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root=inflater.inflate(R.layout.dialog_login_input,null);
        setContentView(root);
        initViews();

        //注册 Presenter
        RxBus.getInstance().register(mPresenter);
    }

    @Override
    public void showLoading() {
        showOrHideLoading(true);
    }

    @Override
    public void showError(int code, String msg) {
        switch (code){
            case IAccountManager.PW_ERROR:
                showPasswordError();
                break;
            case IAccountManager.SERVER_FAIL:
                showServerError();
                break;
        }
    }


    private void initViews() {
        mPhone=findViewById(R.id.phone);
        mPw=findViewById(R.id.password);
        mBtnConfirm=findViewById(R.id.btn_confirm);
        mLoading=findViewById(R.id.loading);
        mTips=findViewById(R.id.tips);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        //显示上一个界面显示的手机号
        mPhone.setText(mPhoneStr);
    }

    private void submit() {
        String password=mPw.getText().toString();
        //网络请求登录
        mPresenter.requestLogin(mPhoneStr,password);

    }

    /**
     * 处理登录成功UI
     */
    @Override
    public void showLoginSuc(){
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText("登录成功");
        dismiss();
        ToastUtil.show(getContext(),"登录成功");

    }

    /**
     * 显示服务器出错
     */
    public void showServerError(){
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 密码错误
     */
    public void showPasswordError(){
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText("密码错误");
    }

    /**
     * 显示或隐藏Loading
     * @param show
     */
    public void showOrHideLoading(boolean show){
        if(show){
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        }else{
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

        //注销Presenter
        RxBus.getInstance().unRegister(mPresenter);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
