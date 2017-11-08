package com.yjc.mytaxi.common.lbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapException;
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
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.yjc.mytaxi.R;
import com.yjc.mytaxi.common.util.SensorEventHelper;
import com.yjc.mytaxi.main.view.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/8/008.
 */

public class GaoDeLbsLayerImpl implements ILbsLayer {
    private static final String TAG="GaoDeLbsLayerImpl";
    private static final String KEY_MY_MARKERE="1000";
    private Context mContext;
    //地图视图对象
    private MapView mapView;
    //地图模型对象
    private AMap aMap;
    //地图位置变化回调对象
    private LocationSource.OnLocationChangedListener mMapLocationChangeListener;
    //位置定位对象
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private SensorEventHelper mSensorHelper;
    private boolean mFirst=true;
    private CommonLocationChangeListener mLocationChangeListener;
    private MyLocationStyle mMyLocationStyle;
    //管理地图标记集合
    private Map<String,Marker> markerMap=new HashMap<>();
    //当前城市
    private String mCity;

    public GaoDeLbsLayerImpl(Context context) {
        mContext = context;
        //创建地图对象
        mapView=new MapView(context);
        //获取地图模型
        aMap=mapView.getMap();
        //创建定位对象
        mLocationClient=new AMapLocationClient(context);
        mLocationOption=new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //传感器对象
        mSensorHelper=new SensorEventHelper(context);
        mSensorHelper.registerSensorListener();
    }

    @Override
    public View getMapView() {
        return mapView;
    }

    @Override
    public void setLocationChangeListener(CommonLocationChangeListener locationChangeListener) {
        mLocationChangeListener=locationChangeListener;
    }

    @Override
    public void setLocationRes(int res) {
//         自定义系统定位小蓝点
        mMyLocationStyle = new MyLocationStyle();
        mMyLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(res));// 设置小蓝点的图标
        mMyLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        mMyLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // mMyLocationStyle.anchor(int,int)//设置小蓝点的锚点
        mMyLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
    }

    @Override
    public void addOrUpdateMarker(LocationInfo locationInfo, Bitmap bitmap) {
        Marker storedMarker=markerMap.get(locationInfo.getKey());
        LatLng latLng=new LatLng(locationInfo.getLatitude(),
                locationInfo.getLongitude());
        if(storedMarker!=null){
//            Log.d(TAG,"addOrUpdateMarker is not null");
            //如果已经存在则更新角度和位置
            storedMarker.setPosition(latLng);
            storedMarker.setRotateAngle(locationInfo.getRotation());
        }else {
//            Log.d(TAG,"addOrUpdateMarker is null");
            //如果不存在则创建
            MarkerOptions options=new MarkerOptions();
            BitmapDescriptor des=BitmapDescriptorFactory.fromBitmap(bitmap);
            options.icon(des);
            options.anchor(0.5f,0.5f);
            options.position(latLng);
            Marker marker=aMap.addMarker(options);
            marker.setRotateAngle(locationInfo.getRotation());
            markerMap.put(locationInfo.getKey(),marker);
            if(locationInfo.getKey().equals(KEY_MY_MARKERE)){
                //传感器控制我的位置标记旋转角度
                mSensorHelper.setCurrentMarker(marker);
            }
        }
    }

    @Override
    public String getCity() {
        return mCity;
    }

    @Override
    public void poiSearch(String key, final OnSearchedListener listener) {
        if(!TextUtils.isEmpty(key)){
            //组装关键字
            InputtipsQuery inputtipsQuery=new InputtipsQuery(key,"");
            final Inputtips inputTips=new Inputtips(mContext,inputtipsQuery);
            //开始异步搜索
            inputTips.requestInputtipsAsyn();
            //监听处理搜索结果
            inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
                @Override
                public void onGetInputtips(List<Tip> list, int rCode) {
                    if(rCode== com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS) {
                        //正确返回解析结果
                        List<LocationInfo> locationInfos =
                                new ArrayList<>();
                        for (int i=0;i<list.size();i++){
                            Tip tip=list.get(i);
                            LocationInfo locationInfo=new LocationInfo(
                                    tip.getPoint().getLatitude(),
                                    tip.getPoint().getLongitude()
                            );
                            locationInfo.setName(tip.getName());
                            locationInfos.add(locationInfo);
                        }
                        listener.onSearched(locationInfos);
                    }else {
                        listener.onError(rCode);
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle state) {
        mapView.onCreate(state);
        //设置当前位置的图标
        setLocationRes(R.drawable.location_marker);
        setUpMap();
    }

    private void setUpMap() {
        if(mMyLocationStyle!=null){
            aMap.setMyLocationStyle(mMyLocationStyle);
        }
        //设置地图激活（加载监听）
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                mMapLocationChangeListener=onLocationChangedListener;
                Log.d(TAG,"activate");
            }

            @Override
            public void deactivate() {
                if (mLocationClient != null) {
                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
                }
                mLocationClient = null;
            }
        });
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
    }

    private void setUpLocation() {
        //设置监听器
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                //定位位置变化
                if(mMapLocationChangeListener!=null){
                    //当前城市
                    mCity=aMapLocation.getCity();
                    //地图已经激活，通知蓝点实时更新
                    //显示系统小蓝点
                    //位置变更逻辑在此接口的实现类中完成
                    mMapLocationChangeListener.onLocationChanged(aMapLocation);
                    Log.d(TAG,"onLocationChanged");
                    LocationInfo locationInfo=new LocationInfo(
                            aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    locationInfo.setName(aMapLocation.getPoiName());
                    locationInfo.setKey(KEY_MY_MARKERE);
                    //第一次定位
                    if(mFirst){
                        mFirst=false;
                        LatLng latLng=new LatLng(aMapLocation.getLatitude(),
                                aMapLocation.getLongitude());

                        CameraUpdate update= CameraUpdateFactory.newCameraPosition(
                                new CameraPosition(latLng,18,30,30));
                        //移动到当前位置
                        aMap.moveCamera(update);
                        if(mLocationChangeListener!=null){
                            mLocationChangeListener.onLocation(locationInfo);
                        }
                    }
                    if(mLocationChangeListener!=null){
                        mLocationChangeListener.onLocationChange(locationInfo);
                    }
                }
            }
        });
        //启动定位
        mLocationClient.startLocation();
    }


    @Override
    public void onResume() {
        mapView.onResume();
        setUpLocation();
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mapView.onPause();
        mLocationClient.stopLocation();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        mLocationClient.onDestroy();
    }
}

