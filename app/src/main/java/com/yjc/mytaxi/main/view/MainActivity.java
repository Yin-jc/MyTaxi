package com.yjc.mytaxi.main.view;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
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
import com.yjc.mytaxi.main.model.Order;
import com.yjc.mytaxi.main.presenter.IMainPresenter;
import com.yjc.mytaxi.main.presenter.MainPresenterImpl;

import org.w3c.dom.Text;

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
                //首次定位，添加当前位置的标记
                mLbsLayer.addOrUpdateMarker(locationInfo,
                        BitmapFactory.decodeResource(getResources(),R.drawable.start));
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
                        // TODO: 2017/11/9/009 支付 
                        break;
                }
            }
        };
        mBtnCall.setOnClickListener(listener);
        mBtnCancel.setOnClickListener(listener);
        mBtnPay.setOnClickListener(listener);
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
        if(mIsLogin){
            //已登录，直接呼叫
            showCalling();
            // 请求呼叫
            mPresenter.callDriver(mPushKey,mCost,mStartLocation,mEndLocation);
        }else {
            //未登录，先登录
            mPresenter.loginByToken();
            mIsLogin=true;
            ToastUtil.show(this,"请先登录");
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
                    DevUtil.closeInputMethod(MainActivity.this);
                    //记录终点
                    mEndLocation=results.get(position);
                    //绘制路径
                    showRoute(mStartLocation,mEndLocation);
                }
            });
            mEndAdapter.notifyDataSetChanged();
    }

    /**
     * 绘制起点终点路径
     * @param mStartLocation
     * @param mEndLocation
     */
    private void showRoute(final LocationInfo mStartLocation, final LocationInfo mEndLocation) {
        mLbsLayer.clearAllMarkers();
        addStartMarker();
        addEndMarker();
        mLbsLayer.driveRoute(mStartLocation, mEndLocation, Color.GREEN,
                new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        LogUtil.d(TAG,"driveRoute:"+result);

                        mLbsLayer.moveCamera(mStartLocation,mEndLocation);
                        //显示操作区
                        showOptArea();
                        mCost=result.getTaxiCost();
                        String infoString=getString(R.string.route_info);
                        infoString= String.format(infoString,
                                (int)result.getDistance(),mCost,result.getDuration());
                        mTips.setVisibility(View.VISIBLE);
                        mTips.setText(infoString);
                    }
                });
    }

    private void showOptArea() {
        mSelectArea.setVisibility(View.GONE);
        mOptArea.setVisibility(View.VISIBLE);
    }

    private void addEndMarker() {
        if(mEndBit==null || mEndBit.isRecycled()){
            mEndBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.end);
        }
        mLbsLayer.addOrUpdateMarker(mEndLocation,mEndBit);
    }

    private void addStartMarker() {
        if(mStartBit==null || mStartBit.isRecycled()){
            mStartBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.start);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation,mStartBit);
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

    @Override
    public void showNears(List<LocationInfo> data) {
        for (LocationInfo locationInfo:data){
            showLocationChange(locationInfo);
        }
    }

    @Override
    public void showLocationChange(LocationInfo locationInfo) {
        if(mDriverBit==null || mDriverBit.isRecycled()){
            mDriverBit=BitmapFactory.decodeResource(getResources(),R.drawable.car);
        }
        mLbsLayer.addOrUpdateMarker(locationInfo,mDriverBit);
    }

    /**
     * 呼叫司机成功
     */
    @Override
    public void showCallDriverSuc() {
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_suc));
    }

    /**
     * 呼叫司机失败
     */
    @Override
    public void showCallDriverFail() {
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_fail));
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
        ToastUtil.show(this,"司机接单了");
        //清除地图标记
        mLbsLayer.clearAllMarkers();
        //添加司机标记
        final LocationInfo driverLocation=new LocationInfo(order.getDriverLatitude(),
                order.getDriverLongitude());
        showLocationChange(driverLocation);
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
                    }
                });
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
        hideOptAreaAndShowSelectArea();
    }

    private void hideOptAreaAndShowSelectArea() {
        mOptArea.setVisibility(View.GONE);
        mSelectArea.setVisibility(View.VISIBLE);
    }

    private void addLocationMarker() {
        if(mLocationBit==null || mLocationBit.isRecycled()){
            mLocationBit=BitmapFactory.decodeResource(getResources(),
                    R.drawable.navi_map_gps_locked);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation,mLocationBit);
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
}
