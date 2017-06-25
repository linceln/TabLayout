package com.xyz.tablayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.xyz.library.TabLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MainFragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                fragment.setArguments(bundle);
                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }
        });

        // 初始化
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setViewPager(viewPager);

        // 默认页码
        tabLayout.setCurrentItem(2);

        // 设置渐变色
        int[] color = new int[]{Color.MAGENTA, Color.RED, Color.GREEN, Color.CYAN};
        tabLayout.setShaderColors(color);

        // 设置ViewPager滑动监听
        tabLayout.setOnPageChangeListener(new TabLayout.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:

                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:

                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:

                        break;
                }
            }
        });
    }
}
