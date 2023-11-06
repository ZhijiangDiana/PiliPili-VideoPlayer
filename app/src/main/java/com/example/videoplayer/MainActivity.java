package com.example.videoplayer;

import android.content.Intent;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public MainActivity() {
        mainActivity = MainActivity.this;
    }
    Toolbar toolbar;
    ImageView userIcon;
    ViewPager2 viewPager;
    TabLayout tabLayout;
    public static final int VISITOR = -1;
    public static final int NORMAL_USER = 0;
    public static final int MEMBERSHIP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // 初始化阶段
        toolbar = findViewById(R.id.main_toolbar);
        userIcon = findViewById(R.id.main_userIcon);

        initTabLayout();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setSupportActionBar(toolbar);
        //todo 读取本地user信息，若登录已过期则将user设为null
        if (Variable.currentUser != null) {
            //todo 若已登录，则标题显示欢迎界面，点击人头按钮将弹出PopupMenu而不是进入登录界面
            toolbar.setTitle("欢迎" + Variable.currentUser.userNickName + "! ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void login(View view) {
        if (Variable.currentUser == null)
            startActivity(new Intent(mainActivity, LoginActivity.class));
    }

    private void initTabLayout() {
        tabLayout = findViewById(R.id.main_tab);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initViewPager() {
        viewPager = findViewById(R.id.main_viewPager);

        List<Fragment> fragmentList = new ArrayList<>(11);
        fragmentList.add(new MainLiveList());
        fragmentList.add(new MainVideoList());

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
        viewPager.setCurrentItem(1);
    }

    public void test(View view) {
        Intent intent = new Intent(MainActivity.mainActivity, LivePlayer.class);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        startActivity(intent, bundle);
    }
}