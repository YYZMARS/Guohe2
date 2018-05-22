package com.lyy.guohe.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.SpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PlayFragment";

    private Context mContext;


    private View view;
    private CardView mCard0;
    private CardView mCard1;
    private CardView mCard2;
    private CardView mCard3;
    private CardView mCard4;
    private CardView mCard5;
    private ImageView mIvCard0;
    private ImageView mIvCard1;
    private ImageView mIvCard2;
    private ImageView mIvCard3;
    private ImageView mIvCard4;
    private ImageView mIvCard5;

    private String title, url, img, des;

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_play, container, false);

        initView(view);
        getPic();

        return view;
    }

    //获取广告图
    private void getPic() {
        HttpUtil.get(UrlConstant.HEAD_PIC, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    final Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                JSONObject object = new JSONObject(res.getInfo());
                                /**
                                 * @title 广告标题
                                 * @img 广告的图片
                                 * @url 点击广告转到的页面
                                 * @describe 广告的详细描述
                                 */
                                title = object.getString("title");
                                img = object.getString("img");
                                url = object.getString("url");
                                des = object.getString("describe");

                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> Glide.with(mContext).load(img).into(mIvCard0));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            }
        });
    }

    //跳转至抽奖页面
    private void toLottery() {
        String username = SpUtils.getString(Objects.requireNonNull(getActivity()), SpConstant.STU_ID);
        Log.d(TAG, "toLottery: " + UrlConstant.LOTTERY + username);
        Intent intent = new Intent(getActivity(), BrowserActivity.class);
        intent.putExtra("url", UrlConstant.LOTTERY + username);
        intent.putExtra("title", "果核抽奖助手");
        intent.putExtra("isVpn", false);
        startActivity(intent);
    }

    //初始化各控件
    private void initView(View view) {
        mCard0 = (CardView) view.findViewById(R.id.card0);
        mCard0.setOnClickListener(this);
        mCard1 = (CardView) view.findViewById(R.id.card1);
        mCard1.setOnClickListener(this);
        mCard2 = (CardView) view.findViewById(R.id.card2);
        mCard2.setOnClickListener(this);
        mCard3 = (CardView) view.findViewById(R.id.card3);
        mCard3.setOnClickListener(this);
        mCard4 = (CardView) view.findViewById(R.id.card4);
        mCard4.setOnClickListener(this);
        mCard5 = (CardView) view.findViewById(R.id.card5);
        mCard5.setOnClickListener(this);
        mIvCard0 = (ImageView) view.findViewById(R.id.iv_card0);
        mIvCard1 = (ImageView) view.findViewById(R.id.iv_card1);
        mIvCard2 = (ImageView) view.findViewById(R.id.iv_card2);
        mIvCard3 = (ImageView) view.findViewById(R.id.iv_card3);
        mIvCard4 = (ImageView) view.findViewById(R.id.iv_card4);
        mIvCard5 = (ImageView) view.findViewById(R.id.iv_card5);
        Glide.with(mContext).load(R.drawable.card1).into(mIvCard1);
        Glide.with(mContext).load(R.drawable.card2).into(mIvCard2);
        Glide.with(mContext).load(R.drawable.card3).into(mIvCard3);
        Glide.with(mContext).load(R.drawable.card4).into(mIvCard4);
        Glide.with(mContext).load(R.drawable.card5).into(mIvCard5);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.card0:
                //广告位
                Intent intent = new Intent(getActivity(), BrowserActivity.class);
                intent.putExtra("title", des);
                intent.putExtra("url", url);
                intent.putExtra("isVpn", false);
                startActivity(intent);
                break;
            case R.id.card1:
                //抽奖
                toLottery();
                break;
            case R.id.card2:
                //表白墙
                Toasty.success(Objects.requireNonNull(getActivity()), "敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.card3:
                //二手市场
                Toasty.success(Objects.requireNonNull(getActivity()), "敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.card4:
                //兼职信息
                Toasty.success(Objects.requireNonNull(getActivity()), "敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.card5:
                //失物招领
                Toasty.success(Objects.requireNonNull(getActivity()), "敬请期待", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
