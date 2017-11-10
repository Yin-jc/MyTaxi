package com.yjc.mytaxi.common.util;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Administrator on 2017/11/6/006.
 * 设备相关的工具类
 */

public class DevUtil {
    /**
     * 获取UID
     * @param context
     * @return
     */
    public static String UUID(Context context){
        TelephonyManager tm= (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId=tm.getDeviceId();
        return deviceId+ System.currentTimeMillis();
    }

    /**
     * 隐藏软键盘
     * @param context
     */
    public static void closeInputMethod(Activity context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
