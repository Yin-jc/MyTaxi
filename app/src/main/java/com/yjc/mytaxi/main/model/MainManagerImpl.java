package com.yjc.mytaxi.main.model;

import android.util.Log;

import com.google.gson.Gson;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.lbs.LocationInfo;

import com.yjc.mytaxi.common.util.LogUtil;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/11/8/008.
 */

public class MainManagerImpl implements IMainManager{
    private static final String TAG = "MainManagerImpl";
    IHttpClient mHttpClient;


    public MainManagerImpl(IHttpClient mHttpClient) {
        this.mHttpClient = mHttpClient;
    }

    @Override
    public void fetchNearDrivers(final double latitude, final double longitude) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                IRequest request=new BaseRequest(API.Config.getDomain()+
                        API.GET_NEAR_DRIVERS);
                request.setBody("latitude",String.valueOf(latitude));
                request.setBody("longitude",String.valueOf(longitude));
                IResponse response=mHttpClient.get(request,false);
                Log.d(TAG,response.getData());
                if(response.getCode()== BaseBizResponse.STATE_OK) {
                    try {
                        NearDriverResponse nearDriverResponse =
                                new Gson().fromJson(response.getData(), NearDriverResponse.class);
                        return nearDriverResponse;
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void updateLocationToServer(final LocationInfo locationInfo) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                IRequest request=new BaseRequest(API.Config.getDomain()+
                        API.UPLOAD_LOCATION);
                request.setBody("latitude",String.valueOf(locationInfo.getLatitude()));
                request.setBody("longitude",String.valueOf(locationInfo.getLongitude()));
                request.setBody("key",locationInfo.getKey());
                request.setBody("rotation", String.valueOf(locationInfo.getRotation()));
                IResponse response=mHttpClient.post(request,false);
                Log.d(TAG,response.getData());
                if(response.getCode()== BaseBizResponse.STATE_OK) {
                    LogUtil.d(TAG,"位置上报成功");
                }else{
                    LogUtil.d(TAG,"位置上报失败");
                }
                return response;//NullPointerException
            }
        });
    }
}
