package com.yjc.mytaxi.common.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.common.util.LogUtil;
import com.yjc.mytaxi.main.model.Order;
import com.yjc.mytaxi.main.model.OrderStateOptResponse;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;


/**
 * Created by Administrator on 2017/11/8/008.
 */

public class PushReceiver extends BroadcastReceiver {
    private static final int MSG_TYPE_LOCATION = 1;
    //订单变化
    private static final int MSG_TYPE_ORDER=2;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String msg=intent.getStringExtra("msg");
            LogUtil.d("bmob","客户端收到推送内容："+msg);

            //  通知业务或UI
            try {
                JSONObject jsonObject=new JSONObject(msg);
                int type=jsonObject.optInt("type");
                if(type==MSG_TYPE_LOCATION){
                   //位置变化
                    LocationInfo locationInfo=
                            new Gson().fromJson(jsonObject.optString("data"),
                                    LocationInfo.class);
                    RxBus.getInstance().send(locationInfo);
                }else if(type==MSG_TYPE_ORDER){
                    //订单变化
                    Order order=new Gson().fromJson(
                            jsonObject.optString("data"),Order.class);
                    OrderStateOptResponse stateOptResponse=
                            new OrderStateOptResponse();
                    stateOptResponse.setData(order);
                    stateOptResponse.setState(
                            order.getState());
                    stateOptResponse.setCode(BaseBizResponse.STATE_OK);
                    //通知UI
                    RxBus.getInstance().send(stateOptResponse);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
