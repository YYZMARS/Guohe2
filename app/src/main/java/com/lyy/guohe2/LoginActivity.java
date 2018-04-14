package com.lyy.guohe2;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.processbutton.iml.ActionProcessButton;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe2.constant.SpConstant;
import com.lyy.guohe2.utils.NavigateUtil;
import com.lyy.guohe2.utils.ProgressGenerator;
import com.lyy.guohe2.utils.SpUtils;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, ProgressGenerator.OnCompleteListener {

    private static final String TAG = "LoginActivity";

    /**
     * 账号
     */
    private EditText mEtLoginAccount;
    /**
     * 密码
     */
    private EditText mEtLoginPass;
    /**
     * 登录
     */
    private ActionProcessButton mBtnSignIn;
    ProgressGenerator progressGenerator = new ProgressGenerator(this);

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(250, 250, 250));
        context = this;
        initView();
    }

    //初始化相关控件
    private void initView() {
        ImageView mIvLoginHeader = (ImageView) findViewById(R.id.iv_loginHeader);
        Glide.with(this).load(R.drawable.img_loginheader).into(mIvLoginHeader);

        mEtLoginAccount = (EditText) findViewById(R.id.et_loginAccount);
        mEtLoginPass = (EditText) findViewById(R.id.et_loginPass);
        mBtnSignIn = (ActionProcessButton) findViewById(R.id.btn_SignIn);
        mBtnSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_SignIn:
                if (!mEtLoginAccount.getText().toString().equals("") && !mEtLoginPass.getText().toString().equals("")) {
                    //传递用户名和密码
                    progressGenerator.setStu_id(mEtLoginAccount.getText().toString());
                    progressGenerator.setStu_pass(mEtLoginPass.getText().toString());
                    //开始登录加载动画
                    progressGenerator.start(mBtnSignIn);
                    //设置登录按钮和两个登陆框不可用
                    mBtnSignIn.setEnabled(false);
                    mEtLoginAccount.setEnabled(false);
                    mEtLoginPass.setEnabled(false);
                } else {
                    Toasty.warning(context, "请正确输入用户名或密码", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onComplete(String text) {
        try {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.success(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                }
            });
            NavigateUtil.navigateTo(this, MainActivity.class);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(final String text) {
        Log.d(TAG, "onFailure: " + text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnSignIn.setEnabled(true);
                mEtLoginAccount.setEnabled(true);
                mEtLoginPass.setEnabled(true);
                Toasty.error(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
