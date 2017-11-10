package com.yjc.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.dalimao.corelibrary.VerificationCodeInput;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.presenter.ISMSCodeDialogPresenter;
import com.yjc.mytaxi.account.presenter.SmsCodeDialogPresenterImpl;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.ToastUtil;

/**
 * Created by Administrator on 2017/11/1/001.
 */

public class SmsCodeDialog extends Dialog implements ISmsCodeDialogView{
    private static final String TAG="SmsCodeDialog";
    private String mPhone;
    private Button mResentBtn;
    private VerificationCodeInput mVerificationCodeInput;
    private View mLoading;
    private View mErrorView;
    private TextView mPhoneTV;
    private ISMSCodeDialogPresenter mPresenter;

    public SmsCodeDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        //从上一个界面传来的手机号
        this.mPhone=phone;
        IHttpClient httpClient=new OkHttpClientImpl();
        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        IAccountManager manager=new AccountManagerImpl(httpClient,dao);
        mPresenter=new SmsCodeDialogPresenterImpl(this,manager);
    }

    public SmsCodeDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected SmsCodeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root =inflater.inflate(R.layout.dialog_smscode_input,null);
        setContentView(root);
        mPhoneTV=findViewById(R.id.phone);
        String template="验证码已发送至%s";
        mPhoneTV.setText(String.format(template,mPhone));
        mResentBtn=findViewById(R.id.btn_resent);
        mVerificationCodeInput=findViewById(R.id.verificationCodeInput);
        mLoading=findViewById(R.id.loading);
        mErrorView=findViewById(R.id.error);
        mErrorView.setVisibility(View.GONE);
        initListeners();
        requestSendSmsCode();

        //注册Presenter
        RxBus.getInstance().register(mPresenter);
    }

    /**
     * 验证码倒计时
     * @param context
     */
    private CountDownTimer mCountDownTimer=new CountDownTimer(60*1000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mResentBtn.setEnabled(false);
            mResentBtn.setText(String.format(getContext().getString(R.string.after_time_resend),
                    millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            mResentBtn.setEnabled(true);
            mResentBtn.setText("重新发送");
            cancel();
        }
    };


    @Override
    public void showCountDownTimer() {
        mPhoneTV.setText(String.format(getContext()
                .getString(R.string.sms_code_send_phone),mPhone));
        mCountDownTimer.start();
        mResentBtn.setEnabled(false);
    }

    @Override
    public void showError(int code, String msg) {
        mLoading.setVisibility(View.GONE);
        switch (code){
            case IAccountManager.SMS_SEND_FAIL:
                ToastUtil.show(getContext(),getContext().getString(R.string.sms_send_fail));
                break;
            case IAccountManager.SMS_CHECK_FAIL:
                mErrorView.setVisibility(View.VISIBLE);
                mVerificationCodeInput.setEnabled(true);
                break;
            case IAccountManager.SERVER_FAIL:
                ToastUtil.show(getContext(),getContext().getString(R.string.error_server));
                break;
        }
    }


    /**
     * 验证码检查结果UI变化
     * @param suc
     */
    @Override
    public void showSmsCodeCheckState(boolean suc) {
        if(!suc){
            //提示验证码错误
            mErrorView.setVisibility(View.VISIBLE);
            mVerificationCodeInput.setEnabled(true);
            mLoading.setVisibility(View.GONE);
        }else {
            mLoading.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
            // 检查用户是否存在
            mPresenter.requestCheckUserExist(mPhone);
        }
    }

    /**
     * 显示用户是否存在
     * @param exist
     */
    @Override
    public void showUserExist(boolean exist) {
        mLoading.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        if(!exist){
            //用户不存在，进入注册
            CreatePasswordDialog dialog=
                    new CreatePasswordDialog(getContext(),mPhone);
            dialog.show();
        }else {
            //  用户存在，进入登录
            LoginDialog dialog=new LoginDialog(getContext(),mPhone);
            dialog.show();
        }
        dismiss();
    }

    /**
     * 请求下发验证码
     */
    private void requestSendSmsCode() {
        mPresenter.requestSendSmsCode(mPhone);
    }

    private void initListeners() {
        //关闭按钮注册监听器
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //重发验证码按钮注册监听器
        mResentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resent();
            }
        });

        //验证码输入完成监听器
        mVerificationCodeInput.setOnCompleteListener(new VerificationCodeInput.Listener() {
            @Override
            public void onComplete(String code) {
                commit(code);
            }
        });
    }

    /**
     * 提交验证码
     * @param code
     */
    private void commit(final String code) {
        mPresenter.requestCheckSmsCode(mPhone,code);

    }

    @Override
    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void resent() {
        String template="正在向%s发送验证码";
        mPhoneTV.setText(String.format(template,mPhone));
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
        mCountDownTimer.cancel();
    }

}
