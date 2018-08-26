package com.lyy.guohe.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.DialogUtils;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.ImageUtil;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.utils.StuUtils;
import com.lyy.guohe.view.CourseTableView;
import com.lyy.guohe.view.Fab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KbFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "KbFragment";

    private String[] colors = {"#85B8CF", "#90C652", "#D8AA5A", "#FC9F9D", "#0A9A84", "#61BC69", "#12AEF3", "#E29AAD"};

    private Activity mContext;

    //底部Fab
    private MaterialSheetFab materialSheetFab;

    //导入课表的加载对话框
    private ProgressDialog mProgressDialog;

    //课表界面
    private CourseTableView courseTableView;

    //课表的背景
    private ImageView iv_bg_kb;

    //周次
    private String weekNum;

    //背景图片的base64编码
    private String bg_course_64;

    //该学生所有的学年数组
    private String[] years;

    //toolbar中的相关控件
    private LinearLayout ll_kb_bar;
    private TextView tv_kb_week;


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
        initToolBar(view);  //初始化Toolbar
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
        bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_bg_kb.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_bg_kb);
        }

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

    //初始化Toolbar
    private void initToolBar(View view) {
        ll_kb_bar = view.findViewById(R.id.ll_kb_bar);
        tv_kb_week = view.findViewById(R.id.tv_kb_week);
        ll_kb_bar.setOnClickListener(this);
    }

    //获取学生的所有学年信息
    public void getAllYear() {
        String stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
        String stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);
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
                                if (Integer.parseInt(weekNum) > 20)
                                    weekNum = "1";
                                SpUtils.putString(mContext, SpConstant.ALL_YEAR, sb.toString());
                                SpUtils.putBoolean(mContext, SpConstant.IS_HAVE_XIAOLI, true);
                                SpUtils.putString(mContext, SpConstant.SERVER_WEEK, weekNum);
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
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    //根据数据库中的dbCourse对象生成适用于课表的course对象
    private Course makeCourse(DBCourseNew dbCourse, boolean isInThisWeek, boolean isRepeat) {
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
            if (isInThisWeek) {
                int index = new Random().nextInt(8);
                course.setBg_Color(Color.parseColor(colors[index]));
                course.setDay(dbCourse.getDay());
                course.setJieci(dbCourse.getJieci());
                course.setDes(dbCourse.getDes());
                course.setClassName(courseName);      //课程名
                course.setClassRoomName(courseClassroom);  //教室
                course.setClassTeacher(courseTeacher);   //教师
                course.setClassTypeName(courseNum);  //课程号
            } else {
                if (!isRepeat) {
                    course.setBg_Color(Color.parseColor("#D3D3D3"));
                    course.setDay(dbCourse.getDay());
                    course.setJieci(dbCourse.getJieci());
                    course.setDes(dbCourse.getDes());
                    course.setClassName(courseName);      //课程名
                    course.setClassRoomName(courseClassroom);  //教室
                    course.setClassTeacher(courseTeacher);   //教师
                    course.setClassTypeName(courseNum);  //课程号
                }
            }
        }

        return course;
    }

    //显示课表
    private void showKb(String week) {
        if (Integer.parseInt(week) > 20)
            week = "1";

        String finalWeek = week;
        mContext.runOnUiThread(() -> tv_kb_week.setText("第 " + finalWeek + " 周"));

        List<Course> list = new ArrayList<>();

        List<DBCourseNew> courseList = LitePal.findAll(DBCourseNew.class);

        if (courseList.size() > 0) {
            for (DBCourseNew dbCourse : courseList) {
                boolean isInThisWeek = StuUtils.isInThisWeek(Integer.parseInt(finalWeek), dbCourse.getZhouci());
                Course course = makeCourse(dbCourse, isInThisWeek, dbCourse.isRepeat());
                list.add(course);
            }

            courseTableView.drawFrame();
            courseTableView.updateCourseViews(list);
            SpUtils.putBoolean(mContext, SpConstant.IS_OPEN_KB, true);
            updateWidget();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_kb_update:
                //更新课表
                updateKb();
                break;
            case R.id.ll_kb_year_change:
                //更改学年
                changYear();
                break;
            case R.id.ll_kb_bg_change:
                //更改课表背景
                changKbBg();
                break;
            case R.id.ll_kb_current_change:
                //更改当前周
                changeCurrentWeek();
                break;
            case R.id.ll_kb_bar:
                //更换周次
                showWeeksDialog(2);
                break;
        }
        if (materialSheetFab.isSheetVisible())
            materialSheetFab.hideSheet();
    }

    //更改当前周
    private void changeCurrentWeek() {
        //弹出选择周次的对话框
        showWeeksDialog(1);
    }

    //弹出选择周次的对话框
    public void showWeeksDialog(int flag) {
        final String[] items = {"第1周", "第2周", "第3周", "第4周", "第5周", "第6周", "第7周", "第8周", "第9周", "第10周", "第11周", "第12周", "第13周", "第14周", "第15周", "第16周", "第17周", "第18周", "第19周", "第20周"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);
        listDialog.setItems(items, (dialog, which) -> {
            //flag为1表示更换当前周,为2表示更换周
            if (flag == 1) {
                SpUtils.putString(mContext, SpConstant.SERVER_WEEK, which + 1 + "");
                showKb((which + 1) + "");
            } else if (flag == 2) {
                showKb((which + 1) + "");
            }
            mContext.runOnUiThread(() -> tv_kb_week.setText("第 " + (which + 1) + " 周"));
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
        final String[] stringItems = {"从相册中选择", "重置为默认"};
        final ActionSheetDialog dialog = new ActionSheetDialog(mContext, stringItems, null);
        dialog.isTitleShow(false).show();
        dialog.setOnOperItemClickL((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    ImageUtil.choosePhotoFromGallery(mContext, ImageUtil.CHOOSE_PHOTO_FOR_KB);
                    break;
                case 1:
                    Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_bg_kb);
                    SpUtils.remove(mContext, SpConstant.BG_COURSE_64);
                    break;
            }
            dialog.dismiss();
        });
    }

    //更改学年
    private void changYear() {
        years = SpUtils.getString(mContext, SpConstant.ALL_YEAR, "").split("@");
        AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);
        listDialog.setTitle("请选择学年");
        listDialog.setItems(years, (dialog, which) -> {
            LitePal.deleteAll(DBCourseNew.class);
            getKb(years[which]);
        });
        listDialog.show();
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

    @Override
    public void onResume() {
        super.onResume();
        bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_bg_kb.setImageBitmap(bitmap);
        }
    }

    //更新桌面小部件
    private void updateWidget() {
        Intent intent = new Intent();
        intent.setAction(Constant.KB_UPDATE);
        intent.setAction(Constant.KB_LIST_UPDATE);
        mContext.sendBroadcast(intent);
    }
}
