package com.yjc.mytaxi.main;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.PhoneInputDialog;
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

/**
 * 检查本地记录
 * 若用户没有登录则登录
 * 登录之前先校验手机号码
 * token有效使用token自动登录
 */
public class MainActivity extends AppCompatActivity{

    private static final String TAG="MainActivity";
    private IHttpClient mHttpClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpClient=new OkHttpClientImpl();
        checkLoginState();
    }

    /**
     * 检查用户是否登录
     */
    private void checkLoginState() {
        // 获取本地登录信息

        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        final Account account= (Account) dao.get(SharedPreferenceDao.KEY_ACCOUNT,Account.class);

        //登录是否过期
        boolean tokenValid=false;

        // 检查token是否过期

        if(account!=null){
            if(Long.parseLong(account.getExpired()) > System.currentTimeMillis()){
                //token有效
                tokenValid=true;
            }
        }

        if(!tokenValid){
            showPhoneInputDialog();
        }else {
            //  请求网络，完成自动登录
            new Thread(){
                @Override
                public void run() {
                    String url= API.Config.getDomain()+API.LOGIN_BY_TOKEN;
                    IRequest request=new BaseRequest(url);
                    request.setBody("token",account.getToken());

                    IResponse response=mHttpClient.post(request,false);
                    Log.d(TAG,response.getData());
                    if(response.getCode()== BaseResponse.STATE_OK){
                        LoginResponse loginRes=new Gson()
                                .fromJson(response.getData(),LoginResponse.class);
                        if(loginRes.getCode()== BaseBizResponse.STATE_OK) {
                            //保存登录信息
                            Account account = loginRes.getData();
                            // TODO: 2017/11/7/007 加密存储
                            SharedPreferenceDao dao =
                                    new SharedPreferenceDao(MyTaxiApplication.getInstance()
                                            , SharedPreferenceDao.FILE_ACCOUNT);
                            dao.save(SharedPreferenceDao.KEY_ACCOUNT, account);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(MainActivity.this,
                                            getString(R.string.login_suc));
                                }
                            });
                        }
                        if(loginRes.getCode()==BaseBizResponse.STATE_TOKEN_INVALID){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showPhoneInputDialog();
                                }
                            });
                        }
                    }else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(MainActivity.this,
                                        getString(R.string.error_server));
                            }
                        });
                    }

                }
            }.start();
        }
    }

    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog dialog=new PhoneInputDialog(this);
        dialog.show();
    }
}
