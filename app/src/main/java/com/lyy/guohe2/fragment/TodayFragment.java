package com.lyy.guohe2.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lyy.guohe2.activity.BrowserActivity;
import com.lyy.guohe2.activity.KbActivity;
import com.lyy.guohe2.R;
import com.lyy.guohe2.adapter.CourseAdapter;
import com.lyy.guohe2.constant.SpConstant;
import com.lyy.guohe2.constant.UrlConstant;
import com.lyy.guohe2.model.Course;
import com.lyy.guohe2.model.DBCourse;
import com.lyy.guohe2.model.Res;
import com.lyy.guohe2.utils.HttpUtil;
import com.lyy.guohe2.utils.ListViewUtil;
import com.lyy.guohe2.utils.NavigateUtil;
import com.lyy.guohe2.utils.SpUtils;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

public class TodayFragment extends Fragment implements View.OnClickListener {
    //该Fragment的
    private View view;
    private static final String KEY = "title";

    //今日没课时显示的TextView
    private TextView tvKbShow;
    //消息板块的TextView
    private TextView tvMessage;
    //显示今日课程的ListView
    private ListView lvKbToday;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_today, container, false);
        tvKbShow = (TextView) view.findViewById(R.id.tv_kb_show);
        lvKbToday = view.findViewById(R.id.lv_kbToday);
        tvMessage = view.findViewById(R.id.tv_message);
        TextView tvKb = view.findViewById(R.id.tv_Kb);

        tvKb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigateUtil.navigateTo(getActivity(), KbActivity.class);
            }
        });
        tvKbShow.setText("无课，不欺");

        LinearLayout navKb = view.findViewById(R.id.nav_kb);
        navKb.setOnClickListener(this);
        LinearLayout navGrade = view.findViewById(R.id.nav_grade);
        navGrade.setOnClickListener(this);
        LinearLayout navLibrary = view.findViewById(R.id.nav_library);
        navLibrary.setOnClickListener(this);
        LinearLayout navBus = view.findViewById(R.id.nav_bus);
        navBus.setOnClickListener(this);
        LinearLayout navClassroom = view.findViewById(R.id.nav_classroom);
        navClassroom.setOnClickListener(this);
        LinearLayout navSystem = view.findViewById(R.id.nav_system);
        navSystem.setOnClickListener(this);
        LinearLayout navCet = view.findViewById(R.id.nav_cet);
        navCet.setOnClickListener(this);
        LinearLayout navPE = view.findViewById(R.id.nav_pe);
        navPE.setOnClickListener(this);

        initMess();
        initTodayKb();

        return view;
    }

    private void initTodayKb() {
        List<Course> courses = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

        String server_week = SpUtils.getString(Objects.requireNonNull(getActivity()), SpConstant.SERVER_WEEK);
        if (server_week != null) {
            List<DBCourse> courseList = DataSupport.where("zhouci = ? ", server_week).find(DBCourse.class);
            if (courseList.size() != 0) {
                for (int i = 0; i < courseList.size(); i++) {
                    if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                        int jieci = courseList.get(i).getJieci();
                        String courseInfo[] = courseList.get(i).getDes().split("@");
                        Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                        String courseName = "";
                        String courseClassRoom = "";
                        if (courseInfo.length == 2 || courseInfo.length == 3) {
                            courseName = courseInfo[1];
                        } else if (courseInfo.length == 4) {
                            courseName = courseInfo[1];
                            courseClassRoom = courseInfo[3];
                        }
                        Course course = new Course(jieci, courseName, courseClassRoom);
                        courses.add(course);
                    }
                }
                if (courses.size() > 0) {
                    tvKbShow.setVisibility(View.GONE);
                    lvKbToday.setVisibility(View.VISIBLE);
                    CourseAdapter courseAdapter = new CourseAdapter(getActivity(), R.layout.item_course, courses);
                    lvKbToday.setAdapter(courseAdapter);
                    ListViewUtil.setListViewHeightBasedOnChildren(lvKbToday);
                }
            }
        }
    }


    private void initMess() {
        HttpUtil.get(UrlConstant.GET_MSG, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText("服务器异常");
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    final Res res = HttpUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200) {
                        try {
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvMessage.setText(res.getInfo());
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessage.setText("服务器异常");
                            }
                        });
                    }
                } else {
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMessage.setText("服务器异常");
                        }
                    });
                }
            }
        });
    }

//    /**
//     * fragment静态传值
//     */
//    public static TodayFragment newInstance(String str) {
//        TodayFragment fragment = new TodayFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString(KEY, str);
//        fragment.setArguments(bundle);
//
//        return fragment;
//    }

    //选择进入哪一个系统
    private void showSystemDialog() {
        final String[] items = {"教务系统", "奥兰系统", "实验系统", "师生服务中心"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(getActivity());
        listDialog.setTitle("选择要进入的系统");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始

                Intent intent = new Intent(getActivity(), BrowserActivity.class);
                switch (which) {
                    case 0:
                        intent.putExtra("url", UrlConstant.JIAOWU_URL);
                        intent.putExtra("title", "强智教务");
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("url", UrlConstant.AOLAN_URL);
                        intent.putExtra("title", "奥兰系统");
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("url", UrlConstant.LAB_URL);
                        intent.putExtra("title", "实验系统");
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("url", UrlConstant.FUWU_URL);
                        intent.putExtra("title", "师生服务中心");
                        startActivity(intent);
                        break;
                }
            }
        });
        listDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_kb:
                NavigateUtil.navigateTo(getActivity(), KbActivity.class);
                break;
            case R.id.nav_grade:
                break;
            case R.id.nav_library:
                break;
            case R.id.nav_bus:
                break;
            case R.id.nav_cet:
                break;
            case R.id.nav_classroom:
                break;
            case R.id.nav_pe:
                break;
            case R.id.nav_system:
                showSystemDialog();
                break;

        }
    }
}