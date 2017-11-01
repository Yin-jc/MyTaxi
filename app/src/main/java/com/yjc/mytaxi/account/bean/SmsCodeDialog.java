package com.yjc.mytaxi.account.bean;

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
import com.yjc.mytaxi.R;

/**
 * Created by Administrator on 2017/11/1/001.
 */

public class SmsCodeDialog extends Dialog{
    private static final String TAG="SmsCodeDialog";
    private String mPhone;
    private Button mResentBtn;
    private VerificationCodeInput mVerificationCodeInput;
    private View mLoading;
    private View mErrorView;
    private TextView mPhoneTV;

    /**
     * 验证码倒计时
     * @param context
     */
    private CountDownTimer mCountDownTimer=new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mResentBtn.setEnabled(false);
            mResentBtn.setText(String.format("{millisUntilFinished/5}秒后重新发送"));
        }

        @Override
        public void onFinish() {
            mResentBtn.setEnabled(true);
            mResentBtn.setText("重新发送");
            cancel();
        }
    };
    public SmsCodeDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        //从上一个界面传来的手机号
        this.mPhone=phone;
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

        // TODO: 2017/11/1/001 请求下发验证码 
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCountDownTimer.cancel();
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

    private void commit(String code) {
        showLoading();

        // TODO: 2017/11/1/001 网络请求校验码 
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void resent() {
        String template="正在向%s发送验证码";
        mPhoneTV.setText(String.format(template,mPhone));
    }
}
