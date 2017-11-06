package com.yjc.mytaxi.common.http.api;

/**
 * Created by Administrator on 2017/10/31/031.
 */

public class API {

    public static final String TEST_GET="/get?uid=${uid}";
    public static final String TEST_POST="/post";
    //获取验证码
    public static final String GET_SMS_CODE=
            "/f34e28da5816433d/getMsgCode?phone=${phone}";
    //校验验证码
    public static final String CHECK_SMS_CODE =
            "/f34e28da5816433d/checkMsgCode?phone=${phone}&code=${code}" ;


    /**
     * 配置域名信息
     */
    public static class Config{
        //测试环境的域名
        private static final String TEST_DOMAIN="http://cloud.bmob.cn";
        //发布环境的域名
        private static final String RELEASE_DOMAIN="http://cloud.bmob.cn";
        private static final String TEST_APP_ID="e90928398db0130b0d6d21da7bde357e";
        private static final String RELEASE_APP_ID="e90928398db0130b0d6d21da7bde357e";
        private static final String TEST_APP_KEY="514d8f8a2371bdf1566033f6664a24d2";
        private static final String RELEASE_APP_KEY="514d8f8a2371bdf1566033f6664a24d2";
        private static String appId=TEST_APP_ID;
        private static String appKey=TEST_APP_KEY;
        private static String domain=TEST_DOMAIN;

        public static void setDebug(boolean debug){
            domain=debug?TEST_DOMAIN:RELEASE_DOMAIN;
            appId=debug?TEST_APP_ID:RELEASE_APP_ID;
            appKey=debug?TEST_APP_KEY:RELEASE_APP_KEY;
        }
        public static String getDomain(){
            return domain;
        }
        public static String getAppId(){
            return appId;
        }
        public static String getAppKey(){
            return appKey;
        }
    }
}
