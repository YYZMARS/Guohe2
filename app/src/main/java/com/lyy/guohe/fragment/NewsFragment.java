package com.lyy.guohe.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.adapter.NewsAdapter;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.News;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.model.Slide;
import com.lyy.guohe.utils.GlideImageLoader;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.ListViewUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements OnBannerListener, AdapterView.OnItemClickListener {

    private static final String TAG = "NewsFragment";

    List<Slide> slides = new ArrayList<>();
    List<String> images = new ArrayList<>();
    List<String> titles = new ArrayList<>();
    List<News> newsList = new ArrayList<>();

    private Banner banner;

    private ListView lv_news;

    private View view;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.fragment_news, null);

        banner = (Banner) view.findViewById(R.id.banner);
        banner.setOnBannerListener(this);

        lv_news = view.findViewById(R.id.lv_news);

        getSlide();
        initNews();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return view;
    }

    @Override
    public void OnBannerClick(int position) {
        Intent intent = new Intent(getActivity(), BrowserActivity.class);
        intent.putExtra("title", slides.get(position).getTitle());
        intent.putExtra("url", slides.get(position).getUrl());
        intent.putExtra("isVpn", false);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Toasty.success(Objects.requireNonNull(getActivity()), "暂无内容，欢迎投稿", Toast.LENGTH_SHORT).show();

    }

    //获取轮播图
    private void getSlide() {
        HttpUtil.get(UrlConstant.SLIDE, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    final Res res = HttpUtil.handleResponse(data);
                    if (res!=null){
                        if (res.getCode() == 200) {
                            try {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                    try {
                                        JSONArray array = new JSONArray(res.getInfo());
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject object = (JSONObject) array.get(i);
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
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
                        }
                    }else {
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toasty.error(getActivity(), "服务器异常", Toast.LENGTH_SHORT).show());
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

    private void initNews() {
        News news1 = new News("http://p7gzvzwe4.bkt.clouddn.com/test.jpg", "暂无内容，欢迎投稿", "");
        newsList.add(news1);
        News news2 = new News("http://p7gzvzwe4.bkt.clouddn.com/test.jpg", "暂无内容，欢迎投稿", "");
        newsList.add(news2);
//        News news3 = new News("http://p7gzvzwe4.bkt.clouddn.com/test.jpg", "暂无内容，欢迎投稿", "");
//        newsList.add(news3);
//        News news4 = new News("http://p7gzvzwe4.bkt.clouddn.com/test.jpg", "暂无内容，欢迎投稿", "");
//        newsList.add(news4);

        NewsAdapter newsAdapter = new NewsAdapter(getActivity(), R.layout.item_news, newsList);
        lv_news.setAdapter(newsAdapter);
        ListViewUtil.setListViewHeightBasedOnChildren(lv_news);
        lv_news.setOnItemClickListener(this);
    }

//    @Override
//    public void onDestroyView() {
//        // TODO Auto-generated method stub
//        super.onDestroyView();
//        //在销毁视图的时候把父控件remove一下，不然重新加载的时候会异常导致奔溃，提示should remove parent view
//        ViewGroup mGroup = (ViewGroup) view.getParent();
//        if (mGroup != null) {
//            mGroup.removeAllViewsInLayout();
//        }
//    }
}
