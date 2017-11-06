package com.yjc.mytaxi.main;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.bean.PhoneInputDialog;

/**
 * 检查本地记录
 * 若用户没有登录则登录
 * 登录之前先校验手机号码
 */
public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLoginState();
    }

    /**
     * 检查用户是否登录
     */
    private void checkLoginState() {
        // TODO: 2017/11/1/001 获取本地登录信息

        //登录是否过期
        boolean tokenValid=false;

        // TODO: 2017/11/1/001 检查token是否过期

        if(!tokenValid){
            showPhoneInputDialog();
        }else {
            // TODO: 2017/11/1/001 请求网络，完成自动登录
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
