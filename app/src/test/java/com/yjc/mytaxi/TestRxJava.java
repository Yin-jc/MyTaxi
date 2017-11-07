package com.yjc.mytaxi;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/11/7/007.
 */

public class TestRxJava {
    @Before
    public void setUp(){
        Thread.currentThread().setName("currentThread");
    }
    @Test
    public void textSubscribe(){

        //观察者/订阅者
        final Subscriber<String> subscriber=new Subscriber<String>() {

            @Override
            public void onNext(String s) {
                System.out.println("onNext in thread:"+
                Thread.currentThread().getName());
                System.out.println(s);
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted in thread:"+
                        Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError in thread:"+
                Thread.currentThread().getName());
                t.printStackTrace();
            }
        };

        //被观察者
        Observable observable=Observable.create(
                new Observable.OnSubscribe<Subscriber>() {
            @Override
            public void call(Subscriber subscriber1) {
                //发生事件
                System.out.println("call in thread:"+
                        Thread.currentThread().getName());
                subscriber1.onStart();
                subscriber1.onNext("hello world");
                subscriber1.onCompleted();
            }
        });

        //订阅
        observable.subscribe(subscriber);
    }



    @Test
    public void testScheduler(){
        //观察者/订阅者
        final Subscriber<String> subscriber=new Subscriber<String>() {

            @Override
            public void onNext(String s) {
                System.out.println("onNext in thread:"+
                        Thread.currentThread().getName());
                System.out.println(s);
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted in thread:"+
                        Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError in thread:"+
                        Thread.currentThread().getName());
                t.printStackTrace();
            }
        };

        //被观察者
        Observable observable=Observable.create(
                new Observable.OnSubscribe<Subscriber>() {
            @Override
            public void call(Subscriber subscriber1) {
                //发生事件
                System.out.println("call in thread:"+
                        Thread.currentThread().getName());
                subscriber1.onStart();
                subscriber1.onNext("hello world");
                subscriber1.onCompleted();
            }
        });

        //订阅
        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(subscriber);

    }

    @Test
    public void testMap(){
        String name="yjc";
        Observable.just(name)
                .subscribeOn(Schedulers.newThread())
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

                .subscribeOn(Schedulers.newThread())
                .map(new Func1<User, Object>() {
                    @Override
                    public Object call(User user) {
                        System.out.println("process User call in thread:"
                                +Thread.currentThread().getName());
                        return user;
                    }
                })

                .observeOn(Schedulers.newThread())
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
