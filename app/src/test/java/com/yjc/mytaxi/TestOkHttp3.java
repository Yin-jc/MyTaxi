package com.yjc.mytaxi;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/31/031.
 */

public class TestOkHttp3 {
    /**
     * 测试GET方法
     */
    @Test
    public void testGet(){
        //创建OkHttpClient对象
        OkHttpClient client=new OkHttpClient();
        //创建Request对象
        Request request=new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();
        //OkHttpClient执行Request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("Response:"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试POST请求
     */
    @Test
    public void testPost(){
        //创建OkHttpClient对象
        OkHttpClient client=new OkHttpClient();
        //创建Request对象
        MediaType mediaType=MediaType.parse("application/json;charset=utf-8");
        RequestBody body=RequestBody.create(mediaType,"{\"name\":\"yjc\"}");
        Request request=new Request.Builder()
                .url("http://httpbin.org/post")  //请求行
//                .header()  //请求头
                .post(body)  //请求体
                .build();
        //OkHttpClient执行Request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("Response:"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试拦截器
     */
    @Test
    public void testInterceptor(){
        Interceptor interceptor=new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                long start=System.currentTimeMillis();
                Request request=chain.request();
                Response response=chain.proceed(request);
                long end=System.currentTimeMillis();
                System.out.println("interceptor:cost time"+(end-start));
                return response;
            }
        };
        //创建OkHttpClient对象
        OkHttpClient client=new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        //创建Request对象
        Request request=new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();
        //OkHttpClient执行Request
        try {
            Response response=client.newCall(request).execute();
            System.out.println("Response:"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试缓存
     */
    @Test
    public void testCache(){
        //创建缓存对象
        Cache cache=new Cache(new File("Cache.cache"),1024*1024);

        //创建OkHttpClient对象
        OkHttpClient client=new OkHttpClient.Builder()
                .cache(cache)
                .build();
        //创建Request对象
        Request request=new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();
        //OkHttpClient执行Request
        try {
            Response response=client.newCall(request).execute();
            Response reponseFromCache=response.cacheResponse();
            Response responseFromNet=response.networkResponse();
            if(reponseFromCache!=null){
                //从缓存响应
                System.out.println("response from cache");
            }else if(responseFromNet!=null){
                //从网络响应
                System.out.println("response from net");
            }
            System.out.println("Response:"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
