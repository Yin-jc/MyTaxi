package com.yjc.mytaxi.main.view;

/**
 * Created by Administrator on 2017/11/1/001.
 */

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.account.model.AccountManagerImpl;
import com.yjc.mytaxi.account.model.IAccountManager;
import com.yjc.mytaxi.account.view.PhoneInputDialog;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;
import com.yjc.mytaxi.common.lbs.GaoDeLbsLayerImpl;
import com.yjc.mytaxi.common.lbs.ILbsLayer;
import com.yjc.mytaxi.common.lbs.LocationInfo;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
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
    private ILbsLayer mLbsLayer;


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

            }
        });
        ViewGroup mapViewContainer= (ViewGroup) findViewById(R.id.activity_main);
        mapViewContainer.addView(mLbsLayer.getMapView());
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
