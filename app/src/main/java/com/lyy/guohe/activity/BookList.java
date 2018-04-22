package com.lyy.guohe.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.adapter.BookAdapter;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Book;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookList extends AppCompatActivity {

    private Context mContext;

    private ListView lv_book_list;

    private List<Book> bookList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(33, 150, 243));
        setContentView(R.layout.activity_book_list);

        mContext = this;


        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.book_list_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        lv_book_list = (ListView) findViewById(R.id.lv_book_list);

        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");

        lv_book_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = bookList.get(position);
                Intent bookDetailIntent = new Intent(BookList.this, BookDetail.class);
                bookDetailIntent.putExtra("book_url", book.getBook_url());
                startActivity(bookDetailIntent);
            }
        });

        initSwipeRefresh(keyword);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();

    }

    private void initSwipeRefresh(final String keyword) {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {

                requestBookList(keyword);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    //查询图书列表
    private void requestBookList(String bookName) {
        bookList.clear();
        lv_book_list.setVisibility(View.GONE);
        String url = UrlConstant.BOOK_LIST;

        RequestBody requestBody = new FormBody.Builder()
                .add("bookName", bookName)
                .build();

        HttpUtil.post(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = HttpUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200) {
                        try {
                            JSONArray array = new JSONArray(res.getInfo());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String book_author_press = object.getString("book_author_press");
                                String book_can_borrow = object.getString("book_can_borrow");
                                String book_url = object.getString("book_url");
                                String book_title = object.getString("book_title");

                                Book book = new Book(book_title, book_author_press, book_can_borrow, book_url);
                                bookList.add(book);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    BookAdapter bookAdapter = new BookAdapter(BookList.this, R.layout.item_book_list, bookList);
                                    lv_book_list.setAdapter(bookAdapter);
                                    lv_book_list.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Looper.prepare();
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
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
