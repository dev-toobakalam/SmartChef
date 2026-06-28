package com.mad.smartchef.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.mad.smartchef.Fragments.FavoritesFragment;
import com.mad.smartchef.Fragments.SearchFragment;
import com.mad.smartchef.Fragments.SettingFragment;
import com.mad.smartchef.Fragments.ShoppingListFragment;
import com.mad.smartchef.R;
import com.mad.smartchef.utils.LocalHelper;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private View homeView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.background_dark));

            int flags = window.getDecorView().getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }

        homeView = getLayoutInflater().inflate(R.layout.home_layout, null);

        // Explicitly clear any existing tabs before adding fresh ones.
        // TabLayout can retain stale TabView text after a locale change /
        // recreate() unless tabs are removed and re-added explicitly.
        tabLayout.removeAllTabs();

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home).setText(R.string.home));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search).setText(R.string.search));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_favorite).setText(R.string.favorites));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_shopping).setText(R.string.shopping_list));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_setting).setText(R.string.settings));

        showHome();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 0) {
                    showHome();
                } else if (pos == 1) {
                    replaceFragment(new SearchFragment());
                } else if (pos == 2) {
                    replaceFragment(new FavoritesFragment());
                } else if (pos == 3) {
                    replaceFragment(new ShoppingListFragment());
                } else if (pos == 4) {
                    replaceFragment(new SettingFragment());
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showHome() {
        getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        View frame = findViewById(R.id.frameLayout);
        if (frame instanceof android.view.ViewGroup) {
            android.view.ViewGroup container = (android.view.ViewGroup) frame;
            container.removeAllViews();
            container.addView(homeView);
            ImageView imageView = homeView.findViewById(R.id.imageView);
            Glide.with(this)
                    .load("https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=80")
                    .into(imageView);
        }
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        View frame = findViewById(R.id.frameLayout);
        if (frame instanceof android.view.ViewGroup) {
            android.view.ViewGroup container = (android.view.ViewGroup) frame;
            container.removeAllViews();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }
}