package com.lyy.guohe.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.guohe.utils.SpUtils;
import com.roger.catloadinglibrary.CatLoadingView;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    /**
     * 账号
     */
    private EditText mEtLoginAccount;
    /**
     * 密码
     */
    private EditText mEtLoginPass;
    private ImageView mIvBgLogin;

    private FloatingActionButton btnLogin;

    private CatLoadingView loadingView;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        context = this;
        initView();
    }

    //初始化相关控件
    private void initView() {

        mEtLoginAccount = (EditText) findViewById(R.id.et_loginAccount);
        mEtLoginPass = (EditText) findViewById(R.id.et_loginPass);
        mIvBgLogin = findViewById(R.id.iv_bg_login);
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        loadingView = new CatLoadingView();

        Glide.with(this).load(R.drawable.bg_login).into(mIvBgLogin);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_login:
                // 用户登录事件，统计用户点击登录按钮的次数
                StatService.trackCustomKVEvent(this, "login", null);
                if (!mEtLoginAccount.getText().toString().equals("") && !mEtLoginPass.getText().toString().equals("")) {
                    loadingView.show(getSupportFragmentManager(), "");
                    start(mEtLoginAccount.getText().toString(), mEtLoginPass.getText().toString());
                } else {
                    Toasty.warning(context, "请正确输入用户名或密码", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void start(String stu_id, String stu_pass) {

        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.post(UrlConstant.STU_INFO, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toasty.error(context, "网络异常,请稍后重试！", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                String text = res.getInfo();
                                JSONObject object = new JSONObject(text);

                                /**
                                 * academy      学生所在学院
                                 * name         学生姓名
                                 * major        学生专业
                                 * stu_id       学生学号
                                 * stu_pass     学生密码
                                 */
                                String academy = object.getString("academy");
                                String name = object.getString("name");
                                String major = object.getString("major");
                                String stu_id = object.getString("username");
                                String stu_pass = object.getString("password");

                                SpUtils.putString(getApplicationContext(), SpConstant.STU_ID, stu_id);
                                SpUtils.putString(getApplicationContext(), SpConstant.STU_PASS, stu_pass);
                                SpUtils.putString(getApplicationContext(), SpConstant.STU_ACADEMY, academy);
                                SpUtils.putString(getApplicationContext(), SpConstant.STU_NAME, name);
                                SpUtils.putString(getApplicationContext(), SpConstant.STU_MAJOR, major);
                                SpUtils.putBoolean(getApplicationContext(), SpConstant.IS_LOGIN, true);

                                runOnUiThread(() -> {
                                    loadingView.dismiss();
                                    Toasty.success(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                                    NavigateUtil.navigateTo(LoginActivity.this, MainActivity.class);
                                    LoginActivity.this.finish();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(() -> Toasty.error(getApplicationContext(), res.getMsg(), Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    runOnUiThread(() -> Toasty.error(context, "网络异常,请稍后重试！", Toast.LENGTH_SHORT).show());
                }
            }
        });
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
