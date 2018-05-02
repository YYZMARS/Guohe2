package com.lyy.guohe.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.R;
import com.lyy.guohe.adapter.LibraryAdapter;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Library;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.searchlibrary.searchbox.SearchFragment;
import com.lyy.searchlibrary.searchbox.custom.IOnSearchClickListener;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LibraryActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, IOnSearchClickListener {

    private static final String TAG = "LibraryActivity";

    private Context mContext;

    private SearchFragment searchFragment;

    private String[] mVals = new String[15];

    private List<Library> libraryList = new ArrayList<>();

    private LibraryAdapter adapter;

    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    private int[] color = {
            R.color.material_amber_200,
            R.color.material_red_200,
            R.color.material_light_green_300,
            R.color.material_green_200,
            R.color.material_teal_500,
            R.color.material_light_blue_500,
            R.color.material_blue_400,
            R.color.material_pink_200,
            R.color.material_orange_500,
            R.color.material_deep_orange_A200,
            R.color.material_orange_A200,
            R.color.material_lime_400,
            R.color.material_amber_400,
            R.color.material_red_300
    };

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.library_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = () -> {
            libraryList.clear();
            requestBookTop();
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    //发送查询前本热门书籍的请求
    private void requestBookTop() {
        recyclerView.setVisibility(View.GONE);
        String url = UrlConstant.BOOK_TOP;
        HttpUtil.get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                    Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = HttpUtil.handleResponse(data);
                    getHotBook();
                    if (res != null) {
                        if (res.getCode() == 200)
                            handleResponse(res.getInfo());
                        else {
                            Looper.prepare();
                            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                            Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        runOnUiThread(() -> {
                            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                            Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                        Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    //热搜图书
    private void getHotBook() {
        String url = UrlConstant.HOT_BOOK;
        HttpUtil.get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                    Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                JSONArray array = new JSONArray(res.getInfo());
                                JSONArray innerArray = array.getJSONArray(0);
                                for (int i = 0; i < 10; i++) {
                                    if (innerArray.get(i) != null) {
                                        mVals[i] = innerArray.get(i).toString().split(" ")[1];
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                        } else {
                            runOnUiThread(() -> {
                                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                                Toasty.error(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                            Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                        Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void handleResponse(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String bookcode = object.getString("bookcode");
                String press = object.getString("press");
                String name = object.getString("name");
                String author = object.getString("author");
                String url = object.getString("url");

                int x = (int) (Math.random() * 13);
                Library library = new Library(bookcode, name, press, url, author, color[x]);
                libraryList.add(library);
            }

            runOnUiThread(() -> {
                recyclerView.setLayoutManager(layoutManager);
                adapter = new LibraryAdapter(libraryList);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnSearchClick(String keyword) {
        HashMap<String, String> map = new HashMap<>();
        map.put("keyword", keyword);
        NavigateUtil.navigateTo(LibraryActivity.this,BookList.class,map);
//        Intent bookListIntent = new Intent(LibraryActivity.this, BookList.class);
//        bookListIntent.putExtra("keyword", keyword);
//        startActivity(bookListIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search://点击搜索
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(33, 150, 243));
        setContentView(R.layout.activity_library);

        mContext = this;

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.library_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        toolbar.setOnMenuItemClickListener(this);

        searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(this);

        searchFragment.setmVals(mVals);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(LibraryActivity.this, 2);

        initSwipeRefresh();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();
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
