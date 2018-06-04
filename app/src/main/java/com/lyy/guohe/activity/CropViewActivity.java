package com.lyy.guohe.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.utils.SpUtils;
import com.oginotihiro.cropview.CropUtil;
import com.oginotihiro.cropview.CropView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.io.File;

import es.dmoral.toasty.Toasty;

public class CropViewActivity extends AppCompatActivity implements View.OnClickListener {

    private CropView cropView;
    private LinearLayout btnlay;

    private Bitmap croppedBitmap;

    private Uri uri;

    private String flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_view);

        cropView = (CropView) findViewById(R.id.cropView);
        btnlay = (LinearLayout) findViewById(R.id.btnlay);
        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

        doneBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        try {
            Intent intent = getIntent();
            uri = Uri.parse(intent.getStringExtra("uri"));
            flag = intent.getStringExtra("flag");
            cropView.setVisibility(View.VISIBLE);
            btnlay.setVisibility(View.VISIBLE);

        } catch (NullPointerException e) {
            runOnUiThread(() -> {
                Toasty.warning(CropViewActivity.this, "缺少必要的参数", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (uri != null) {
            switch (flag) {
                case "img_header":
                    cropView.of(uri).asSquare().initialize(CropViewActivity.this);
                    break;
                case "course":
                    cropView.of(uri)
                            .withAspect(9, 16)
                            .initialize(CropViewActivity.this);
                    break;
                case "memory":
                    cropView.of(uri)
                            .withAspect(9, 16)
                            .initialize(CropViewActivity.this);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.doneBtn) {
            final ProgressDialog dialog = ProgressDialog.show(CropViewActivity.this, null, "Please wait…", true, false);

            cropView.setVisibility(View.GONE);
            btnlay.setVisibility(View.GONE);

            new Thread() {
                public void run() {
                    croppedBitmap = cropView.getOutput();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
                    String imageBase64 = new String(Base64.encode(baos.toByteArray(), 0));

                    if (flag.equals("course")) {
                        SpUtils.putString(CropViewActivity.this, SpConstant.BG_COURSE_64, imageBase64);
                    } else if (flag.equals("img_header")) {
                        SpUtils.putString(CropViewActivity.this, SpConstant.IMAGE_BASE_64, imageBase64);
                    }

                    Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                    CropUtil.saveOutput(CropViewActivity.this, destination, croppedBitmap, 80);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            CropViewActivity.this.finish();
                        }
                    });
                }
            }.start();
        } else if (id == R.id.cancelBtn) {
            reset();
            CropViewActivity.this.finish();
        }
    }

    private void reset() {
        cropView.setVisibility(View.GONE);
        btnlay.setVisibility(View.GONE);
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
