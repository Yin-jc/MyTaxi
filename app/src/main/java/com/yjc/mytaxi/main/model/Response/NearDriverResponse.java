package com.yjc.mytaxi.main.model.Response;

import com.yjc.mytaxi.common.http.biz.BaseBizResponse;
import com.yjc.mytaxi.common.lbs.LocationInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/11/8/008.
 */

public class NearDriverResponse extends BaseBizResponse{

    List<LocationInfo> data;

    public List<LocationInfo> getData() {
        return data;
    }

    public void setData(List<LocationInfo> data) {
        this.data = data;
    }
}
