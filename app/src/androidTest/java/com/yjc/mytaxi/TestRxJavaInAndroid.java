package com.yjc.mytaxi;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/11/7/007.
 */

@RunWith(AndroidJUnit4.class)
public class TestRxJavaInAndroid {
    
    @Test
    public void testMapInAndroid(){
        String name="yjc";
        Observable.just(name)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, User>() {
                    @Override
                    public User call(String name) {
                        User user=new User();
                        user.setName(name);
                        System.out.println("process User call in thread:"
                                +Thread.currentThread().getName());
                        return user;
                    }
                })

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object data) {
                        System.out.println("receive User call in thread:"
                                +Thread.currentThread().getName());
                    }
                });
    }

    public static class User{
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
