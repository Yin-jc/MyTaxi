package com.yjc.mytaxi.common.dataBus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2017/11/7/007.
 * 自定义注解，用于标记观察者的方法
 */

@Target(ElementType.METHOD) //方法
@Retention(RetentionPolicy.RUNTIME)  //运行时有效
@Documented
public @interface RegisterBus {
}
