package com.yjc.mytaxi.common.util;

import android.media.MediaCodec;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/1/001.
 */

public class FormaUtil {
    public static boolean checkMobile(String mobile){
        String regex="(\\+\\d+)?1[3458]\\d{9}$";
        return Pattern.matches(regex,mobile);
    }
}
