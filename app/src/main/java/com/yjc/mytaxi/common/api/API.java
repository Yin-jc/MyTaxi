package com.yjc.mytaxi.common.api;

/**
 * Created by Administrator on 2017/10/31/031.
 */

public class API {

    public static final String TEST_GET="/get?uid=${uid}";
    public static final String TEST_POST="/post";

    /**
     * 配置域名信息
     */
    public static class Config{
        //测试环境的域名
        public static final String TEST_DOMAIN="http://httpbin.org";
        //发布环境的域名
        public static final String RELEASE_DOMAIN="http://httpbin.org";
        private static String domain=TEST_DOMAIN;

        public static void setDebug(boolean debug){
            domain=debug?TEST_DOMAIN:RELEASE_DOMAIN;
        }
        public static String getDomain(){
            return domain;
        }
    }
}
