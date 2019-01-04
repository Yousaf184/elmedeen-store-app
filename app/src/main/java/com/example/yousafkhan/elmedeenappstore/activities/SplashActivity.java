package com.example.yousafkhan.elmedeenappstore.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yousafkhan.elmedeenappstore.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_DELAY = 1200;
    private boolean splashScreenShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!splashScreenShown) {

            setContentView(R.layout.activity_splash);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent openHomeActivity = new Intent(SplashActivity.this , HomeActivity.class);
                    startActivity(openHomeActivity);

                }
            }, SPLASH_DISPLAY_DELAY);

            splashScreenShown = true;

        } else {
            Intent openHomeActivity = new Intent(this, HomeActivity.class);
            startActivity(openHomeActivity);
        }
    }
}
