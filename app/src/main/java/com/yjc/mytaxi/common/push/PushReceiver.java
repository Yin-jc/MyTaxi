package com.yjc.mytaxi.common.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yjc.mytaxi.common.util.LogUtil;

import cn.bmob.push.PushConstants;


/**
 * Created by Administrator on 2017/11/8/008.
 */

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String msg=intent.getStringExtra("msg");
            LogUtil.d("bmob","客户端收到推送内容："+msg);

            // TODO: 2017/11/8/008 通知业务或UI
        }
    }
}
