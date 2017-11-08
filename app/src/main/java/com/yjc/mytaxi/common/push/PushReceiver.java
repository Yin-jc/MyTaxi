package com.yjc.mytaxi.common.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.common.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;


/**
 * Created by Administrator on 2017/11/8/008.
 */

public class PushReceiver extends BroadcastReceiver {
    private static final int MSG_TYPE_LOCATION = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String msg=intent.getStringExtra("msg");
            LogUtil.d("bmob","客户端收到推送内容："+msg);

            // TODO: 2017/11/8/008 通知业务或UI
            try {
                JSONObject jsonObject=new JSONObject(msg);
                int type=jsonObject.optInt("type");
                if(type==MSG_TYPE_LOCATION){
                   //位置变化
                    LocationInfo locationInfo=
                            new Gson().fromJson(jsonObject.optString("data"),
                                    LocationInfo.class);
                    RxBus.getInstance().send(locationInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
