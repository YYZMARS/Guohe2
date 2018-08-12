package com.lyy.guohe.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.activity.MainActivity;
import com.lyy.guohe.activity.ScoreActivity;
import com.lyy.guohe.activity.SportActivity;
import com.lyy.guohe.adapter.CourseAdapter;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.BusUtil;
import com.lyy.guohe.utils.DialogUtils;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.ListViewUtil;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.guohe.utils.SpUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TodayFragment extends Fragment implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Activity mContext;

    //今日没课时显示的TextView
    private TextView tvKbShow;
    //消息板块的TextView
    private TextView tvMessage;
    //显示今日课程的ListView
    private ListView lvKbToday;

    private ProgressDialog mProgressDialog;

    private ImageView ivOneImg;

    private TextView tvOneDate;

    private TextView tvImgAuthor;

    private TextView tvOneWord;

    private TextView tvOneWordFrom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getActivity() != null)
            mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        initView(view);
        initMess();
        initTodayKb();
        initOneContent();

        return view;
    }

    //加载首页View
    private void initView(View view) {

        //加载toolbar
        initToolBar(view);

        //加载头部
        initHeader(view);

        //加载中间
        initContent(view);

    }

    //加载toolbar
    private void initToolBar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_today);
        toolbar.setTitle("");
        if (mContext != null) {
            setHasOptionsMenu(true);
            ((AppCompatActivity) mContext).setSupportActionBar(toolbar);
            android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) mContext).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_left);
            }
            //显示果核的彩蛋
            showEgg(view);
        }

    }

    //加载首页头部
    private void initHeader(View view) {
        LinearLayout navGrade = view.findViewById(R.id.nav_grade);
        navGrade.setOnClickListener(this);
        LinearLayout navBus = view.findViewById(R.id.nav_bus);
        navBus.setOnClickListener(this);
        LinearLayout navPE = view.findViewById(R.id.nav_pe);
        navPE.setOnClickListener(this);
        LinearLayout navMore = view.findViewById(R.id.nav_more);
        navMore.setOnClickListener(this);
        LinearLayout navSystem = view.findViewById(R.id.nav_system);
        navSystem.setOnClickListener(this);
    }

    //加载首页中间
    @SuppressLint("SetTextI18n")
    private void initContent(View view) {
        tvKbShow = (TextView) view.findViewById(R.id.tv_kb_show);
        lvKbToday = view.findViewById(R.id.lv_kbToday);
        tvMessage = view.findViewById(R.id.tv_message);
        TextView tvKb = view.findViewById(R.id.tv_Kb);

        //点击跳转至第二个fragment
        tvKb.setOnClickListener(v -> {
            final MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.setFragmentSkipInterface(viewPager -> viewPager.setCurrentItem(1));
            mainActivity.skipToFragment();
        });
        tvKbShow.setText("今天居然没有课~" + "\uD83D\uDE01");

        ivOneImg = view.findViewById(R.id.iv_one_img);
        ivOneImg.setDrawingCacheEnabled(true);
        ivOneImg.setOnClickListener(this);
        tvImgAuthor = view.findViewById(R.id.tv_img_author);
        tvOneDate = view.findViewById(R.id.tv_one_date);
        tvOneWord = view.findViewById(R.id.tv_one_word);
        tvOneWordFrom = view.findViewById(R.id.tv_one_word_from);

        LinearLayout llOne = view.findViewById(R.id.ll_one);
        llOne.setOnClickListener(this);
    }

    //加载今日课表
    private void initTodayKb() {
        List<Course> courses = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

        if (mContext != null) {
            String server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
            List<DBCourseNew> courseList = new ArrayList<>();
            if (server_week != null) {
                try {
                    courseList = LitePal.where("zhouci = ? ", server_week).find(DBCourseNew.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (courseList.size() != 0) {
                    for (int i = 0; i < courseList.size(); i++) {
                        if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                            int jieci = courseList.get(i).getJieci();
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length > 2) {
                                courseName = courseInfo[1];
                                if (courseInfo.length == 5) {
                                    courseClassRoom = courseInfo[4];
                                }
                            }

                            Course course = new Course(jieci, courseName, courseClassRoom);
                            courses.add(course);
                        }
                    }
                    if (courses.size() > 0) {
                        tvKbShow.setVisibility(View.GONE);
                        lvKbToday.setVisibility(View.VISIBLE);
                        CourseAdapter courseAdapter = new CourseAdapter(mContext, R.layout.item_course, courses);
                        lvKbToday.setAdapter(courseAdapter);
                        ListViewUtil.setListViewHeightBasedOnChildren(lvKbToday);
                    }
                }
            }
        }
    }

    //加载首页Message
    private void initMess() {
        HttpUtil.get(UrlConstant.GET_MSG, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mContext.runOnUiThread(() -> tvMessage.setText("服务器异常"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    final Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                mContext.runOnUiThread(() -> {
                                    String s = res.getInfo();
                                    tvMessage.setText(s.substring(2, s.length() - 2));
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            mContext.runOnUiThread(() -> tvMessage.setText("服务器异常"));
                        }
                    } else {
                        mContext.runOnUiThread(() -> tvMessage.setText("服务器异常"));
                    }
                } else {
                    mContext.runOnUiThread(() -> tvMessage.setText("服务器异常"));
                }
            }
        });
    }

    //显示果核的彩蛋
    private void showEgg(View view) {
        //果核的彩蛋
        TextView mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mTvTitle.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 5;//点击次数
            final static long DURATION = 3 * 1000;//规定有效时间
            long[] mHits = new long[COUNTS];
            int index = 0;

            @Override
            public void onClick(View v) {
                /**
                 * 实现双击方法
                 * src 拷贝的源数组
                 * srcPos 从源数组的那个位置开始拷贝.
                 * dst 目标数组
                 * dstPos 从目标数组的那个位子开始写数据
                 * length 拷贝的元素的个数
                 */
                index++;
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (index == 3) {
                    Toast.makeText(mContext, "哈哈哈，就快发现彩蛋了！", Toast.LENGTH_SHORT).show();
                }
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    DialogUtils.showEggDialog(mContext);
                    index = 0;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_grade:
                //跳转至成绩查询界面
                NavigateUtil.navigateTo(mContext, ScoreActivity.class);
                break;
            case R.id.nav_bus:
                //显示即将到来的校车
                DialogUtils.showBusDialog(mContext, BusUtil.hasSchoolBus());
                break;
            case R.id.nav_pe:
                //显示可以进入的体育系统
                showPEDialog();
                break;
            case R.id.nav_system:
                DialogUtils.showSystemDialog(mContext);
                break;
            case R.id.iv_one_img:
                //显示One模块的对话框
                DialogUtils.showPopImageDialog(mContext, ivOneImg);
                break;
            case R.id.nav_more:
                //显示更多对话框
                DialogUtils.showMoreDialog(mContext);
                break;
        }
    }

    //加载One的内容
    private void initOneContent() {
        RequestBody requestBody = new FormBody.Builder()
                .add("TransCode", "030111")
                .add("OpenId", "123456789")
                .add("Body", "")
                .build();
        HttpUtil.post(UrlConstant.ONE, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONObject object = new JSONObject(data).getJSONObject("Body");
                        String date = object.getString("date").split(" ")[0].replaceAll("-", "/");
                        String imgUrl = object.getString("img_url");
                        String imgAuthor = object.getString("img_author");
                        String imgKind = object.getString("img_kind");
                        String url = object.getString("url");
                        String word = object.getString("word");
                        String wordFrom = object.getString("word_from");

                        mContext.runOnUiThread(() -> {
                            tvOneWord.setText(word);
                            tvOneWordFrom.setText(wordFrom);
                            tvOneDate.setText(date);
                            tvImgAuthor.setText(imgAuthor + " | " + imgKind);
                            Glide.with(mContext).load(imgUrl).into(ivOneImg);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //选择进入哪一个体育系统
    private void showPEDialog() {
        final String[] items = {"俱乐部查询", "早操出勤查询", "体育成绩查询"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(mContext);
        listDialog.setTitle("选择要进入的系统");
        listDialog.setItems(items, (dialog, which) -> {
            // which 下标从0开始
            Intent intent = new Intent(mContext, SportActivity.class);
            switch (which) {
                case 0:
                    intent.putExtra("url", UrlConstant.CLUB_SCORE);
                    intent.putExtra("type", "1");
                    startActivity(intent);
                    break;
                case 1:
                    intent.putExtra("url", UrlConstant.EXERCISE_SCORE);
                    intent.putExtra("type", "2");
                    startActivity(intent);
                    break;
                case 2:
                    showPePassDialog();
                    break;
            }
        });
        listDialog.show();
    }

    private void showPePassDialog() {
        String pePass = SpUtils.getString(mContext, SpConstant.PE_PASS);
        if (pePass == null) {
            final EditText editText = new EditText(mContext);
            AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
            inputDialog.setTitle("请输入你的体育学院密码(默认姓名首字母大写)").setView(editText);
            inputDialog.setPositiveButton("确定",
                    (dialog, which) -> {
                        mProgressDialog = ProgressDialog.show(mContext, null, "密码验证中,请稍后……", true, false);
                        mProgressDialog.setCancelable(true);
                        mProgressDialog.setCanceledOnTouchOutside(true);
                        final String username1 = SpUtils.getString(mContext, SpConstant.STU_ID);
                        String pePass1 = editText.getText().toString();
                        if (username1 != null && !pePass1.equals("")) {
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("username", username1)
                                    .add("password", pePass1)
                                    .build();
                            HttpUtil.post(UrlConstant.CLUB_SCORE, requestBody, new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    mContext.runOnUiThread(() -> {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String data = response.body().string();
                                        Res res = HttpUtil.handleResponse(data);
                                        if (res != null) {
                                            if (res.getCode() == 200 || res.getCode() == 1000) {
                                                mContext.runOnUiThread(() -> {
                                                    mProgressDialog.dismiss();
                                                    SpUtils.putString(mContext, SpConstant.PE_PASS, pePass1);
                                                    Intent intent = new Intent(mContext, BrowserActivity.class);
                                                    intent.putExtra("url", UrlConstant.PE_SCORE);
                                                    intent.putExtra("title", "体育成绩查询");
                                                    startActivity(intent);
                                                });
                                            } else {
                                                mContext.runOnUiThread(() -> {
                                                    if (mProgressDialog.isShowing())
                                                        mProgressDialog.dismiss();
                                                    Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                        } else {
                                            mContext.runOnUiThread(() -> {
                                                if (mProgressDialog.isShowing())
                                                    mProgressDialog.dismiss();
                                                Toasty.error(mContext, "发生异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    } else {
                                        mContext.runOnUiThread(() -> {
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            Toasty.error(mContext, "服务器发生异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            });
                        } else {
                            mContext.runOnUiThread(() -> Toasty.warning(mContext, "输入框不可为空", Toast.LENGTH_SHORT).show());
                        }
                    }).show();
        } else {
            Intent intent = new Intent(mContext, BrowserActivity.class);
            intent.putExtra("url", UrlConstant.PE_SCORE);
            intent.putExtra("title", "体育成绩查询");
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_donate:
                DialogUtils.showDonateDialog(mContext);
                break;
            case android.R.id.home:
                MainActivity activity = (MainActivity) mContext;
                activity.drawer.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTodayKb();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}