package com.yjc.mytaxi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.yjc.mytaxi.common.util.DevUtil;
import com.yjc.mytaxi.common.util.ToastUtil;

import java.lang.ref.SoftReference;

/**
 * Created by Administrator on 2017/11/6/006.
 * 密码创建/修改
 */

public class CreatePasswordDialog extends Dialog{

    private static final String TAG="CreatePasswordDialog";
    private static final int REGISTER_SUC=1;
    private static final int SERVER_FAIL=100;
    private static final int LOGIN_SUC = 2;
    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mRePw;
    private Button mBtnConfim;
    private View mLoading;
    private TextView mTips;
    private IHttpClient mHttpClient;
    private String mPhoneStr;
    private MyHandler mHandler;

    /**
     * 接受子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<CreatePasswordDialog> codeDialogRef;

        public MyHandler(CreatePasswordDialog codeDialog) {
            codeDialogRef = new SoftReference<>(codeDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatePasswordDialog dialog=codeDialogRef.get();
            if(dialog==null){
                return;
            }
            //处理UI变化
            switch (msg.what){
                case REGISTER_SUC:
                    dialog.showRegisterSuc();
                    break;
                case SERVER_FAIL:
                    dialog.showServerError();
                    break;
                case LOGIN_SUC:
                    dialog.showLoginSuc();
            }
        }
    }

    private void showLoginSuc() {
        dismiss();
        ToastUtil.show(getContext(),getContext().getString(R.string.login_suc));
    }

    private void showServerError() {
        mTips.setTextColor(getContext().getResources()
                .getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 处理注册成功
     */
    private void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfim.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources()
                .getColor(R.color.color_text_normal));
        mTips.setText(getContext()
                .getString(R.string.register_suc_and_loading));
        // 请求网络，完成自动登录
        new Thread(){
            @Override
            public void run() {
                String url= API.Config.getDomain()+API.LOGIN;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",mPhoneStr);
                String password=mPw.getText().toString();
                request.setBody("password",password);
                IResponse response=mHttpClient.post(request,false);
                Log.d(TAG,response.getData());
                if(response.getCode()== BaseResponse.STATE_OK){
                    LoginResponse loginRes=new Gson()
                            .fromJson(response.getData(),LoginResponse.class);
                    if(loginRes.getCode()==BaseBizResponse.STATE_OK){
                        //保存登录信息
                        Account account=loginRes.getData();
                        SharedPreferenceDao dao=
                                new SharedPreferenceDao(MyTaxiApplication.getInstance()
                                ,SharedPreferenceDao.FILE_ACCOUNT);
                        dao.save(SharedPreferenceDao.KEY_ACCOUNT,account);
                        mHandler.sendEmptyMessage(LOGIN_SUC);
                    } else {
                        mHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                }else {

                }

            }
        }.start();

    }

    public CreatePasswordDialog(@NonNull Context context,String phone) {
        this(context, R.style.Dialog);
        //从上一个页面传来的手机号
        mPhoneStr=phone;
        mHttpClient=new OkHttpClientImpl();
        mHandler=new MyHandler(this);
    }

    public CreatePasswordDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected CreatePasswordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root=inflater.inflate(R.layout.dialog_create_pw,null);
        setContentView(root);
        initViews();
    }

    private void initViews() {
        mPhone=findViewById(R.id.phone);
        mPw=findViewById(R.id.pw);
        mRePw=findViewById(R.id.pwl);
        mBtnConfim=findViewById(R.id.btn_confirm);
        mLoading=findViewById(R.id.loading);
        mTips=findViewById(R.id.tips);
        mTitle=findViewById(R.id.dialog_title);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        mPhone.setText(mPhoneStr);
    }

    /**
     * 提交注册
     */
    private void submit() {
        if(checkPassword()){
            final String password=mPw.getText().toString();
            final String phone=mPhoneStr;
            //请求网络，提交注册
            new Thread(){
                @Override
                public void run() {
                    String url= API.Config.getDomain()+API.REGISTER;
                    IRequest request=new BaseRequest(url);
                    request.setBody("phone",phone);
                    request.setBody("password",password);
                    request.setBody("uid", DevUtil.UUID(getContext()));
                    IResponse response=mHttpClient.get(request,false);
                    Log.d(TAG,response.getData());
                    if(response.getCode()== BaseResponse.STATE_OK){
                        BaseBizResponse bizResponse=new Gson()
                                .fromJson(response.getData(),BaseBizResponse.class);
                        if(bizResponse.getCode()==BaseBizResponse.STATE_OK){
                            mHandler.sendEmptyMessage(REGISTER_SUC);
                        } else {
                            mHandler.sendEmptyMessage(SERVER_FAIL);
                        }
                    }else {

                    }

                }
            }.start();
        }
    }

    /**
     * 检查密码输入
     * @return
     */
    private boolean checkPassword() {
        String password=mPw.getText().toString();
        if(TextUtils.isEmpty(password)){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_null));
            mTips.setTextColor(getContext()
                    .getResources().getColor(R.color.error_red));
            return false;
        }
        if(!password.equals(mRePw.getText().toString())){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_not_equal));
            mTips.setTextColor(getContext()
                    .getResources().getColor(R.color.error_red));
            return false;
        }
        return true;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
