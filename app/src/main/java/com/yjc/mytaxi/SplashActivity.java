package com.yjc.mytaxi;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

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
    }
}
