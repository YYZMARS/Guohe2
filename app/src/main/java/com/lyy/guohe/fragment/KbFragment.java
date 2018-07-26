package com.lyy.guohe.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.lyy.guohe.App;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.DialogUtils;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.utils.StuUtils;
import com.lyy.guohe.view.CourseTableView;
import com.lyy.guohe.view.Fab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KbFragment extends Fragment implements View.OnClickListener {

    //背景颜色数组
    private int color[] = {
            R.drawable.course_info_blue,
            R.drawable.course_info_brown,
            R.drawable.course_info_cyan,
            R.drawable.course_info_deep_orange,
            R.drawable.course_info_deep_purple,
            R.drawable.course_info_green,
            R.drawable.course_info_indigo,
            R.drawable.course_info_light_blue,
            R.drawable.course_info_light_green,
            R.drawable.course_info_lime,
            R.drawable.course_info_orange,
            R.drawable.course_info_pink,
            R.drawable.course_info_purple,
            R.drawable.course_info_red,
            R.drawable.course_info_teal,
            R.drawable.course_info_yellow,
            R.drawable.course_info_blue,
            R.drawable.course_info_brown,
            R.drawable.course_info_cyan,
            R.drawable.course_info_orange,
            R.drawable.course_info_pink,
            R.drawable.course_info_purple,
            R.drawable.course_info_red,
            R.drawable.course_info_teal
    };

    private static final String TAG = "KbFragment";

    private Activity mContext;

    //底部Fab
    private MaterialSheetFab materialSheetFab;

    private ProgressDialog mProgressDialog;

    private CourseTableView courseTableView;

    private ImageView iv_bg_kb;

    private String weekNum;

    //该学生所有的学年数组
    String[] years;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (getActivity() != null) {
            mContext = getActivity();
        }

        //初始化界面
        View view = inflater.inflate(R.layout.fragment_kb, container, false);
        initView(view);
        return view;
    }

    //初始化界面
    private void initView(View view) {
        initFab(view);      //初始化底部Fab
        initKbView(view);   //初始化课表界面
        initKbData();       //初始化课表数据
    }

    //初始化课表数据
    private void initKbData() {
        weekNum = SpUtils.getString(mContext, SpConstant.SERVER_WEEK, "");
        if (!weekNum.equals("")) {
            showKb(weekNum);
        }
    }

    //初始化课表界面
    private void initKbView(View view) {
        iv_bg_kb = view.findViewById(R.id.iv_bg_kb);
        Glide.with(this).load(R.drawable.bg_kb_default).into(iv_bg_kb);

        //构造课表界面
        courseTableView = view.findViewById(R.id.ctv_fragment);
        courseTableView.setOnCourseItemClickListener((tv, jieci, day, des) -> DialogUtils.showCourseDialog(mContext, des));
    }

    //初始化底部Fab
    private void initFab(View view) {
        Fab fab = view.findViewById(R.id.fab);
        View sheetView = view.findViewById(R.id.fab_sheet);
        View overlay = view.findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.material_white_1000);
        int fabColor = getResources().getColor(R.color.material_white_1000);
        materialSheetFab = new MaterialSheetFab(fab, sheetView, overlay, sheetColor, fabColor);
        LinearLayout ll_kb_update = view.findViewById(R.id.ll_kb_update);
        ll_kb_update.setOnClickListener(this);
//        LinearLayout ll_kb_week_change = view.findViewById(R.id.ll_kb_week_change);
//        ll_kb_week_change.setOnClickListener(this);
        LinearLayout ll_kb_year_change = view.findViewById(R.id.ll_kb_year_change);
        ll_kb_year_change.setOnClickListener(this);
        LinearLayout ll_kb_bg_change = view.findViewById(R.id.ll_kb_bg_change);
        ll_kb_bg_change.setOnClickListener(this);
        LinearLayout ll_kb_current_change = view.findViewById(R.id.ll_kb_current_change);
        ll_kb_current_change.setOnClickListener(this);
    }

    //初始化学年列表
    private void initYearList() {
        years = SpUtils.getString(mContext, SpConstant.ALL_YEAR, "").split("@");
        if (years[0].equals("")) {
            getAllYear();
        } else {
            getKb(years[0]);
        }
    }

    //获取学生的所有学年信息
    public void getAllYear() {
        String stu_id = SpUtils.getString(App.getContext(), SpConstant.STU_ID);
        String stu_pass = SpUtils.getString(App.getContext(), SpConstant.STU_PASS);
        RequestBody requestBody = new FormBody.Builder()
                .add(Constant.STU_ID, stu_id)
                .add(Constant.STU_PASS, stu_pass)
                .build();
        HttpUtil.post(UrlConstant.XIAO_LI, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mContext.runOnUiThread(() -> Toasty.error(mContext, "出现异常，请稍后", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                JSONObject object = new JSONObject(res.getInfo());
                                JSONArray array = object.getJSONArray("all_year");
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < array.length(); i++) {
                                    sb.append(array.get(i)).append("@");
                                }
                                weekNum = object.getString("weekNum");
                                SpUtils.putString(App.getContext(), SpConstant.ALL_YEAR, sb.toString());
                                SpUtils.putBoolean(App.getContext(), SpConstant.IS_HAVE_XIAOLI, true);
                                SpUtils.putString(App.getContext(), SpConstant.SERVER_WEEK, weekNum);
                                mContext.runOnUiThread(() -> {
                                    try {
                                        getKb(array.getString(0));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mContext.runOnUiThread(() -> Toasty.error(mContext, "出现异常，请稍后", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        mContext.runOnUiThread(() -> Toasty.error(mContext, "出现异常，请稍后", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    mContext.runOnUiThread(() -> Toasty.error(mContext, "出现异常，请稍后", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    //设置每节课的信息
    private void setCourse(int jieci, JSONObject object) {
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (int i = 0; i < days.length; i++) {
            String des = object.optString(days[i]);
            if (!des.equals("")) {
                StuUtils.handleCourseInfo(jieci, i + 1, des);
            }
        }
    }

    //获取课表
    private void getKb(String semester) {
        mProgressDialog = ProgressDialog.show(mContext, null, "课表导入中,请稍后……", true, false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        String stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
        String stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);
        RequestBody requestBody = new FormBody.Builder()
                .add(Constant.STU_ID, stu_id)
                .add(Constant.STU_PASS, stu_pass)
                .add(Constant.SEMESTER, semester)
                .build();
        HttpUtil.post(UrlConstant.ALL_COURSE_NEW, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mContext.runOnUiThread(() -> {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = HttpUtil.handleResponse(data);
                    if (res != null) {
                        if (res.getCode() == 200) {
                            try {
                                JSONArray array = new JSONArray(res.getInfo());
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    setCourse((2 * i) + 1, object);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mContext.runOnUiThread(() -> {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                showKb(weekNum);
                                Toasty.success(mContext, "课表导入成功", Toast.LENGTH_SHORT).show();
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
                            Toasty.error(mContext, "课表获取失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    mContext.runOnUiThread(() -> {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Log.d(TAG, "onResponse: 这里出现了异常");
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
//        HttpUtil.get(UrlConstant.ALL_COURSE_NEW, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String data = response.body().string();
//                    Res res = HttpUtil.handleResponse(data);
//                    if (res != null) {
//                        if (res.getCode() == 200) {
//                            try {
//                                JSONArray array = new JSONArray(res.getInfo());
//                                for (int i = 0; i < array.length(); i++) {
//                                    JSONObject object = array.getJSONObject(i);
//                                    setCourse((2 * i) + 1, object);
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            mContext.runOnUiThread(() -> {
//                                if (mProgressDialog.isShowing())
//                                    mProgressDialog.dismiss();
//                                Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
//                            });
//                        }
//                    } else {
//                        mContext.runOnUiThread(() -> {
//                            if (mProgressDialog.isShowing())
//                                mProgressDialog.dismiss();
//                            Toasty.error(mContext, "课表获取失败，请稍后重试", Toast.LENGTH_SHORT).show();
//                        });
//                    }
//                } else {
//                    mContext.runOnUiThread(() -> {
//                        if (mProgressDialog.isShowing())
//                            mProgressDialog.dismiss();
//                        Log.d(TAG, "onResponse: 这里出现了异常");
//                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            }
//        });
    }

    //显示课表
    private void showKb(String week) {
        if (Integer.parseInt(week) > 20)
            week = "1";

        List<Course> list = new ArrayList<>();

        List<DBCourseNew> courseList = LitePal.where("zhouci = ? ", week).find(DBCourseNew.class);

        List<String> stringList = new ArrayList<>();

        for (DBCourseNew dbCourse : courseList) {
            String courseInfo[] = dbCourse.getDes().split("@");
            String courseName = courseInfo[1];
            stringList.add(courseName);
        }

        List<String> listWithoutDup = new ArrayList<>(new HashSet<>(stringList));
        listWithoutDup.add("");     //加这句是为了防止数组越界

        for (DBCourseNew dbCourse : courseList) {
            Course course = new Course();
            String courseInfo[] = dbCourse.getDes().split("@");
            String courseNum = "";
            String courseClassroom = "";
            String courseName = "";
            String courseTeacher = "";

            if (courseInfo.length == 1) {
                courseNum = courseInfo[0];
            }
            if (courseInfo.length == 2) {
                courseNum = courseInfo[0];
                courseName = courseInfo[1];
            }
            if (courseInfo.length == 3 || courseInfo.length == 4) {
                courseNum = courseInfo[0];
                courseName = courseInfo[1];
                courseTeacher = courseInfo[2];
            }
            if (courseInfo.length == 5) {
                courseNum = courseInfo[0];
                courseName = courseInfo[1];
                courseTeacher = courseInfo[2];
                courseClassroom = courseInfo[4];
            }

            if (dbCourse.getDes().length() > 3) {
                course.setDay(dbCourse.getDay());
                course.setJieci(dbCourse.getJieci());
                course.setDes(dbCourse.getDes());
                course.setClassName(courseName);      //课程名
                course.setClassRoomName(courseClassroom);  //教室
                course.setClassTeacher(courseTeacher);   //教师
                course.setClassTypeName(courseNum);  //课程号
                course.setBg_Color(color[listWithoutDup.indexOf(courseName)]);
                list.add(course);
            }
        }

        courseTableView.drawFrame();
        courseTableView.updateCourseViews(list);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_kb_update:
                updateKb();     //更新课表
                break;
//            case R.id.ll_kb_week_change:
//                changeWeek();   //更改周次
//                break;
            case R.id.ll_kb_year_change:
                changYear();    //更改学年
                break;
            case R.id.ll_kb_bg_change:
                changKbBg();    //更改课表背景
                break;
            case R.id.ll_kb_current_change:
                changeCurrentWeek();        //更改当前周
                break;
        }
        if (materialSheetFab.isSheetVisible())
            materialSheetFab.hideSheet();
    }

    //更改当前周
    private void changeCurrentWeek() {
        //弹出选择周次的对话框
        showWeeksDialog();
    }


    //弹出选择周次的对话框
    public void showWeeksDialog() {
        final String[] items = {"第1周", "第2周", "第3周", "第4周", "第5周", "第6周", "第7周", "第8周", "第9周", "第10周", "第11周", "第12周", "第13周", "第14周", "第15周", "第16周", "第17周", "第18周", "第19周", "第20周"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);
        listDialog.setItems(items, (dialog, which) -> {

            SpUtils.putString(mContext, SpConstant.SERVER_WEEK, which + 1 + "");
            showKb(which + 1 + "");
            dialog.dismiss();
        });

        AlertDialog dialog = listDialog.create();
        dialog.setTitle("请选择周次");
        dialog.show();

        // 将对话框的大小按屏幕大小的百分比设置
        WindowManager windowManager = mContext.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth() * 0.8); //设置宽度
        lp.height = (int) (display.getHeight() * 0.7);
        dialog.getWindow().setAttributes(lp);
    }

    //更改课表背景
    private void changKbBg() {
        Toast.makeText(mContext, "更改课表背景", Toast.LENGTH_SHORT).show();
    }

    //更改学年
    private void changYear() {

        Toast.makeText(mContext, "更改学年", Toast.LENGTH_SHORT).show();
    }

    //更新课表
    private void updateKb() {
        List<DBCourseNew> dbCourses = LitePal.findAll(DBCourseNew.class);
        if ((dbCourses.size() > 0)) {
            LitePal.deleteAll(DBCourseNew.class);
        }
        SpUtils.remove(mContext, SpConstant.ALL_YEAR);
        initYearList();
    }
}
