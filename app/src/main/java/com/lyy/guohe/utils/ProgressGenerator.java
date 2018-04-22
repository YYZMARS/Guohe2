package com.lyy.guohe.utils;

import android.support.annotation.NonNull;

import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Res;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProgressGenerator {

    private static final String TAG = "ProgressGenerator";

    public interface OnCompleteListener {

        public void onComplete(String text);

        public void onFailure(String text);
    }

    private OnCompleteListener mListener;
    private int mProgress = 50;

    public ProgressGenerator(OnCompleteListener listener) {
        mListener = listener;
    }

    private String stu_id;
    private String stu_pass;

    public void setStu_id(String stu_id) {
        this.stu_id = stu_id;
    }

    public void setStu_pass(String stu_pass) {
        this.stu_pass = stu_pass;
    }

    public void start() {

        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.post(UrlConstant.STU_INFO, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mListener.onFailure("服务器异常,请稍后重试");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    Res res = HttpUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200) {
                        try {
                            mListener.onComplete(res.getInfo());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mListener.onFailure(res.getMsg());
                    }
                } else {
                    mListener.onFailure("服务器异常,请稍后重试");
                }
            }
        });
    }
}