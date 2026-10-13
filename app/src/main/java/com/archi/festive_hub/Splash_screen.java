
package com.archi.festive_hub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_screen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing() && !isDestroyed()) {
                Intent intent = new Intent(
                        Splash_screen.this,
                        activity_signup.class
                );

                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        // Initialize splash screen views
        ImageView logo = findViewById(R.id.imgSplashLogo);
        TextView appName = findViewById(R.id.tvSplashAppName);
        TextView tagline = findViewById(R.id.tvSplashTagline);

        // Logo: fade-in + scale-up animation
        AnimationSet logoAnimation = new AnimationSet(true);

        AlphaAnimation logoFade = new AlphaAnimation(0f, 1f);
        logoFade.setDuration(1000);

        ScaleAnimation logoScale = new ScaleAnimation(
                0.5f, 1.0f,
                0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        logoScale.setDuration(1000);

        logoAnimation.addAnimation(logoFade);
        logoAnimation.addAnimation(logoScale);

        logo.startAnimation(logoAnimation);

        // App name fade-in
        AlphaAnimation nameFade = new AlphaAnimation(0f, 1f);
        nameFade.setDuration(1000);
        nameFade.setStartOffset(300);

        appName.startAnimation(nameFade);

        // Tagline fade-in
        AlphaAnimation taglineFade = new AlphaAnimation(0f, 1f);
        taglineFade.setDuration(1000);
        taglineFade.setStartOffset(600);

        tagline.startAnimation(taglineFade);

        // Navigate to signup screen after 2.5 seconds
        handler.postDelayed(navigateRunnable, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }
}
