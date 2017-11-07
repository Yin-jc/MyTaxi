package com.yjc.mytaxi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.response.Account;
import com.yjc.mytaxi.account.response.LoginResponse;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.http.impl.BaseResponse;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.ToastUtil;

import java.lang.ref.SoftReference;

/**
 * Created by Administrator on 2017/11/7/007.
 * 登录框
 */

public class LoginDialog extends Dialog{

    private static final String TAG ="LoginDialog" ;
    private static final int LOGIN_SUC = 1;
    private static final int SERVER_FAIL = -1;
    private static final int PW_ERROR = -2;
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private IHttpClient mHttpClient;
    private MyHandler mHandler;

    /**
     * 接受子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<LoginDialog> dialogRef;

        public MyHandler(LoginDialog dialog) {
            dialogRef = new SoftReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialog dialog=dialogRef.get();
            if(dialog==null){
                return;
            }
            //处理UI变化
            switch (msg.what){
                case LOGIN_SUC:
                    dialog.showLoginSuc();
                    break;
                case PW_ERROR:
                    dialog.showPasswordError();
                    break;
                case SERVER_FAIL:
                    dialog.showServerError();
                    break;
            }
        }
    }

    public LoginDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        mPhoneStr=phone;
        mHttpClient=new OkHttpClientImpl();
        mHandler=new MyHandler(this);
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
    }

    @Override
    public void dismiss() {
        super.dismiss();
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
        final String password=mPw.getText().toString();

        // 网络请求登录
        new Thread(){
            @Override
            public void run() {
                String url= API.Config.getDomain()+API.LOGIN;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",mPhoneStr);
//                String password=mPw.getText().toString();
                request.setBody("password",password);

                IResponse response=mHttpClient.post(request,false);
                Log.d(TAG,response.getData());
                if(response.getCode()== BaseResponse.STATE_OK){
                    LoginResponse loginRes=new Gson()
                            .fromJson(response.getData(),LoginResponse.class);
                    if(loginRes.getCode()==BaseBizResponse.STATE_OK) {
                        //保存登录信息
                        Account account = loginRes.getData();
                        // TODO: 2017/11/7/007 加密存储
                        SharedPreferenceDao dao =
                                new SharedPreferenceDao(MyTaxiApplication.getInstance()
                                        , SharedPreferenceDao.FILE_ACCOUNT);
                        dao.save(SharedPreferenceDao.KEY_ACCOUNT, account);
                        mHandler.sendEmptyMessage(LOGIN_SUC);
                    }
                    if(loginRes.getCode()==BaseBizResponse.STATE_PW_ERROR){
                        mHandler.sendEmptyMessage(PW_ERROR);
                    } else {
                        mHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                }else {
                    mHandler.sendEmptyMessage(SERVER_FAIL);
                }

            }
        }.start();

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

    /**
     * 处理登录成功UI
     */
    public void showLoginSuc(){
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText("登录成功");
        ToastUtil.show(getContext(),"登录成功");
        dismiss();
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


}
