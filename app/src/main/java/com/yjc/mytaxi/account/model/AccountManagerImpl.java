package com.yjc.mytaxi.account.model;

import android.util.Log;

import com.google.gson.Gson;
import com.yjc.mytaxi.MyTaxiApplication;
import com.yjc.mytaxi.account.model.bean.Account;
import com.yjc.mytaxi.account.model.response.LoginResponse;
import com.yjc.mytaxi.common.dataBus.RxBus;
import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.http.impl.BaseResponse;
import com.yjc.mytaxi.common.storage.SharedPreferenceDao;
import com.yjc.mytaxi.common.util.DevUtil;
import com.yjc.mytaxi.common.util.LogUtil;

import rx.functions.Func1;

/**
 * Created by Administrator on 2017/11/7/007.
 */

public class AccountManagerImpl implements IAccountManager {

    private static final String TAG="AccountManagerImpl";
    //登录是否过期
    private boolean tokenValid=false;

    //网络请求库
    private IHttpClient httpClient;
    //数据存储
    private SharedPreferenceDao sharedPreferenceDao;

    public AccountManagerImpl(IHttpClient httpClient,
                              SharedPreferenceDao sharedPreferenceDao) {
        this.httpClient = httpClient;
        this.sharedPreferenceDao = sharedPreferenceDao;
    }

    /**
     * 获取验证码
     * @param phone
     */
    @Override
    public void fetchSMSCode(final String phone) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url= API.Config.getDomain()+API.GET_SMS_CODE;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",phone);

                IResponse response=httpClient.get(request,false);
                LogUtil.d(TAG,response.getData());
                BaseBizResponse bizRes=new BaseBizResponse();
                if(response.getCode()== BaseResponse.STATE_OK){
                    bizRes=new Gson()
                            .fromJson(response.getData(),BaseBizResponse.class);
                    if(bizRes.getCode()==BaseBizResponse.STATE_OK){
                        bizRes.setCode(SMS_SEND_SUC);
                    } else {
                        bizRes.setCode(SMS_SEND_FAIL);
                    }
                }else {
                    bizRes.setCode(SERVER_FAIL);
                }
                return bizRes;
            }
        });
    }

    /**
     * 校验验证码
     * @param phone
     * @param smsCode
     */
    @Override
    public void checkSMSCode(final String phone, final String smsCode) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url= API.Config.getDomain()+API.CHECK_SMS_CODE;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",phone);
                request.setBody("code",smsCode);

                IResponse response=httpClient.get(request,false);
                LogUtil.d(TAG,response.getData());
                BaseBizResponse bizRes=new BaseBizResponse();
                if(response.getCode()== BaseResponse.STATE_OK){
                    bizRes=new Gson()
                            .fromJson(response.getData(),BaseBizResponse.class);
                    if(bizRes.getCode()==BaseBizResponse.STATE_OK){
                        bizRes.setCode(SMS_CHECK_SUC);
                    } else {
                        bizRes.setCode(SMS_CHECK_FAIL);
                    }
                }else {
                    bizRes.setCode(SERVER_FAIL);
                }
                return bizRes;
            }
        });

    }


    /**
     * 检查用户是否存在
     * @param phone
     */
    @Override
    public void checkUserExist(final String phone) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url= API.Config.getDomain()+API.CHECK_USER_EXIST;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",phone);

                IResponse response=httpClient.get(request,false);
                Log.d(TAG,response.getData());
                BaseBizResponse bizRes=new BaseBizResponse();
                if(response.getCode()== BaseResponse.STATE_OK){
                    bizRes=new Gson()
                            .fromJson(response.getData(),BaseBizResponse.class);
                    if(bizRes.getCode()==BaseBizResponse.STATE_USER_EXIST){
                        bizRes.setCode(USER_EXIST);
                    } else if(bizRes.getCode()==BaseBizResponse.STATE_USER_NOT_EXIST){
                        bizRes.setCode(USER_NOT_EXIST);
                    }
                }else {
                    bizRes.setCode(SERVER_FAIL);
                }
                return bizRes;
            }
        });
    }

    /**
     * 注册
     * @param phone
     * @param password
     */
    @Override
    public void register(final String phone, final String password) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url= API.Config.getDomain()+API.REGISTER;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",phone);
                request.setBody("password",password);
                request.setBody("uid", DevUtil.UUID(MyTaxiApplication.getInstance()));

                IResponse response=httpClient.post(request,false);
                Log.d(TAG,response.getData());
                BaseBizResponse bizRes=new BaseBizResponse();
                if(response.getCode()== BaseResponse.STATE_OK){
                    bizRes=new Gson()
                            .fromJson(response.getData(),BaseBizResponse.class);
                    if(bizRes.getCode()==BaseBizResponse.STATE_OK){
                        bizRes.setCode(REGISTER_SUC);
                    } else {
                        bizRes.setCode(SERVER_FAIL);
                    }
                }else {
                    bizRes.setCode(SERVER_FAIL);
                }
                return bizRes;
            }
        });
    }

    /**
     * 登录
     * @param phone
     * @param password
     */
    @Override
    public void login(final String phone, final String password) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {

                String url= API.Config.getDomain()+API.LOGIN;
                IRequest request=new BaseRequest(url);
                request.setBody("phone",phone);
                request.setBody("password",password);

                IResponse response=httpClient.post(request,false);
                Log.d(TAG,response.getData());
                LoginResponse loginRes=new LoginResponse();
                if(response.getCode()== BaseResponse.STATE_OK){
                    loginRes=new Gson().fromJson(response.getData(),LoginResponse.class);
                    if(loginRes.getCode()==BaseBizResponse.STATE_OK) {
                        //保存登录信息
                        Account account = loginRes.getData();
                        // TODO: 2017/11/7/007 加密存储
                        SharedPreferenceDao dao =
                                new SharedPreferenceDao(MyTaxiApplication.getInstance()
                                        , SharedPreferenceDao.FILE_ACCOUNT);
                        dao.save(SharedPreferenceDao.KEY_ACCOUNT, account);
                        loginRes.setCode(LOGIN_SUC);
                    }
                    if(loginRes.getCode()==BaseBizResponse.STATE_PW_ERROR){
                        loginRes.setCode(PW_ERROR);
                    }
                }else {
                    loginRes.setCode(SERVER_FAIL);
                }
                return loginRes;
            }
        });
    }

    /**
     * token登录
     */
    @Override
    public void loginByToken() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                // 获取本地登录信息
                SharedPreferenceDao dao=new SharedPreferenceDao(MyTaxiApplication.getInstance(),
                        SharedPreferenceDao.FILE_ACCOUNT);
                Account account= (Account) dao.get(SharedPreferenceDao.KEY_ACCOUNT,Account.class);

                // 检查token是否过期
                if(account!=null){
                    if(Long.parseLong(account.getExpired()) > System.currentTimeMillis()){
                        //token有效
                        tokenValid=true;
                    }
                }

                LoginResponse loginRes=new LoginResponse();

                if(!tokenValid){
                    loginRes.setCode(TOKEN_INVALID);
                    return loginRes;

                }else {
                    //  请求网络，完成自动登录
                    String url= API.Config.getDomain()+API.LOGIN_BY_TOKEN;
                    IRequest request=new BaseRequest(url);
                    request.setBody("token",account.getToken());

                    IResponse response=httpClient.post(request,false);
                    Log.d(TAG,response.getData());
                    if(response.getCode()== BaseResponse.STATE_OK){
                        loginRes=new Gson().fromJson(response.getData(),LoginResponse.class);
                        if(loginRes.getCode()== BaseBizResponse.STATE_OK) {
                            //保存登录信息
                            account = loginRes.getData();
                            // TODO: 2017/11/7/007 加密存储
                            dao = new SharedPreferenceDao(MyTaxiApplication.getInstance()
                                            , SharedPreferenceDao.FILE_ACCOUNT);
                            dao.save(SharedPreferenceDao.KEY_ACCOUNT, account);
                            loginRes.setCode(LOGIN_SUC);
                        }
                        //token过期
                        if(loginRes.getCode()==BaseBizResponse.STATE_TOKEN_INVALID){
                            loginRes.setCode(TOKEN_INVALID);
                        }
                    }else {
                        loginRes.setCode(SERVER_FAIL);
                    }
                    return loginRes;
                }
            }
        });

    }

    @Override
    public boolean isLogin() {
        //获取本地登录信息
        Account account= (Account) sharedPreferenceDao.get(SharedPreferenceDao.KEY_ACCOUNT,
                Account.class);
        //登录是否过期
        boolean tokenValid=false;
        if(account!=null){
            if(Long.parseLong(account.getExpired())>System.currentTimeMillis()){
                //token有效
                tokenValid=true;
            }
        }
        return tokenValid;
    }
}



