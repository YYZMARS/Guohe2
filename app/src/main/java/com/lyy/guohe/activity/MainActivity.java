package com.lyy.guohe.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.dialog.widget.ActionSheetDialog;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.adapter.TitleFragmentPagerAdapter;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.fragment.KbFragment;
import com.lyy.guohe.fragment.PlayFragment;
import com.lyy.guohe.fragment.TodayFragment;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.utils.DialogUtils;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.ImageUtil;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.guohe.utils.RomUtils;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.utils.StuUtils;
import com.tencent.bugly.beta.Beta;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private Context mContext;

    private long exitTime = 0;


    public TabLayout mTabLayout;
    private ViewPager mViewPager;

    private CircleImageView civ_header;

    public DrawerLayout drawer;

    //首页头像的base64编码
    private String imageBase64;

    private FragmentSkipInterface mFragmentSkipInterface;

    public void setFragmentSkipInterface(FragmentSkipInterface fragmentSkipInterface) {
        mFragmentSkipInterface = fragmentSkipInterface;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //获取该学生的学年信息
        StuUtils.getAllYear(mContext);
        //初始化权限
        initPermission();
        //初始化布局
        initView();
        //初始化Fragment
        initFragment();
        //初始化相关第三方服务
        initApp();
        //判断云端是否有离线消息
        getPushMessage();
    }

    //初始化统计
    private void initApp() {
        // MTA进入首页事件,统计用户进入首页的次数
        Properties prop = new Properties();
        prop.setProperty("name", "homepage");
        StatService.trackCustomKVEvent(this, "homepage", prop);
    }

    //初始化布局
    private void initView() {
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //初始化NavigationView
        initNavigationView();
        //初始化tabLayout
        initTabLayout();

    }

    //初始化NavigationView
    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView tvStuName = navigationView.getHeaderView(0).findViewById(R.id.tvStuName);
        TextView tvStuId = navigationView.getHeaderView(0).findViewById(R.id.tvStuId);
        TextView tvStuAcademy = navigationView.getHeaderView(0).findViewById(R.id.tvStuAcademy);
        String stuName = SpUtils.getString(mContext, SpConstant.STU_NAME);
        String stuId = SpUtils.getString(mContext, SpConstant.STU_ID);
        String stuAcademy = SpUtils.getString(mContext, SpConstant.STU_ACADEMY);
        tvStuName.setText(stuName);
        tvStuId.setText(stuId);
        tvStuAcademy.setText(stuAcademy);

        //初始化头像
        initHeaderImage(navigationView);
    }

    //初始化头像
    private void initHeaderImage(NavigationView navigationView) {
        civ_header = navigationView.getHeaderView(0).findViewById(R.id.civ_header);
        civ_header.setOnClickListener(v -> {
            final String[] stringItems = {"更换头像"};
            final ActionSheetDialog dialog = new ActionSheetDialog(MainActivity.this, stringItems, null);
            dialog.isTitleShow(false).show();

            dialog.setOnOperItemClickL((parent, view1, position, id) -> {
                switch (position) {
                    case 0:
                        ImageUtil.choosePhotoFromGallery(MainActivity.this, ImageUtil.CHOOSE_PHOTO_FOR_HEADER);
                        break;
                }
                dialog.dismiss();
            });
        });
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }
    }

    //初始化TabLayout
    private void initTabLayout() {
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        //设置tablayout滑动监听
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        List<DBCourseNew> dbCourses = LitePal.findAll(DBCourseNew.class);
                        if (!(dbCourses.size() > 0)) {
                            Toasty.warning(MainActivity.this, "请点击右下角导入课表后查看", Toast.LENGTH_SHORT).show();
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
    }

    //初始化相应的fragment
    private void initFragment() {
        String[] titles = new String[]{"今日", "课表", "操场"};

        List<Fragment> fragments = new ArrayList<>();

        TodayFragment fragment1 = new TodayFragment();
        fragments.add(fragment1);
        KbFragment fragment2 = new KbFragment();
        fragments.add(fragment2);
        PlayFragment fragment3 = new PlayFragment();
        fragments.add(fragment3);

        //设置适配器
        TitleFragmentPagerAdapter adapter = new TitleFragmentPagerAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(adapter);
        //绑定
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            Drawable d = null;
            switch (i) {
                case 0:
                    d = getResources().getDrawable(R.drawable.tab_menu_today);
                    break;
                case 1:
                    d = getResources().getDrawable(R.drawable.tab_menu_kb);
                    break;
                case 2:
                    d = getResources().getDrawable(R.drawable.tab_menu_playground);
                    break;
            }
            if (tab != null)
                tab.setIcon(d);
        }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_lite:
                toLite();
                break;
            case R.id.nav_aboutUs:
                NavigateUtil.navigateTo(this, UsActivity.class);
                break;
            case R.id.nav_shareApp:
                shareApp();
                break;
            case R.id.nav_contactMe:
                contactMe();
                break;
            case R.id.nav_joinUs:
                joinUs();
                break;
            case R.id.nav_changeAccount:
                changeAccount();
                break;
            case R.id.nav_checkUpdate:
                Beta.checkUpgrade();
                break;
            case R.id.nav_updateInfo:
                //跳转至更新说明页
                NavigateUtil.navigateToUrlWithoutVPN(this, "更新说明", UrlConstant.UPDATE_INFO);
                break;
            case R.id.nav_feedBack:
                NavigateUtil.navigateTo(this, FeedBackActivity.class);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //联系我
    private void contactMe() {
        //跳转进qq
        if (RomUtils.checkApkExist(this, "com.tencent.mobileqq")) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + "420326369" + "&version=1")));
            } catch (Exception e) {
                // 未安装手Q或安装的版本不支持
                runOnUiThread(() -> Toasty.error(MainActivity.this, "本机未安装QQ应用或版本不支持", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toasty.error(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
        }
    }

    //加入我们
    private void joinUs() {
        if (RomUtils.checkApkExist(this, "com.tencent.mobileqq")) {
            joinQQGroup("673515498");
        } else {
            Toasty.error(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
        }
    }

    //加入QQ群，传递qq群号即可
    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=" + key + "&card_type=group&source=qrcode\n"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            runOnUiThread(() -> Toasty.error(MainActivity.this, "本机未安装QQ应用或版本不支持", Toast.LENGTH_SHORT).show());
        }
    }

    //切换账号
    private void changeAccount() {
        final boolean isLogin = SpUtils.getBoolean(getApplicationContext(), SpConstant.IS_LOGIN);
        if (isLogin) {
            SpUtils.clear(getApplicationContext());

            LitePal.deleteAll(DBCourseNew.class);

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

    /**
     * android 6.0 以上需要动态申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.READ_LOGS,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    //更新小部件
    private void updateWidget() {
        String WIDGET_UPDATE = "com.lyy.widget.UPDATE_ALL";
        Intent intent = new Intent(WIDGET_UPDATE);
        sendBroadcast(intent);
    }

    //分享此应用
    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "分享 果核： 分享自@酷安网  https://www.coolapk.com/apk/com.lyy.guohe");
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "果核"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
        imageBase64 = SpUtils.getString(mContext, SpConstant.IMAGE_BASE_64);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ImageUtil.CHOOSE_PHOTO_FOR_HEADER:
                if (resultCode == RESULT_OK) {
                    // 4.4及以上系统使用这个方法处理图片
                    Uri uri = data.getData();
                    Intent cropIntent = new Intent(mContext, CropViewActivity.class);
                    cropIntent.putExtra("flag", "img_header");
                    cropIntent.putExtra("uri", uri.toString());
                    startActivity(cropIntent);
                }
                break;

            case ImageUtil.CHOOSE_PHOTO_FOR_KB:
                if (resultCode == RESULT_OK) {
                    // 4.4及以上系统使用这个方法处理图片
                    Uri uri = data.getData();
                    Intent cropIntent = new Intent(mContext, CropViewActivity.class);
                    if (uri != null) {
                        cropIntent.putExtra("uri", uri.toString());
                        cropIntent.putExtra("flag", "course");
                        startActivity(cropIntent);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {  //System.currentTimeMillis()无论何时调用，肯定大于2000
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

    /**
     * Fragment跳转
     */
    public void skipToFragment() {
        if (mFragmentSkipInterface != null) {
            mFragmentSkipInterface.gotoFragment(mViewPager);
        }
    }

    public interface FragmentSkipInterface {
        /**
         * ViewPager中子Fragment之间跳转的实现方法
         */
        void gotoFragment(ViewPager viewPager);
    }

    //判断云端是否有离线消息
    private void getPushMessage() {
        HttpUtil.get(UrlConstant.PUSH_MESS, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(data);
                        int id = object.optInt("id");
                        String url = object.optString("url");
                        String content = object.optString("content");
                        int flag = object.optInt("flag");

                        int localId = SpUtils.getInt(mContext, SpConstant.MESS_ID, 0);

                        Log.d(TAG, "onResponse: " + localId + " " + id + " " + flag);

                        //flag为1表示云端设置该消息可见，设为0表示云端设置消息不可见
                        if (flag == 1) {
                            if (localId < id) {
                                SpUtils.putString(mContext, SpConstant.MESS_URL, url);
                                SpUtils.putString(mContext, SpConstant.MESS_CONTENT, content);
                                SpUtils.putInt(mContext, SpConstant.MESS_ID, id);
                                runOnUiThread(() -> {
                                    DialogUtils.showPushMessDialog(MainActivity.this, content, url);
                                    SpUtils.putBoolean(mContext, SpConstant.IS_MESS_READ, true);
                                });
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //跳转到小程序
    private void toLite() {
        String appId = "wx31c614cef0a3c2b1"; // 填应用AppId
        IWXAPI api = WXAPIFactory.createWXAPI(mContext, appId);

        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        //果核Lite gh_75a1ef8c0da
        //校园导览 gh_dec0502dd077
        req.userName = "gh_75a1ef8c0da5"; // 填小程序原始id
//        req.path = path;                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
        api.sendReq(req);
    }

    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp launchMiniProResp = (WXLaunchMiniProgram.Resp) resp;
            String extraData = launchMiniProResp.extMsg; // 对应JsApi navigateBackApplication中的extraData字段数据
        }
    }
}
