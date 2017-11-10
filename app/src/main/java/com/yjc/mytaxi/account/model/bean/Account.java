package com.yjc.mytaxi.account.model.bean;

/**
 * Created by Administrator on 2017/11/6/006.
 * 账户信息返回格式
 */

public class Account {
    private String token;
    private String uid;
    private String account;
    private String expired;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }
}
