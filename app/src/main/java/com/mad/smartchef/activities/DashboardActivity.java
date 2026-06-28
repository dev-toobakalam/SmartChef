package com.mad.smartchef.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mad.smartchef.R;
import com.mad.smartchef.utils.LocalHelper;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Find views
        ImageView logo = findViewById(R.id.logoImage);
        TextView quote = findViewById(R.id.appQuote);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations
        logo.startAnimation(fadeIn);
        quote.startAnimation(slideUp);

        // Delay and start main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to splash
        }, 3000); // 3-second delay
    }
}