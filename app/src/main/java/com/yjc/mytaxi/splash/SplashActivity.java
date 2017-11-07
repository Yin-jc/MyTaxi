package com.yjc.mytaxi.splash;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.yjc.mytaxi.R;
import com.yjc.mytaxi.main.view.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //API>=21
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            final AnimatedVectorDrawable anim= (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.anim);
            final ImageView logo= (ImageView) findViewById(R.id.logo);
            logo.setImageDrawable(anim);
            anim.start();
        }

        /**
         * 延时3秒然后跳转到main页面
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        },3000);
    }

}
