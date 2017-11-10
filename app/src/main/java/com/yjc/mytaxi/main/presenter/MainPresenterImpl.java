package com.yjc.mytaxi.main.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.model.LoginResponse;
import com.yjc.mytaxi.account.presenter.CreatePasswordDialogPresenterImpl;
import com.yjc.mytaxi.common.dataBus.RegisterBus;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.main.model.IMainManager;
import com.yjc.mytaxi.main.model.NearDriverResponse;
import com.yjc.mytaxi.main.model.Order;
import com.yjc.mytaxi.main.model.OrderStateOptResponse;
import com.yjc.mytaxi.main.view.IMainView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class MainPresenterImpl implements IMainPresenter {

    private static final String TAG = "MainPresenterImpl";
    private IMainView view;
    private IAccountManager accountManager;
    private IMainManager mainManager;
    //当前订单
    private Order mCurrentOrder;

    public MainPresenterImpl(IMainView view, IAccountManager accountManager,
                             IMainManager mainManager) {
        this.view = view;
        this.accountManager = accountManager;
        this.mainManager=mainManager;
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse response){
        switch (response.getCode()){
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            case IAccountManager.TOKEN_INVALID:
                view.showError(IAccountManager.TOKEN_INVALID,"");
                break;
            case IAccountManager.SERVER_FAIL:
                view.showError(IAccountManager.SERVER_FAIL,"");
                break;
        }
    }

    @RegisterBus
    public void onNearDriversResponse(NearDriverResponse response){
        if(response.getCode()== BaseBizResponse.STATE_OK){
            view.showNears(response.getData());
        }
    }

    @RegisterBus
    public void onLocationInfo(LocationInfo locationInfo){
        if(mCurrentOrder!=null &&
                (mCurrentOrder.getState()==OrderStateOptResponse.ORDER_STATE_ACCEPT)){
            //更新司机到上车点的路径信息
            view.updateDriverToStartRoute(locationInfo,mCurrentOrder);
        }else if(mCurrentOrder!=null &&
                mCurrentOrder.getState()==OrderStateOptResponse.ORDER_STATE_START_DRIVE){
            //更新司机到终点的路径信息
            view.updateDriverToEndRoute(locationInfo,mCurrentOrder);
        }else {
            view.showLocationChange(locationInfo);
        }
    }

    @RegisterBus
    public void onOrderStateOptResponse(OrderStateOptResponse response){
        if(response.getState()==OrderStateOptResponse.ORDER_STATE_CREATE){
            //呼叫司机
            if(response.getCode()==BaseBizResponse.STATE_OK){
                view.showCallDriverSuc();
                //保存当前订单
                mCurrentOrder=response.getData();
            }else {
                view.showCallDriverFail();
            }
        }else if(response.getState()==OrderStateOptResponse.ORDER_STATE_CANCEL){
            //取消订单
            if(response.getCode()==BaseBizResponse.STATE_OK){
                view.showCancelSuc();
            }else {
                view.showCancelFail();
            }
        }else if(response.getState()==OrderStateOptResponse.ORDER_STATE_ACCEPT){
            //司机接单
            mCurrentOrder=response.getData();
            view.showDriverAcceptOrder(mCurrentOrder);
        }else if(response.getState()==OrderStateOptResponse.ORDER_STATE_ARRIVE_START){
            //司机到达上车点
            mCurrentOrder=response.getData();
            view.showDriverArriveStart(mCurrentOrder);
        }else if(response.getState()==OrderStateOptResponse.ORDER_STATE_START_DRIVE){
            //开始行程
            mCurrentOrder=response.getData();
            view.showStartDrive(mCurrentOrder);
        }else if(response.getState()==OrderStateOptResponse.ORDER_STATE_ARRIVE_END){
            //到达终点
            mCurrentOrder=response.getData();
            view.showArriveEnd(mCurrentOrder);
        }
    }

    @Override
    public void loginByToken() {
        accountManager.loginByToken();
    }

    @Override
    public void fetchNearDrivers(double latitude, double longtitude) {
        mainManager.fetchNearDrivers(latitude,longtitude);
    }

    @Override
    public void updateLocationToServer(LocationInfo locationInfo) {
        mainManager.updateLocationToServer(locationInfo);
    }

    @Override
    public void callDriver(String pushKey, float cost, LocationInfo startLocation, LocationInfo endLocation) {
        mainManager.callDriver(pushKey,cost,startLocation,endLocation);
    }

    @Override
    public void cancel() {
        if(mCurrentOrder!=null){
//            Log.d(TAG,"cancel");
            mainManager.cancelOrder(mCurrentOrder.getOrderId());
        }else {
            view.showCancelSuc();
        }
    }
}
