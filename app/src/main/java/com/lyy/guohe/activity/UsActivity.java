package com.lyy.guohe.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.utils.ImageUtil;
import com.tencent.stat.StatService;

import es.dmoral.toasty.Toasty;

public class UsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_us);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(144, 202, 249));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Uri uri = Uri.parse(UrlConstant.APP);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        ImageView view = findViewById(R.id.iv_us);
        Glide.with(this).load(R.drawable.bg_us).into(view);
        ImageView iv_turing = findViewById(R.id.iv_turing);
        iv_turing.setOnLongClickListener(v -> {
            final String[] stringItems = {"分享", "下载到本地"};
            final ActionSheetDialog dialog = new ActionSheetDialog(UsActivity.this, stringItems, null);
            dialog.isTitleShow(false).show();

            dialog.setOnOperItemClickL((parent, view1, position, id) -> {
                switch (position) {
                    case 0:
                        ImageUtil.shareImg(UsActivity.this, iv_turing, "果核里的图灵", "我的主题", "我的分享内容");
                        break;
                    case 1:
                        ImageUtil.saveImage(UsActivity.this, iv_turing);
                        Toasty.success(UsActivity.this, "图片已保存至Picture文件夹", Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            });
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        StatService.onResume(this);
    }
}
