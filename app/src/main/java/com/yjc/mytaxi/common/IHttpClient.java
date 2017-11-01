package com.yjc.mytaxi.common;

/**
 * Created by Administrator on 2017/10/31/031.
 */

public interface IHttpClient {
    IResponse get(IRequest request, boolean forceCache);
    IResponse post(IRequest request,boolean forceCache);
}
