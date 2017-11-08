package com.yjc.mytaxi.main.view;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
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
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.ToastUtil;
import com.yjc.mytaxi.main.model.IMainManager;
import com.yjc.mytaxi.main.model.MainManagerImpl;
import com.yjc.mytaxi.main.presenter.IMainPresenter;
import com.yjc.mytaxi.main.presenter.MainPresenterImpl;

import org.w3c.dom.Text;

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
    }

    /**
     * 更新POI列表
     * @param results
     */
    private void updatePoiList(List<LocationInfo> results) {

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
