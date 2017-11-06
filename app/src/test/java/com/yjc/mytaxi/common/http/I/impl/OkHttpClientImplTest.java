package com.yjc.mytaxi.common.http.I.impl;

import com.yjc.mytaxi.common.http.IHttpClient;
import com.yjc.mytaxi.common.http.IRequest;
import com.yjc.mytaxi.common.http.IResponse;
import com.yjc.mytaxi.common.http.api.API;
import com.yjc.mytaxi.common.http.impl.BaseRequest;
import com.yjc.mytaxi.common.http.impl.OkHttpClientImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Administrator on 2017/10/31/031.
 */
public class OkHttpClientImplTest {
    IHttpClient httpClient;
    @Before
    public void setUp() throws Exception {
        httpClient=new OkHttpClientImpl();
        API.Config.setDebug(false);
    }

    @Test
    public void get() throws Exception {
        //request对象
        String url=API.Config.getDomain()+API.TEST_GET;
        IRequest request=new BaseRequest(url);

        request.setBody("uid","12345");
        request.setHeader("testHeader","test header");
        IResponse response=httpClient.get(request,false);
        System.out.println("startCode="+response.getCode());
        System.out.println("body="+response.getData());
    }

    @Test
    public void post() throws Exception {
        //request对象
        String url=API.Config.getDomain()+API.TEST_POST;
        IRequest request=new BaseRequest(url);

        request.setBody("uid","12345");
        request.setHeader("testHeader","test header");
        IResponse response=httpClient.post(request,false);
        System.out.println("startCode="+response.getCode());
        System.out.println("body="+response.getData());
    }

}