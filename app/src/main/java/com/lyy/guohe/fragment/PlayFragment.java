package com.lyy.guohe.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.LotteryActivity;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.model.Slide;
import com.lyy.guohe.utils.GlideImageLoader;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.NavigateUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment implements View.OnClickListener, OnBannerListener {

    private Activity mContext;

    List<Slide> slides = new ArrayList<>();
    List<String> images = new ArrayList<>();
    List<String> titles = new ArrayList<>();

    private Banner banner;

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() != null)
            mContext = getActivity();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        initView(view);
        getSlide();

        return view;
    }

    //获取轮播图
    private void getSlide() {
        HttpUtil.get(UrlConstant.SLIDE, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mContext.runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    final Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {

                                mContext.runOnUiThread(() -> {
                                    try {
                                        JSONArray array = new JSONArray(res.getInfo());
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject object = array.getJSONObject(i);
                                            Slide slide = new Slide(object.getString("describe"), object.getString("img"), object.getString("title"), object.getString("url"));
                                            slides.add(slide);
                                        }

                                        for (int i = 0; i < slides.size(); i++) {
                                            images.add(slides.get(i).getImg());
                                            titles.add(slides.get(i).getDescribe());
                                        }
                                        initBanner();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            mContext.runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        mContext.runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    mContext.runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    //初始化Banner
    private void initBanner() {
        banner.setBannerStyle(BannerConfig.NUM_INDICATOR_TITLE);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(images);
        //设置标题集合
        banner.setBannerTitles(titles);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    //初始化各控件
    private void initView(View view) {
        CardView mCard1 = (CardView) view.findViewById(R.id.card1);
        mCard1.setOnClickListener(this);
        CardView mCard2 = (CardView) view.findViewById(R.id.card2);
        mCard2.setOnClickListener(this);
        CardView mCard3 = (CardView) view.findViewById(R.id.card3);
        mCard3.setOnClickListener(this);
        CardView mCard4 = (CardView) view.findViewById(R.id.card4);
        mCard4.setOnClickListener(this);
        CardView mCard5 = (CardView) view.findViewById(R.id.card5);
        mCard5.setOnClickListener(this);
        ImageView mIvCard1 = (ImageView) view.findViewById(R.id.iv_card1);
        ImageView mIvCard2 = (ImageView) view.findViewById(R.id.iv_card2);
        ImageView mIvCard3 = (ImageView) view.findViewById(R.id.iv_card3);
        ImageView mIvCard4 = (ImageView) view.findViewById(R.id.iv_card4);
        ImageView mIvCard5 = (ImageView) view.findViewById(R.id.iv_card5);
        Glide.with(mContext).load(R.drawable.card1).into(mIvCard1);
        Glide.with(mContext).load(R.drawable.card2).into(mIvCard2);
        Glide.with(mContext).load(R.drawable.card3).into(mIvCard3);
        Glide.with(mContext).load(R.drawable.card4).into(mIvCard4);
        Glide.with(mContext).load(R.drawable.card5).into(mIvCard5);

        banner = (Banner) view.findViewById(R.id.banner);
        banner.setOnBannerListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.card1:
                //抽奖
                NavigateUtil.navigateTo(getActivity(), LotteryActivity.class);
                break;
            case R.id.card2:
                //表白墙
                NavigateUtil.navigateToUrlWithoutVPN(mContext, "缘来如此", UrlConstant.BIAO_BAI);
                break;
            case R.id.card3:
                //二手市场
                NavigateUtil.navigateToUrlWithoutVPN(mContext, "二手交易", UrlConstant.ER_SHOU);
                break;
            case R.id.card4:
                //兼职信息
                NavigateUtil.navigateToUrlWithoutVPN(mContext, "兼职招聘", UrlConstant.FIND_JOB);
                break;
            case R.id.card5:
                //失物招领
                NavigateUtil.navigateToUrlWithoutVPN(mContext, "失物招领", UrlConstant.FIND_LOST);
                break;
        }
    }

    @Override
    public void OnBannerClick(int position) {
        //链接不为空时跳转
        if (!slides.get(position).getUrl().equals("")) {
            NavigateUtil.navigateToUrlWithoutVPN(mContext, slides.get(position).getTitle(), slides.get(position).getUrl());
        }
    }
}
