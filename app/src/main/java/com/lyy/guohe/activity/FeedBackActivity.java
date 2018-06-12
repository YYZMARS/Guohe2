package com.lyy.guohe.activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.SpUtils;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedBackActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 有什么想告诉我们的吗？
     */
    private EditText mEtContent;
    /**
     * QQ/微信/电话，方便我们联系到你来更好地解决问题。
     */
    private EditText mEtContact;
    /**
     * 发射
     */
    private Button mBtnFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        initView();
        StatusBarCompat.setStatusBarColor(this, Color.rgb(119, 136, 213));
        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.feedback_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initView() {
        mEtContent = (EditText) findViewById(R.id.et_content);
        mEtContact = (EditText) findViewById(R.id.et_contact);
        mBtnFeedback = (Button) findViewById(R.id.btn_feedback);
        mBtnFeedback.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_feedback:
                String content = mEtContent.getText().toString();
                String contact = mEtContact.getText().toString();
                String stuName = SpUtils.getString(this, SpConstant.STU_NAME);
                if (!contact.equals("") && !content.equals("")) {
                    ProgressDialog waitingDialog = new ProgressDialog(FeedBackActivity.this);
                    waitingDialog.setTitle("反馈");
                    waitingDialog.setMessage("反馈中...");
                    waitingDialog.setIndeterminate(true);
                    waitingDialog.setCancelable(false);
                    waitingDialog.show();

                    RequestBody requestBody = new FormBody.Builder()
                            .add("name", stuName)
                            .add("content", content)
                            .add("category", "2")
                            .add("contact", contact)
                            .build();
                    HttpUtil.post(UrlConstant.FEEDBACK, requestBody, new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            runOnUiThread(() -> {
                                Toasty.warning(FeedBackActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                if (waitingDialog.isShowing())
                                    waitingDialog.dismiss();
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String data = response.body().string();
                                Res res = HttpUtil.handleResponse(data);
                                if (res != null) {
                                    runOnUiThread(() -> {
                                        Toasty.success(FeedBackActivity.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                                        if (waitingDialog.isShowing())
                                            waitingDialog.dismiss();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toasty.warning(FeedBackActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                        if (waitingDialog.isShowing())
                                            waitingDialog.dismiss();
                                    });
                                }
                            } else {
                                runOnUiThread(() -> {
                                    Toasty.warning(FeedBackActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                    if (waitingDialog.isShowing())
                                        waitingDialog.dismiss();
                                });
                            }
                        }
                    });
                } else {
                    Toasty.warning(this, "请将内容输入完整", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
