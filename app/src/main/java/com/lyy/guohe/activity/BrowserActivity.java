package com.lyy.guohe.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.utils.SpUtils;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BrowserActivity extends AppCompatActivity {

    private static final String TAG = "BrowserActivity";

    private static int index = 0;

    private WebView mWebview;

    private Context mContext;

    private ProgressBar progressBar;

    private OkHttpClient mOkHttpClient;
    private Headers requestHeaders;
    private String cookie;

    private String[] vpn_ACC = {"172210710135", "172210708219", "172210702133", "172210703201", "172210703202", "172210710108"};  //VPN账号数组
    private String[] vpn_Pass = {"220455", "242410", "211375", "033880", "012943", "080021"};//VPN密码数组

    private String vpn_user;
    private String vpn_pwd;

    private String X5url = "";      //网站的url
    private String title = "";      //网站的title
    private boolean isVpn;  //判断该网址是否需要VPN登录
    /**
     * 奥兰系统
     */
    private TextView mTitle;

    //0表示正常，-1表示被占用，1表示密码错误,-3表示网络异常
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.d(TAG, "handleMessage: " + "vpn正常");
                    break;
                case -1:
                    Log.d(TAG, "handleMessage: " + "vpn被占用");
                    if (index < vpn_ACC.length) {
                        VpnSource(vpn_ACC[index], vpn_Pass[index]);
                        index++;
                    } else
                        index = 0;
                    CheckThread thread1 = new CheckThread();
                    thread1.start();
                    break;
                case 1:
                    Log.d(TAG, "handleMessage: " + "vpn密码错误");
                    if (index < vpn_ACC.length) {
                        VpnSource(vpn_ACC[index], vpn_Pass[index]);
                        index++;
                    } else
                        index = 0;
                    CheckThread thread = new CheckThread();
                    thread.start();
                    break;
            }
        }
    };

    class CheckThread extends Thread {

        Message message = new Message();

        @Override
        public void run() {
            super.run();
            String url = "https://vpn.just.edu.cn/dana-na/auth/url_default/login.cgi";
            FormBody formBody = new FormBody.Builder()
                    .add("tz_offset", "480")
                    .add("username", vpn_user)
                    .add("password", vpn_pwd)
                    .add("realm", "LDAP-REALM")
                    .add("btnSubmit", "登录")
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(url)
                    .headers(requestHeaders)
                    .build();

        /*
        0: 正常
        1: 密码错误
        -1: 在其他地方登录
        -3: network error
        */

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message.what = -3;
                            handler.sendMessage(message);
                            Toasty.error(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String location = response.header("location");
                    response.close();
                    if (location != null && location.contains("welcome.cgi?p=failed")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        });
                    } else if (location != null && location.contains("welcome.cgi?p=user-confirm")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                message.what = -1;
                                handler.sendMessage(message);
                            }
                        });
                    } else {
                        cookie = response.headers("Set-Cookie").get(0)
                                + ";" + response.headers("Set-Cookie").get(1)
                                + ";" + response.headers("Set-Cookie").get(2);
                        if (location != null) {
                            Request request = new Request.Builder().url(location)
                                    .headers(requestHeaders).header("Cookie", cookie).build();
                            try {
                                response = mOkHttpClient.newCall(request).execute();
                                cookie = cookie + ";" + response.header("Set-Cookie");
                                response.close();
                                if (cookie != null) {
                                    CookieManager cookieManager = CookieManager.getInstance();
                                    for (String t : cookie.split(";")) {
                                        cookieManager.setCookie(X5url, t);
                                    }
                                }
                                runOnUiThread(() -> {
                                    initView();
                                    message.what = 0;
                                    handler.sendMessage(message);
                                    mWebview.setInitialScale(100);
                                    if (X5url.equals(UrlConstant.PE_SCORE)) {
                                        String stu_id = SpUtils.getString(BrowserActivity.this, SpConstant.STU_ID);
                                        String pe_pass = SpUtils.getString(BrowserActivity.this, SpConstant.PE_PASS);
                                        loginTy(stu_id, pe_pass);
                                    } else {
                                        mWebview.loadUrl(X5url);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        mContext = this;
        StatusBarCompat.setStatusBarColor(this, Color.rgb(33, 150, 243));
        Intent intent = getIntent();
        X5url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        isVpn = intent.getBooleanExtra("isVpn", true);

        initView();

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitle.setText(title);
        if (isVpn) {
            vpn_user = "152210702112";
            vpn_pwd = "087290";

            VpnSource(vpn_user, vpn_pwd);

            CheckThread thread = new CheckThread();
            thread.start();

        } else {
            if (!X5url.equals(""))
                mWebview.loadUrl(X5url);
            else
                Toasty.warning(mContext, "url异常", Toast.LENGTH_SHORT).show();
        }
    }

    public void VpnSource(String vpn_user, String vpn_pwd) {
        this.vpn_user = vpn_user;
        this.vpn_pwd = vpn_pwd;

        requestHeaders = new Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .build();
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;
        if (sslContext != null) {
            mOkHttpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier(DO_NOT_VERIFY)
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();
        }
    }

    /*
    0:  success
    -1: error
    -2: network error
     */
    public void loginTy(String user, String pwd) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder()
                .add("username", user)
                .add("password", pwd)
                .add("chkuser", "true");
        RequestBody requestBody = formBodyBuilder.build();
        Request request = new Request.Builder().url("https://vpn.just.edu.cn/,DanaInfo=202.195.195.147+index1.asp")
                .headers(requestHeaders)
                .post(requestBody)
                .header("Cookie", cookie)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                byte[] b = response.body().bytes();
                String info = new String(b, "GB2312");
                if (info.contains("密码或用户名不正确")) {

                } else {
//                cookie = cookie + ";" + response.headers("Set-Cookie").get(0);
//                APPAplication.save.edit().putString("vpn_cookie", cookie).apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWebview.loadUrl(X5url);
                        }
                    });
                }
            }
        });


    }

    /*
    0: 正常
    -1: 密码错误
    -2: 在其他地方登录
    -3: network error
     */
    public void checkVpnUser() {
        String url = "https://vpn.just.edu.cn/dana-na/auth/url_default/login.cgi";
        FormBody formBody = new FormBody.Builder()
                .add("tz_offset", "480")
                .add("username", vpn_user)
                .add("password", vpn_pwd)
                .add("realm", "LDAP-REALM")
                .add("btnSubmit", "登录")
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .headers(requestHeaders)
                .build();

        /*
        0: 正常
        1: 密码错误
        -1: 在其他地方登录
        -3: network error
        */

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String location = response.header("location");
                response.close();
                if (location != null && location.contains("welcome.cgi?p=failed")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(mContext, "vpn密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (location != null && location.contains("welcome.cgi?p=user-confirm")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(mContext, "VPN被占用", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    cookie = response.headers("Set-Cookie").get(0)
                            + ";" + response.headers("Set-Cookie").get(1)
                            + ";" + response.headers("Set-Cookie").get(2);
                    if (location != null) {
                        Request request = new Request.Builder().url(location)
                                .headers(requestHeaders)
                                .header("Cookie", cookie)
                                .build();
                        try {
                            response = mOkHttpClient.newCall(request).execute();
                            cookie = cookie + ";" + response.header("Set-Cookie");
                            response.close();
                            if (cookie != null) {
                                CookieManager cookieManager = CookieManager.getInstance();
                                for (String t : cookie.split(";")) {
                                    cookieManager.setCookie(url, t);
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initView();
                                    mWebview.setInitialScale(100);
                                    mWebview.loadUrl(url);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        mWebview = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        mTitle = (TextView) findViewById(R.id.title);

        mWebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });


        WebSettings webSettings = mWebview.getSettings();

        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(true);
        // 设置缓存模式,一共有四种模式
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置缓存路径
        //webSettings.setAppCachePath("");
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true);
        // 将图片调整到合适的大小
        webSettings.setUseWideViewPort(true);
        // 支持内容重新布局,一共有四种方式
        // 默认的是NARROW_COLUMNS
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置可以被显示的屏幕控制
        webSettings.setDisplayZoomControls(true);
        // 设置默认字体大小
        webSettings.setDefaultFontSize(12);

        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 5);
        webSettings.setUserAgentString("lyy Mozilla/5.0 (Linux; Android; guohe) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        mWebview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                    if (webView != null && webView.getUrl().contains("202.195.195.198")) {
                        webView.loadUrl("javascript:{" +
                                "var id=document.getElementById('iframe2');" +
                                "id.style.height='388px';" +
                                "id.style.height=(id.contentWindow.document.body.scrollHeight+12).toString()+'px';" +
                                "console.log('已优化');" +
                                "}");
                    }
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("确定", (dialog, which) -> result.confirm());
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("确定");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm());
                b.setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel());
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      final JsPromptResult result) {
                final View v = View.inflate(view.getContext(), R.layout.browser_prompt_dialog, null);
                AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("提示");
                b.setView(v);
                b.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String value = ((EditText) v.findViewById(R.id.browser_prompt_dialog_et))
                            .getText().toString();
                    result.confirm(value);
                });
                b.setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel());
                b.create().show();
                return true;
            }
        });
        mWebview.setWebViewClient(new WebViewClient() {

            //设置webview是否可以发开外链
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {

                if (!url.startsWith("http")) {
                    toIntent(url);
                    return true;
                }

                return super.shouldOverrideUrlLoading(webView, url);

            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                // handler.cancel();// Android默认的处理方式
                sslErrorHandler.proceed();// 接受所有网站的证书
                // handleMessage(Message msg);// 进行其他处理
            }


        });
    }

    private void toIntent(String url) {
        try {
            Toast.makeText(mContext, "尝试打开外部应用", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            Toasty.warning(mContext, "您还未安装客户端", Toast.LENGTH_SHORT).show();
        }
    }

    public void exitVpn() {
        if (cookie != null) {
            Headers requestHeaders = new Headers.Builder()
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                    .build();
            X509TrustManager xtm = new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
            HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            if (sslContext != null) {
                OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory())
                        .hostnameVerifier(DO_NOT_VERIFY)
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .build();
                Request request = new Request.Builder()
                        .url("https://vpn.just.edu.cn/dana-na/auth/logout.cgi")
                        .headers(requestHeaders)
                        .header("Cookie", cookie)
                        .build();

                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(mContext, "退出VPN失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        response.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.success(mContext, "成功退出VPN", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitVpn();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        StatService.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}