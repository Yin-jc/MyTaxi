package com.yjc.mytaxi.common.impl;

import com.yjc.mytaxi.common.IHttpClient;
import com.yjc.mytaxi.common.IRequest;
import com.yjc.mytaxi.common.IResponse;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/31/031.
 * OkHttp的实现
 */

public class OkHttpClientImpl implements IHttpClient{

    OkHttpClient mOkHttpClient=new OkHttpClient.Builder()
            .build();


    @Override
    public IResponse get(IRequest request, boolean forceCache) {
        /**
         * 解析业务参数,构造request
         */
        request.setMethod(IRequest.GET);
        //解析头部
        Map<String,String> header=request.getHeader();

        Request.Builder builder=new Request.Builder();
        for (String key:header.keySet()){
            //组装成OkHttp3的Header
            builder.header(key,header.get(key));
        }
        //获取url
        String url=request.getUrl();
        builder.url(url).get();
        Request okRequest=builder.build();
        return execute(okRequest);
    }


    @Override
    public IResponse post(IRequest request, boolean forceCache) {
        request.setMethod(IRequest.POST);
        MediaType mediaType=MediaType.parse("application/json;charset=utf-8");
        RequestBody body= RequestBody.create(mediaType,request.getBody().toString());
        Map<String,String> header=request.getHeader();
        Request.Builder builder=new Request.Builder();
        for (String key:header.keySet()){
            builder.header(key,header.get(key));
        }
        String url=request.getUrl();
        builder.url(url).post(body);
        Request okRequest=builder.build();
        return execute(okRequest);
    }

    /**
     * 请求执行过程
     * @param request
     * @return
     */
    private IResponse execute(Request request) {
        /**
         * 解析业务参数，构造request
         */
        BaseResponse commonResponse=new BaseResponse();
        try {
            Response response=mOkHttpClient.newCall(request).execute();
            //设置状态码
            commonResponse.setCode(response.code());
            String body=response.body().string();
            //设置响应数据
            commonResponse.setData(body);
        } catch (IOException e) {
            e.printStackTrace();
            commonResponse.setCode(commonResponse.STATE_UNKNOWN_ERROR);
            commonResponse.setData(e.getMessage());
        }
        return commonResponse;
    }
}
