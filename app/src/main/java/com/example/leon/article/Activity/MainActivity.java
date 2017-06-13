package com.example.leon.article.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.leon.article.Activity.art.ArtConstant;
import com.example.leon.article.R;
import com.example.leon.article.fragment.ArticleFragment;
import com.example.leon.article.fragment.HomeFragment;
import com.example.leon.article.fragment.MoreFragment;
import com.example.leon.article.fragment.VipFragment;
import com.example.leon.article.utils.CommonUtils;
import com.example.leon.article.utils.Constant;
import com.example.leon.article.utils.SPUtil;
import com.example.leon.article.widget.BottomNavigationViewHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FragmentPagerAdapter mAdapter;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ViewPager viewpager;
    private BottomNavigationView navigationview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Long lastUsingTime = (Long) SPUtil.get(Constant.Share_prf.LAST_USING_TIME, 0L);
        long currentTimeMillis = System.currentTimeMillis();
        if (lastUsingTime != 0 && currentTimeMillis - lastUsingTime > 24 * 60 * 60 * 1000) {
            SPUtil.clear();
        }
        //记录上次使用时间
        SPUtil.put(Constant.Share_prf.LAST_USING_TIME, currentTimeMillis);

        initView();
        ifShowArticle();
        initEvent();
    }

    private void initEvent() {
        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        navigationview.setSelectedItemId(R.id.menu_home);
                        break;
                    case 1:
                        navigationview.setSelectedItemId(R.id.menu_article);
                        break;
                    case 2:
                        navigationview.setSelectedItemId(R.id.menu_vip);
                        break;
                    case 3:
                        navigationview.setSelectedItemId(R.id.menu_more);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //发表文章后跳转到当前页面
    private void ifShowArticle() {
        int position = getIntent().getIntExtra(ArtConstant.SHOW_ARTICLEFRAGMENT, 0);
        viewpager.setCurrentItem(position);
        if (position == 0) {
            navigationview.setSelectedItemId(R.id.menu_home);
        }
        if (position == 1) {
            navigationview.setSelectedItemId(R.id.menu_article);
        }
    }


    private void initView() {

        initfragments();
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        navigationview = (BottomNavigationView) findViewById(R.id.navigationview);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationview.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            // set your height here
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    CommonUtils.dip2px(this,13), displayMetrics);
            // set your width here
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    CommonUtils.dip2px(this,13), displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }

        BottomNavigationViewHelper.disableShiftMode(navigationview);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        viewpager.setAdapter(mAdapter);

        //禁止ViewPager滑动
        viewpager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        viewpager.setOffscreenPageLimit(1);
        navigationview.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        viewpager.setCurrentItem(0);
                        break;
                    case R.id.menu_article:
                        viewpager.setCurrentItem(1);
                        break;
                    case R.id.menu_vip:
                        viewpager.setCurrentItem(2);
                        break;
                    case R.id.menu_more:
                        viewpager.setCurrentItem(3);
                        break;
                }
                return true;
            }
        });

    }

    private void initfragments() {

        mFragments.add(new HomeFragment());
        mFragments.add(new ArticleFragment());
        mFragments.add(new VipFragment());
        mFragments.add(new MoreFragment());

    }

    public void gotoArticle() {
        viewpager.setCurrentItem(1);
        navigationview.setSelectedItemId(R.id.menu_article);
    }

    public void gotoHomePage() {
        viewpager.setCurrentItem(0);
        navigationview.setSelectedItemId(R.id.menu_home);
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记录上次使用时间
        SPUtil.put(Constant.Share_prf.LAST_USING_TIME, System.currentTimeMillis());
    }
}
