package com.lyy.guohe.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.lyy.guohe.R;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourse;
import com.lyy.guohe.model.DBDate;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.view.CourseTableView;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KbFragment extends Fragment {

    private Context mContext;

    private CourseTableView courseTableView;

    private ProgressDialog mProgressDialog;

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
    private String server_week;     //服务器当前周

    private String stu_id;
    private String stu_pass;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kb, container, false);
        mContext = getActivity();
        stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
        stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);
        //初始化课表的背景
        ImageView iv_course_table = view.findViewById(R.id.iv_course_table);
        String bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_course_table);
        }

        List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);

        //构造课表界面
        courseTableView = view.findViewById(R.id.ctv);

        courseTableView.setOnCourseItemClickListener((tv, jieci, day, des) -> showCourseDialog(des));

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("课表导入中,请稍后……");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        //判断今天是不是周一,是周一就发送请求查询今天的周数
        if (weekday == 2) {
            getXiaoLi();
        } else {
            if (dbCourses.size() > 0) {
                showKb(server_week);
            }
        }
        return view;
    }


    //显示课程信息的对话框
    private void showCourseDialog(String courseMesg) {
        String[] courseInfo = courseMesg.split("@");
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
        if (courseInfo.length == 3) {
            courseNum = courseInfo[0];
            courseName = courseInfo[1];
            courseTeacher = courseInfo[2];
        }
        if (courseInfo.length == 4) {
            courseNum = courseInfo[0];
            courseName = courseInfo[1];
            courseTeacher = courseInfo[2];
            courseClassroom = courseInfo[3];
        }

        final MaterialDialog dialog = new MaterialDialog(mContext);

        dialog.isTitleShow(false)//
                .btnNum(1)
                .content("课程信息为：\n" + "课程号：\t" + courseNum + "\n课程名：\t" + courseName + "\n课程教师：\t" + courseTeacher + "\n教室：\t" + courseClassroom)
                .btnText("确定")//
                .showAnim(new BounceBottomEnter())
                .show();

        //left btn click listener
        dialog.setOnBtnClickL(
                (OnBtnClickL) () -> dialog.dismiss()
        );

    }

    //发送查询校历的请求
    private void getXiaoLi() {
        String url = UrlConstant.XIAO_LI;
        if (stu_id != null && stu_pass != null) {
            final RequestBody requestBody = new FormBody.Builder()
                    .add("username", stu_id)
                    .add("password", stu_pass)
                    .build();

            HttpUtil.post(url, requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        Res res = HttpUtil.handleResponse(data);
                        if (res != null) {
                            if (res.getCode() == 200) {
                                SpUtils.putString(mContext, SpConstant.XIAO_LI, res.getInfo());
                                try {
                                    JSONObject object = new JSONObject(res.getInfo());
                                    //获取当前周数
                                    server_week = object.getString("weekNum");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SpUtils.putString(mContext, SpConstant.SERVER_WEEK, server_week);
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> showKb(server_week));
                            }
                        } else {
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                        }

                    }
                }
            });
        }
    }

    //显示课表
    private void showKb(final String week) {
        List<Course> list = new ArrayList<>();

        List<DBCourse> courseList = DataSupport.where("zhouci = ? ", week).find(DBCourse.class);
        if (courseList.size() > 0) {
            List<String> stringList = new ArrayList<>();

            for (DBCourse dbCourse : courseList) {
                String courseInfo[] = dbCourse.getDes().split("@");
                String courseName = courseInfo[1];
                stringList.add(courseName);
            }

            List<String> listWithoutDup = new ArrayList<>(new HashSet<>(stringList));
            listWithoutDup.add("");     //加这句是为了防止数组越界

            for (DBCourse dbCourse : courseList) {
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
                if (courseInfo.length == 3) {
                    courseNum = courseInfo[0];
                    courseName = courseInfo[1];
                    courseTeacher = courseInfo[2];
                }
                if (courseInfo.length == 4) {
                    courseNum = courseInfo[0];
                    courseName = courseInfo[1];
                    courseTeacher = courseInfo[2];
                    courseClassroom = courseInfo[3];
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

            String a[] = {"1", "2", "3", "4", "5", "6", "7"};
            String b = "";
            String month = "";
            List<DBDate> dbDateList = DataSupport.findAll(DBDate.class);
            for (DBDate dbDate : dbDateList) {
                if (dbDate.getZhouci() == Integer.parseInt(week)) {
                    month = dbDate.getMonth();
                    b = dbDate.getDate();
                }
            }

            String c[] = b.split(",");
            if (!b.equals("")) {
                courseTableView.setDates(c);
            } else {
                courseTableView.setDates(a);
            }

            courseTableView.setPreMonth(month + "月");

            courseTableView.drawFrame();
            courseTableView.updateCourseViews(list);

            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
        if (server_week != null) {
            showKb(server_week);
        }
    }
}
