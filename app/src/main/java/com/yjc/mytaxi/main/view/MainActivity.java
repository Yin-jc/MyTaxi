package com.yjc.mytaxi.main.view;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.view.PhoneInputDialog;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.lbs.GaoDeLbsLayerImpl;
import com.yjc.mytaxi.common.lbs.ILbsLayer;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.common.lbs.RouteInfo;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.DevUtil;
import com.yjc.mytaxi.common.util.LogUtil;
import com.yjc.mytaxi.common.util.ToastUtil;
import com.yjc.mytaxi.main.model.IMainManager;
import com.yjc.mytaxi.main.model.MainManagerImpl;
import com.yjc.mytaxi.main.model.bean.Order;
import com.yjc.mytaxi.main.presenter.IMainPresenter;
import com.yjc.mytaxi.main.presenter.MainPresenterImpl;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;

/**
 * 检查本地记录
 * 若用户没有登录则登录
 * 登录之前先校验手机号码
 * token有效使用token自动登录
 * 地图接入
 * 定位自己位置，显示蓝点
 * 使用Maker标记当前位置和方向
 * 地图封装
 * 获取附近司机
 */
public class MainActivity extends AppCompatActivity implements IMainView{

    private static final String TAG="MainActivity";
    private static final String LOCATION_END = "10000end";
    private IMainPresenter mPresenter;
    private ILbsLayer mLbsLayer;
    private Bitmap mDriverBit;
    private String mPushKey;
    //起点与终点
    private AutoCompleteTextView mStartEdit;
    private AutoCompleteTextView mEndEdit;
    private PoiAdapter mEndAdapter;
    //标题栏显示当前城市
    private TextView mCity;
    //记录起点和终点
    private LocationInfo mStartLocation;
    private LocationInfo mEndLocation;
    private Bitmap mStartBit;
    private Bitmap mEndBit;
    //操作状态相关元素
    private View mOptArea;
    private View mSelectArea;
    private View mLoadingArea;
    private TextView mTips;
    private TextView mLoadingText;
    private Button mBtnCall;
    private Button mBtnCancel;
    private Button mBtnPay;
    private float mCost;
    //判断当前是否已经登录
    private boolean mIsLogin;

    private Bitmap mLocationBit;
    //判断当前是否已经定位
    private boolean mIsLocate;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient httpClient=new OkHttpClientImpl();
        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        IAccountManager manager=new AccountManagerImpl(httpClient,dao);
        IMainManager mainManager=new MainManagerImpl(httpClient);
        mPresenter=new MainPresenterImpl(this,manager,mainManager);
        mPresenter.loginByToken();

        //注册 Presenter
        RxBus.getInstance().register(mPresenter);

        //地图服务
        mLbsLayer=new GaoDeLbsLayerImpl(this);
        mLbsLayer.onCreate(savedInstanceState);
        mLbsLayer.setLocationChangeListener(new ILbsLayer.CommonLocationChangeListener() {
            @Override
            public void onLocationChange(LocationInfo locationInfo) {
                Log.d(TAG,"onLocationChange");
            }

            @Override
            public void onLocation(LocationInfo locationInfo) {
                //隐藏软键盘
                DevUtil.closeInputMethod(MainActivity.this);
//                mLbsLayer.addOrUpdateMarker(locationInfo,
//                        BitmapFactory.decodeResource(getResources(),R.drawable.start));
                //记录起点
                mStartLocation=locationInfo;
                //设置标题
                mCity.setText(mLbsLayer.getCity());
                //设置起点
                mStartEdit.setText(locationInfo.getName());
                //获取附近司机
                getNearDrivers(locationInfo.getLatitude(),locationInfo.getLongitude());
                //上报当前位置
                updateLocationToServer(locationInfo);
                //首次定位，添加当前位置的标记
                // TODO: 2017/11/11/011 放在前面无法定位？
                addLocationMarker();
                mIsLocate=true;
                //获取进行中的订单
                getProcessingOrder();

            }
        });
        ViewGroup mapViewContainer= (ViewGroup) findViewById(R.id.map_container);
        mapViewContainer.addView(mLbsLayer.getMapView());

        //推送服务 初始化BmobSDK
        Bmob.initialize(this, API.Config.getAppId());
        //使用推送服务时的初始化操作
        BmobInstallation installation=BmobInstallation.getCurrentInstallation(this);
        installation.save();
        mPushKey=installation.getInstallationId();
        //启动推送服务
        BmobPush.startWork(this);
        //初始化其他视图元素
        initViews();

        mIsLogin=mPresenter.isLogin();
    }

    private void initViews() {
        mStartEdit= (AutoCompleteTextView) findViewById(R.id.start);
        mEndEdit= (AutoCompleteTextView) findViewById(R.id.end);
        mCity= (TextView) findViewById(R.id.city);
        mOptArea = findViewById(R.id.optArea);
        mSelectArea=findViewById(R.id.select_area);
        mLoadingArea = findViewById(R.id.loading_area);
        mLoadingText = (TextView) findViewById(R.id.loading_text);
        mBtnCall = (Button) findViewById(R.id.btn_call_driver);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnPay = (Button) findViewById(R.id.btn_pay);
        mTips = (TextView) findViewById(R.id.tips_info);
        mEndEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //关键搜索推荐地点
                mLbsLayer.poiSearch(s.toString(), new ILbsLayer.OnSearchedListener() {
                    @Override
                    public void onSearched(List<LocationInfo> results) {
                        //更新列表
                        updatePoiList(results);
                    }

                    @Override
                    public void onError(int rCode) {

                    }
                });
            }
        });
        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_call_driver:
                        //呼叫司机
                        callDriver();
                        break;
                    case R.id.btn_cancel:
                        // 取消呼叫
                        cancel();
                        break;
                    case R.id.btn_pay:
                        // 支付
                        pay();
                        break;
                }
            }
        };
        mBtnCall.setOnClickListener(listener);
        mBtnCancel.setOnClickListener(listener);
        mBtnPay.setOnClickListener(listener);
    }

    private void pay() {
        mLoadingArea.setVisibility(View.VISIBLE);
        mTips.setVisibility(View.GONE);
        mLoadingText.setText(getString(R.string.paying));
        mPresenter.pay();
    }

    /**
     * 取消呼叫
     */
    private void cancel() {
        if(!mBtnCall.isEnabled()){
            //说明已经点击了呼叫
            showCanceling();
            mPresenter.cancel();
        }else {
            //只是显示了路径信息，还没呼叫
            restoreUI();
        }
    }

    /**
     * 显示取消中
     */
    private void showCanceling() {
        mTips.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.VISIBLE);
        mLoadingText.setText(getString(R.string.canceling));
        mBtnCancel.setEnabled(false);
    }

    /**
     * 呼叫司机
     */
    private void callDriver() {
        mIsLogin=mPresenter.isLogin();
        if(mIsLogin){
            //已登录，直接呼叫
            showCalling();
            // 请求呼叫
            mPresenter.callDriver(mPushKey,mCost,mStartLocation,mEndLocation);
        }else {
            //未登录，先登录
//            mPresenter.loginByToken();
            ToastUtil.show(this,"请先登录");
            showPhoneInputDialog();
        }
    }

    /**
     * 显示呼叫中
     */
    private void showCalling() {
        mTips.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.VISIBLE);
        mLoadingText.setText(R.string.calling_driver);
        mBtnCall.setEnabled(false);
        mBtnCancel.setEnabled(true);
    }

    /**
     * 更新POI列表
     * @param results
     */
    private void updatePoiList(final List<LocationInfo> results) {
        List<String> listString=new ArrayList<>();
        for(int i=0;i<results.size();i++) {
            listString.add(results.get(i).getName());
        }
        if(mEndAdapter==null){
            mEndAdapter=new PoiAdapter(getApplicationContext(),listString);
            mEndEdit.setAdapter(mEndAdapter);
        }else{
            mEndAdapter.setData(listString);
        }
        mEndEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToastUtil.show(MainActivity.this,results.get(position).getName());
                //隐藏软键盘
                DevUtil.closeInputMethod(MainActivity.this);
                //记录终点
                mEndLocation=results.get(position);
                mEndLocation.setKey(LOCATION_END);
                //绘制路径
                showRoute(mStartLocation, mEndLocation, new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        LogUtil.d(TAG, "driverRoute: " + result);
                        mLbsLayer.moveCamera(mStartLocation, mEndLocation);
                        // 显示操作区
                        showOptArea();
                        mCost = result.getTaxiCost();
                        String infoString = getString(R.string.route_info);
                        infoString = String.format(infoString,
                                (int) result.getDistance(),
                                mCost,
                                result.getDuration());
                        mTips.setVisibility(View.VISIBLE);
                        mTips.setText(infoString);
                    }
                });
            }
        });
        mEndAdapter.notifyDataSetChanged();
    }

    /**
     * 绘制起点终点路径
     * @param mStartLocation
     * @param mEndLocation
     */
    private void showRoute(final LocationInfo mStartLocation, final LocationInfo mEndLocation,
                           ILbsLayer.OnRouteCompleteListener listener) {
        mLbsLayer.clearAllMarkers();
        addStartMarker();
        addEndMarker();
        mLbsLayer.driveRoute(mStartLocation, mEndLocation, Color.GREEN,
//                new ILbsLayer.OnRouteCompleteListener() {
//                    @Override
//                    public void onComplete(RouteInfo result) {
//                        LogUtil.d(TAG,"driveRoute:"+result);
//
//                        mLbsLayer.moveCamera(mStartLocation,mEndLocation);
//                        //显示操作区
//                        showOptArea();
//                        mCost=result.getTaxiCost();
//                        String infoString=getString(R.string.route_info);
//                        infoString= String.format(infoString,
//                                (int)result.getDistance(),mCost,result.getDuration());
//                        mTips.setVisibility(View.VISIBLE);
//                        mTips.setText(infoString);
//                    }
//                }
                listener);
    }

    /**
     * 显示操作区
     */
    private void showOptArea() {
        mOptArea.setVisibility(View.VISIBLE);
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mBtnCall.setEnabled(true);
        mBtnCancel.setEnabled(true);
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnCall.setVisibility(View.VISIBLE);
        mBtnPay.setVisibility(View.GONE);
    }

    /**
     * 显示起点标记
     */
    private void addStartMarker() {
        if(mStartBit==null || mStartBit.isRecycled()){
            mStartBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.start);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation,mStartBit);
    }

    /**
     * 显示终点标记
     */
    private void addEndMarker() {
        if(mEndBit==null || mEndBit.isRecycled()){
            mEndBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.end);
        }
        mLbsLayer.addOrUpdateMarker(mEndLocation,mEndBit);
    }

    /**
     * 上报当前位置
     * @param locationInfo
     */
    private void updateLocationToServer(LocationInfo locationInfo) {
        locationInfo.setKey(mPushKey);
        mPresenter.updateLocationToServer(locationInfo);
    }

    /**
     * 获取附近的司机
     * @param latitude
     * @param longitude
     */
    private void getNearDrivers(double latitude, double longitude) {
        mPresenter.fetchNearDrivers(latitude,longitude);
    }


    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog dialog=new PhoneInputDialog(this);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                RxBus.getInstance().register(mPresenter);
            }
        });
        RxBus.getInstance().unRegister(mPresenter);
    }

    @Override
    public void showLoading() {
        // TODO: 2017/11/7/007 显示加载框
    }

    @Override
    public void showError(int code, String msg) {
        switch (code){
            case IAccountManager.TOKEN_INVALID:
                ToastUtil.show(this,"登录过期");
                showPhoneInputDialog();
                mIsLogin=false;
                break;
            case IAccountManager.SERVER_FAIL:
                showPhoneInputDialog();
                break;
        }
    }

    /**
     * 登录成功
     */
    @Override
    public void showLoginSuc() {
        ToastUtil.show(this,getString(R.string.login_suc));
        mIsLogin=true;
        if(mStartLocation!=null){
            updateLocationToServer(mStartLocation);
        }
        getProcessingOrder();
    }

    /**
     * 显示附近的司机
     * @param data
     */
    @Override
    public void showNears(List<LocationInfo> data) {
        for (LocationInfo locationInfo:data){
            showDriverLocation(locationInfo);
        }
    }

    /**
     * 显示司机的标记
     * @param locationInfo
     */
    @Override
    public void showDriverLocation(LocationInfo locationInfo) {
        if(mDriverBit==null || mDriverBit.isRecycled()){
            mDriverBit=BitmapFactory.decodeResource(getResources(),R.drawable.car);
        }
        mLbsLayer.addOrUpdateMarker(locationInfo,mDriverBit);
    }

    /**
     * 呼叫司机成功
     */
    // TODO: 2017/11/11/011 此处的逻辑是否和列表选择地点后绘制路径逻辑重复
    @Override
    public void showCallDriverSuc(Order order) {
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_suc));
        //显示操作区
        showOptArea();
        mBtnCall.setEnabled(false);
        //显示路径信息
//        if(order.getEndLatitude()!=0 ||
//                order.getEndLongitude()!=0){
//            mEndLocation=new LocationInfo(order.getEndLatitude(),
//                    order.getEndLongitude());
//            mEndLocation.setKey(LOCATION_END);
//            //绘制路径
//            showRoute(mStartLocation, mEndLocation, new ILbsLayer.OnRouteCompleteListener() {
//                @Override
//                public void onComplete(RouteInfo result) {
//                    LogUtil.d(TAG,"driverRoute:"+result);
//                    mLbsLayer.moveCamera(mStartLocation,mEndLocation);
//                    mCost=result.getTaxiCost();
//                    String infoString=getString(R.string.route_info_calling);
//                    mTips.setText(String.format(infoString,
//                            (int)result.getDistance(),
//                            mCost,
//                            result.getDuration()));
//                    mTips.setVisibility(View.VISIBLE);
//                    mBtnCall.setEnabled(false);
//                }
//            });
//        }
        LogUtil.d(TAG,"showCallDriverSuc:"+order);
    }

    /**
     * 呼叫司机失败
     */
    @Override
    public void showCallDriverFail() {
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_fail));
        mBtnCall.setEnabled(true);
    }

    /**
     * 订单取消成功
     */
    @Override
    public void showCancelSuc() {
        ToastUtil.show(this,getString(R.string.order_cancel_suc));
        restoreUI();
    }

    /**
     * 订单取消失败
     */
    @Override
    public void showCancelFail() {
        ToastUtil.show(this,getString(R.string.order_cancel_error));
        mBtnCancel.setEnabled(true);
    }

    /**
     * 显示司机接单
     * @param order
     */
    @Override
    public void showDriverAcceptOrder(final Order order) {
        ToastUtil.show(this,getString(R.string.driver_accept_order));
        //清除地图标记
        mLbsLayer.clearAllMarkers();
        //添加司机标记
        final LocationInfo driverLocation=new LocationInfo(order.getDriverLatitude(),
                order.getDriverLongitude());
        showDriverLocation(driverLocation);
        //显示我的位置
        addLocationMarker();
        //显示司机到乘客的路径
        mLbsLayer.driveRoute(driverLocation, mStartLocation,
                Color.BLUE,
                new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        //地图聚焦司机和我的位置
                        mLbsLayer.moveCamera(mStartLocation,driverLocation);
                        //显示司机、路径信息
                        StringBuilder stringBuilder=new StringBuilder();
                        stringBuilder.append("司机:")
                                     .append(order.getName())
                                     .append(",车牌:")
                                     .append(order.getCarNo())
                                     .append(",预计")
                                     .append(result.getDuration())
                                     .append("分钟到达");
                        mTips.setText(stringBuilder.toString());
                        //显示操作区
                        showOptArea();
                        //呼叫不可点击
                        mBtnCall.setEnabled(false);
                    }
                });
    }

    /**
     * 显示司机到达上车点
     * @param mCurrentOrder
     */
    @Override
    public void showDriverArriveStart(Order mCurrentOrder) {
        showOptArea();
        String arriveTemp=getString(R.string.driver_arrive);
        mTips.setText(String.format(arriveTemp,mCurrentOrder.getName(),
                mCurrentOrder.getCarNo()));
        mBtnCall.setEnabled(false);
        mBtnCancel.setEnabled(true);
        //清除地图标记
        mLbsLayer.clearAllMarkers();
        LocationInfo driverLocation=new LocationInfo(
                mCurrentOrder.getDriverLatitude(),
                mCurrentOrder.getDriverLongitude());
        // TODO: 2017/11/11/011 是否可以去掉
        driverLocation.setKey(mCurrentOrder.getKey());
        showDriverLocation(driverLocation);
        //显示我的位置
        addLocationMarker();
    }

    /**
     * 显示开始行程
     * @param order
     */
    @Override
    public void showStartDrive(Order order) {
        LocationInfo locationInfo=new LocationInfo(order.getDriverLatitude(),
                order.getDriverLongitude());
        //路径规划绘制
        updateDriverToEndRoute(locationInfo,order);
        //隐藏按钮
        mBtnCancel.setVisibility(View.GONE);
        mBtnCall.setVisibility(View.GONE);
    }

    /**
     * 显示到达终点
     * @param order
     */
    @Override
    public void showArriveEnd(Order order) {
        String tipsTemp=getString(R.string.pay_info);
        mTips.setText(String.format(tipsTemp,
                order.getCost(),
                order.getName(),
                order.getCarNo()));
        //显示操作区
        showOptArea();
        mBtnCall.setVisibility(View.GONE);
        mBtnCancel.setVisibility(View.GONE);
        mBtnPay.setVisibility(View.VISIBLE);
    }

    /**
     * 司机到终点的路径绘制或更新
     * @param locationInfo
     * @param order
     */
    @Override
    public void updateDriverToEndRoute(LocationInfo locationInfo, final Order order) {
        //终点位置从order中获取
        if(order.getEndLatitude()!=0 ||
                order.getEndLongitude()!=0){
            mEndLocation=new LocationInfo(order.getEndLatitude(),
                    order.getEndLongitude());
            mEndLocation.setKey(LOCATION_END);
        }
        mLbsLayer.clearAllMarkers();
        //添加终点标记
        addEndMarker();
        //添加司机标记
        showDriverLocation(locationInfo);
        mLbsLayer.driveRoute(locationInfo, mEndLocation, Color.GREEN,
                new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        String tipsTemp=getString(R.string.driving_info);
                        mTips.setText(String.format(tipsTemp,
                                order.getName(),
                                order.getCarNo(),
                                result.getDistance(),
                                result.getDuration()));
                        //显示操作区
                        showOptArea();
                        mBtnCall.setEnabled(false);
                        mBtnCancel.setEnabled(false);
                    }
                });
        //聚焦
        mLbsLayer.moveCamera(locationInfo,mEndLocation);
    }

    /**
     * 支付成功
     * @param order
     */
    @Override
    public void showPaySuc(Order order) {
        restoreUI();
        ToastUtil.show(this,getString(R.string.pay_suc));
    }

    /**
     * 支付失败
     */
    @Override
    public void showPayFail() {
        restoreUI();
        ToastUtil.show(this,getString(R.string.pay_fail));
    }

    /**
     * 司机到上车点的路径绘制或更新
     * @param locationInfo
     * @param order
     */
    @Override
    public void updateDriverToStartRoute(LocationInfo locationInfo, final Order order) {
        mLbsLayer.clearAllMarkers();
        addLocationMarker();
        showDriverLocation(locationInfo);
        mLbsLayer.driveRoute(locationInfo, mStartLocation, Color.BLUE,
                new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        String tipsTemp=getString(R.string.accept_info);
                        mTips.setText(String.format(tipsTemp,
                                order.getName(),
                                order.getCarNo(),
                                result.getDistance(),
                                result.getDuration()));
                    }
                });
        //聚焦
        mLbsLayer.moveCamera(locationInfo,mStartLocation);
    }

    /**
     * 恢复UI
     */
    private void restoreUI() {
        //清除地图上所有标记
        mLbsLayer.clearAllMarkers();
        //添加定位标记
        addLocationMarker();
        //恢复地图视野,第二个参数为缩放系数
        mLbsLayer.moveCameraToPoint(mStartLocation,17);
        //获取附近司机
        getNearDrivers(mStartLocation.getLatitude(),
                mStartLocation.getLongitude());
        //隐藏操作栏
        hideOptArea();
    }

    private void hideOptArea() {
        mOptArea.setVisibility(View.GONE);
    }

    private void addLocationMarker() {
        if(mLocationBit==null || mLocationBit.isRecycled()){
            mLocationBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.navi_map_gps_locked);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation,mLocationBit);
    }

    /**
     * 处理进行中的订单 Q:进入显示错误
     */
    public void getProcessingOrder() {
        if(mIsLogin && mIsLocate){
            mPresenter.getProcessingOrder();
        }
    }



    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLbsLayer.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mLbsLayer.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLbsLayer.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销Presenter
        RxBus.getInstance().unRegister(mPresenter);
        mLbsLayer.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
