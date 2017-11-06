package com.yjc.mytaxi.common.storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/11/6/006.
 * SharedPreference数据访问对象
 */

public class SharedPreferenceDao {
    private static final String TAG="SharedPreferenceDao";
    public static final String FILE_ACCOUNT="FILE_ACCOUNT";
    public static final String KEY_ACCOUNT="KEY_ACCOUNT";
    private SharedPreferences sharedPreferences;
    /**
     * 初始化
     */
    public SharedPreferenceDao(Application application,String fileName){
        sharedPreferences=application.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    /**
     * 保存K-V
     */
    public void save(String key,String value){
        sharedPreferences.edit().putString(key,value).commit();
    }
    /**
     * 读取K-V
     */
    public String get(String key){
        return sharedPreferences.getString(key,null);
    }
    /**
     * 保存对象
     */
    public void save(String key,Object object){
        String value=new Gson().toJson(object);
        save(key,value);
    }
    /**
     * 读取对象
     */
    public Object get(String key,Class cls){
        String value=get(key);
        try {
            if(value!=null){
                Object o=new Gson().fromJson(value,cls);
                return o;
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return null;
    }
}
