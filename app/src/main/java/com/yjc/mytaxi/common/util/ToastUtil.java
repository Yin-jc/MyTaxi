package com.yjc.mytaxi.common.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast 工具类
 * Created by Administrator on 2017/11/7/007.
 */
public class ToastUtil {
    public static void show(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}
