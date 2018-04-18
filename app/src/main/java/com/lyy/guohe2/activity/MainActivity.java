package com.lyy.guohe2.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.lyy.guohe2.R;
import com.lyy.guohe2.constant.SpConstant;
import com.lyy.guohe2.fragment.KbFragment;
import com.lyy.guohe2.fragment.NewsFragment;
import com.lyy.guohe2.fragment.PlayFragment;
import com.lyy.guohe2.fragment.TodayFragment;
import com.lyy.guohe2.model.DBCourse;
import com.lyy.guohe2.utils.NavigateUtil;
import com.lyy.guohe2.utils.SpUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private LayoutInflater mInflater;
    private List<String> mTitleList = new ArrayList<>();//页卡标题集合
    private View view1, view2, view3, view4, view5;//页卡视图
    private List<View> mViewList = new ArrayList<>();//页卡视图集合
    private List<String> listTitles;
    private List<Fragment> fragments;
    private List<TextView> listTextViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏透明
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);

        initView();
        initFragment();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        //设置tablayout滑动监听
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);
                        if (!(dbCourses.size() > 0)) {
                            Toasty.warning(MainActivity.this, "请导入课表后查看", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TextView tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
        TextView tvStuId = navigationView.getHeaderView(0).findViewById(R.id.tvStuId);
        String name = SpUtils.getString(getApplicationContext(), SpConstant.STU_NAME);
        String stuId = SpUtils.getString(getApplicationContext(), SpConstant.STU_ID);
        tvName.setText("姓名：" + name);
        tvStuId.setText("学号：" + stuId);

    }

    //初始化相应的fragment
    private void initFragment() {
        listTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        listTextViews = new ArrayList<>();


        listTitles.add("今日");
        listTitles.add("课表");
        listTitles.add("资讯");
        listTitles.add("操场");

        TodayFragment fragment1 = new TodayFragment();
        fragments.add(fragment1);
        KbFragment fragment2 = new KbFragment();
        fragments.add(fragment2);
        NewsFragment fragment3 = new NewsFragment();
        fragments.add(fragment3);
        PlayFragment fragment4 = new PlayFragment();
        fragments.add(fragment4);

        //mTabLayout.setTabMode(TabLayout.SCROLL_AXIS_HORIZONTAL);//设置tab模式，当前为系统默认模式
        for (int i = 0; i < listTitles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(listTitles.get(i)));//添加tab选项
        }

        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
            @Override
            public CharSequence getPageTitle(int position) {
                return listTitles.get(position);
            }
        };
        mViewPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mAdapter);//给Tabs设置适配器

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_donate) {
            showDonateDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_aboutUs:
                NavigateUtil.navigateTo(this, UsActivity.class);
                break;
            case R.id.nav_shareApp:
                break;
            case R.id.nav_contactMe:
                //跳转进qq
                if (checkApkExist(this, "com.tencent.mobileqq")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + "420326369" + "&version=1")));
                } else {
                    Toasty.error(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_joinUs:
                if (checkApkExist(this, "com.tencent.mobileqq")) {
                    joinQQGroup("DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8");
                } else {
                    Toasty.error(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_changeAccount:
                changeAccount();
                break;
            case R.id.nav_checkUpdate:
                break;
            case R.id.nav_updateInfo:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //检测手机上是否安装某应用
    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /****************
     *
     * 发起添加群流程。群号：果核 内测(673515498) 的 key 为： DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8
     * 调用 joinQQGroup(DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8) 即可发起手Q客户端申请加群 果核 内测(673515498)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(MainActivity.this, "本机未安装QQ应用或版本不支持", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //切换账号
    private void changeAccount() {
        final boolean isLogin = SpUtils.getBoolean(getApplicationContext(), SpConstant.IS_LOGIN);
        if (isLogin) {
            SpUtils.clear(getApplicationContext());

            DataSupport.deleteAll(DBCourse.class);

            finish();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
            startActivity(intent);
            Toasty.success(getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    //弹出支持捐赠对话框
    private void showDonateDialog() {
        final MaterialDialog dialog = new MaterialDialog(MainActivity.this);
        dialog.content(
                "假如此App为您带来了便利与舒适，假如您愿意支持我们。我们希望能得到小小的赞赏，这是一种莫大的肯定与鼓励。\n" +
                        "金钱是保持自由的一种工具，我们诚挚祈望未来与你同在。^ ^")//
                .btnText("关闭", "支持")//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {//left btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                },
                new OnBtnClickL() {//right btn click listener
                    @Override
                    public void onBtnClick() {
                        if (checkApkExist(MainActivity.this, "com.eg.android.AlipayGphone")) {
                            donate();
                        } else {
                            Toasty.warning(MainActivity.this, "本机未安装支付宝", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                }
        );
    }

    //跳转到支付宝付款界面
    private void donate() {
        String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX07846GRVQI6HABMOJ72%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
        try {
            Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
