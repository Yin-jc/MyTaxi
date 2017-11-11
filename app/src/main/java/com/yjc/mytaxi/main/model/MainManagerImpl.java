package com.yjc.mytaxi.main.model;

import android.util.Log;

import com.google.gson.Gson;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.account.model.bean.Account;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.lbs.LocationInfo;

import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.LogUtil;
import com.yjc.mytaxi.main.model.Response.NearDriverResponse;
import com.yjc.mytaxi.main.model.Response.OrderStateOptResponse;

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
                return null;//NullPointerException
            }
        });
    }

    @Override
    public void callDriver(final String pushKey, final float cost, final LocationInfo startLocation, final LocationInfo endLocation) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                SharedPreferenceDao sharedPreferenceDao=
                        new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                                SharedPreferenceDao.FILE_ACCOUNT);
                Account account= (Account) sharedPreferenceDao.get(SharedPreferenceDao.KEY_ACCOUNT,
                        Account.class);
                String uid=account.getUid();
                String phone=account.getAccount();
                IRequest request=new BaseRequest(API.Config.getDomain()+
                    API.CALL_DRIVER);
                request.setBody("key",pushKey);
                request.setBody("uid",uid);
                request.setBody("phone",phone);
                request.setBody("startLatitude", String.valueOf(
                        startLocation.getLatitude()
                ));
                request.setBody("startLongitude",String.valueOf(
                        startLocation.getLongitude()
                ));
                request.setBody("endLatitude", String.valueOf(
                        endLocation.getLatitude()
                ));
                request.setBody("endLongitude",String.valueOf(
                        endLocation.getLongitude()
                ));
                request.setBody("cost",String.valueOf(cost));

                IResponse response=mHttpClient.post(request,false);
                OrderStateOptResponse orderStateOptResponse=
                        new OrderStateOptResponse();
                if(response.getCode()==BaseBizResponse.STATE_OK){
                    //解析订单信息
                    orderStateOptResponse=new Gson().fromJson(response.getData(),
                            OrderStateOptResponse.class);
                }
                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.
                        ORDER_STATE_CREATE);
                LogUtil.d(TAG,"call driver:"+response.getData());
                LogUtil.d(TAG,"call driver phone:"+phone);
                return orderStateOptResponse;
            }
        });
    }

    @Override
    public void cancelOrder(final String orderId) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                IRequest request=new BaseRequest(API.Config.getDomain()
                    +API.CANCEL_ORDER);
                request.setBody("id",orderId);
                IResponse response=mHttpClient.post(request,false);
                OrderStateOptResponse orderStateOptResponse=
                        new OrderStateOptResponse();
                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.ORDER_STATE_CANCEL);
                LogUtil.d(TAG,"cancel order:"+response.getData());
                LogUtil.d(TAG,"cancel order:"+response.getCode());
                LogUtil.d(TAG,"cancel order:"+orderStateOptResponse.getCode());
                return orderStateOptResponse;
            }
        });
    }

    @Override
    public void pay(final String orderId) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                IRequest request=new BaseRequest(API.Config.getDomain()+
                API.PAY);
                request.setBody("id",orderId);
                IResponse response=mHttpClient.post(request,false);
                OrderStateOptResponse orderStateOptResponse=
                        new OrderStateOptResponse();
                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.PAY);

                LogUtil.d(TAG,"cancel order:"+response.getData());
                return orderStateOptResponse;
            }
        });
    }

    /**
     * 获取进行中的订单
     */
    @Override
    public void getProcessingOrder() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                SharedPreferenceDao sharedPreferenceDao=
                        new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                                SharedPreferenceDao.FILE_ACCOUNT);
                Account account= (Account) sharedPreferenceDao.get(SharedPreferenceDao.KEY_ACCOUNT,
                        Account.class);
                String uid=account.getUid();
                IRequest request=new BaseRequest(API.Config.getDomain()+
                        API.GET_PROCESSING_ORDER);
                request.setBody("uid",uid);
                IResponse response=mHttpClient.get(request,false);
                LogUtil.d(TAG,"getProcessingOrder order:"+response.getData());
                if(response.getCode()==BaseBizResponse.STATE_OK){
                    //解析订单
                    OrderStateOptResponse orderStateOptResponse=
                            new Gson().fromJson(response.getData(),
                                    OrderStateOptResponse.class);
                    // TODO: 2017/11/11/011 此处必须进行判断,否则会出现订单状态错乱
                    if(orderStateOptResponse.getCode()==BaseBizResponse.STATE_OK){
                        orderStateOptResponse.setCode(response.getCode());
                        orderStateOptResponse.setState(orderStateOptResponse.getData().getState());
                        LogUtil.d(TAG,"getProcessingOrder order state="+orderStateOptResponse);
                        return orderStateOptResponse;
                    }
                }
                return null;
            }
        });

    }
}
