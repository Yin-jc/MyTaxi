package com.yjc.mytaxi.main.view;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.google.gson.Gson;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.presenter.LoginDialogPresenterImpl;
import com.yjc.mytaxi.account.view.PhoneInputDialog;
import com.yjc.mytaxi.account.model.Account;
import com.yjc.mytaxi.account.model.LoginResponse;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.http.impl.BaseResponse;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.SensorEventHelper;
import com.yjc.mytaxi.common.util.ToastUtil;
import com.yjc.mytaxi.main.presenter.IMainPresenter;
import com.yjc.mytaxi.main.presenter.MainPresenterImpl;

/**
 * 检查本地记录
 * 若用户没有登录则登录
 * 登录之前先校验手机号码
 * token有效使用token自动登录
 * 地图接入
 * 定位自己位置，显示蓝点
 * 使用Maker标记当前位置和方向
 */
public class MainActivity extends AppCompatActivity implements IMainView{

    private static final String TAG="MainActivity";
    private IMainPresenter mPresenter;
    private MapView mapView;
    private AMap aMap;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker mLocMarker;
    private SensorEventHelper mSensorHelper;
    private boolean mFirst=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient httpClient=new OkHttpClientImpl();
        SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                SharedPreferenceDao.FILE_ACCOUNT);
        IAccountManager manager=new AccountManagerImpl(httpClient,dao);
        mPresenter=new MainPresenterImpl(this,manager);
        mPresenter.loginByToken();

        //注册 Presenter
        RxBus.getInstance().register(mPresenter);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();
        mSensorHelper=new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些aMap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                doActivate(onLocationChangedListener);
            }

            @Override
            public void deactivate() {
                doDeActivate();
            }
        });
        // 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }

    /**
     * 激活定位
     */
    public void doActivate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                        mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                        if(mFirst){
                            mFirst=false;
                            LatLng latLng=new LatLng(aMapLocation.getLatitude(),
                                    aMapLocation.getLongitude());
                            CameraUpdate update=CameraUpdateFactory.
                                    newCameraPosition(new CameraPosition(
                                            latLng, 18, 30, 30));
                            aMap.moveCamera(update);
//                            addMyMarker(latLng);
                        }
                    } else {
                        String errText="定位失败,"+aMapLocation.getErrorCode()
                                +":"+aMapLocation.getErrorInfo();
                        Log.e(TAG,errText);
                    }
                }
            });
            //设置为高精度定位模式
            mLocationOption.setLocationMode(
                    AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    public void doDeActivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 添加自己位置的Marker
     * @param latlng
     */
    private void addMyMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_locked);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

//		BitmapDescriptor des = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        MarkerOptions options = new MarkerOptions();
        options.icon(des);
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
        mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
    }

    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog dialog=new PhoneInputDialog(this);
        dialog.show();
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
                break;
            case IAccountManager.SERVER_FAIL:
                showPhoneInputDialog();
                break;
        }
    }

    @Override
    public void showLoginSuc() {
        ToastUtil.show(this,getString(R.string.login_suc));
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        doDeActivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销Presenter
        RxBus.getInstance().unRegister(mPresenter);

        mapView.onDestroy();
    }
}
