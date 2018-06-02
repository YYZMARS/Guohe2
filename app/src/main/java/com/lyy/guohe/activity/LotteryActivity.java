package com.lyy.guohe.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

import es.dmoral.toasty.Toasty;

public class LotteryActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery);
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#2196F3"));
        mContext = this;
        initView();
    }

    private void initView() {
        TextView mTitle = (TextView) findViewById(R.id.title);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progress);
        WebView mWebview = (WebView) findViewById(R.id.webview);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitle.setText("果核抽奖助手");


        mWebview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    mProgress.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    mProgress.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    mProgress.setProgress(newProgress);//设置进度值
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("确定", (dialog, which) -> result.confirm());
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
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
                AlertDialog.Builder b = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
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

        WebSettings webSettings = mWebview.getSettings();
        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(true);
        // 设置缓存模式,一共有四种模式
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
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

        String stu_id = SpUtils.getString(mContext, SpConstant.STU_ID, "");
        if (!stu_id.equals("")) {
            String url = UrlConstant.LOTTERY + stu_id;
            mWebview.loadUrl(url);
        } else {
            Toasty.error(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show();
        }
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
}
