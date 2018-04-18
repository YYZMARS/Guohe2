package com.lyy.guohe2.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.lyy.guohe2.R;
import com.lyy.guohe2.constant.SpConstant;
import com.lyy.guohe2.constant.UrlConstant;
import com.lyy.guohe2.model.Course;
import com.lyy.guohe2.model.DBCourse;
import com.lyy.guohe2.model.DBDate;
import com.lyy.guohe2.model.Res;
import com.lyy.guohe2.utils.HttpUtil;
import com.lyy.guohe2.utils.SpUtils;
import com.lyy.guohe2.view.CourseTableView;

import org.json.JSONArray;
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

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class KbFragment extends Fragment {

    private View view;

    private Context mContext;

    public static final int CHOOSE_PHOTO = 2;

    private static final String TAG = "KbActivity";

    private CourseTableView courseTableView;

    private ProgressDialog mProgressDialog;

    private ImageView iv_course_table;

    private String bg_course_64;

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

//    private TextView tv_course_table_toolbar;

    private List<String> all_year_list;

    private String stu_id;
    private String stu_pass;
    private String current_year;

    private String server_week;     //服务器当前周

    public KbFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_kb, container, false);
        mContext = getActivity();
        all_year_list = new ArrayList<>();
        //初始化课表的背景
        iv_course_table = (ImageView) view.findViewById(R.id.iv_course_table);
        bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_course_table);
        }

        List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);
        if (dbCourses.size() > 0) {
            //构造课表界面
            courseTableView = (CourseTableView) view.findViewById(R.id.ctv);

            courseTableView.setOnCourseItemClickListener(new CourseTableView.OnCourseItemClickListener() {
                @Override
                public void onCourseItemClick(TextView tv, int jieci, int day, String des) {
//                String string = tv.getText().toString();
                    Log.d(TAG, "onCourseItemClick: " + des);
                    showCourseDialog(des);
                }
            });

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("课表导入中,请稍后……");
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(true);

            stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
            stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);

            server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
            showKb(server_week);
//            if (server_week != null) {
//                Calendar calendar = Calendar.getInstance();
//                int weekday = calendar.get(Calendar.DAY_OF_WEEK);
//                //判断今天是不是周一
//                if (weekday == 2) {
//                    //判断是否第一次导入课表，默认false，没有导入课表
//                    boolean first_open_course = SpUtils.getBoolean(mContext, SpConstant.FIRST_OPEN_COURSE);
//                    if (first_open_course) {
//                        getXiaoLi();
//                        SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
//                    } else {
//                        showKb(server_week);
//                    }
//                } else {
//                    //判断是否第一次导入课表，默认false，没有导入课表
//                    boolean first_open_course = SpUtils.getBoolean(mContext, SpConstant.FIRST_OPEN_COURSE);
//                    if (first_open_course) {
//                        getXiaoLi();
//                        SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
//                    } else {
//                        showKb(server_week);
//                    }
//                }
//
////                List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);
//                if (dbCourses.size() == 0) {
//                    getXiaoLi();
//                    SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
//                }
//
//            } else {
//                getXiaoLi();
//                SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
//            }
        } else {
            Toasty.warning(mContext, "请导入课表后查看", Toast.LENGTH_SHORT).show();
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

        dialog.setOnBtnClickL(
                new OnBtnClickL() {//left btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                }
        );

    }

    //发送查询校历的请求
    private void getXiaoLi() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
        String url = UrlConstant.XIAO_LI;
        if (stu_id != null && stu_pass != null) {
            final RequestBody requestBody = new FormBody.Builder()
                    .add("username", stu_id)
                    .add("password", stu_pass)
                    .build();

            HttpUtil.post(url, requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
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
                            SpUtils.putString(mContext, SpConstant.XIAO_LI, res.getInfo());
                            try {
                                JSONObject object = new JSONObject(res.getInfo());
                                //获取当前周数
                                server_week = object.getString("weekNum");
                                //获取这个学生所有的学年
                                JSONArray jsonArray = object.getJSONArray("all_year");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    all_year_list.add(jsonArray.get(i).toString());
                                }
                                current_year = all_year_list.get(0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tv_course_table_toolbar.setText("第" + server_week + "周");
//                                }
//                            });
                            SpUtils.putString(mContext, SpConstant.SERVER_WEEK, server_week);
                            if (current_year != null) {
                                getKbInfo(current_year);
                            }
                        } else {
                            Looper.prepare();
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Toasty.error(mContext, "出现错误，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //获取所有周的课表信息
    private void getKbInfo(final String year) {
        String url = UrlConstant.ALL_COURSE;
        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .add("semester", year)
                .build();

        HttpUtil.post(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    if (data.length() > 3) {
                        Res res = HttpUtil.handleResponse(data);
                        assert res != null;
                        if (res.getCode() == 200) {
                            try {
                                JSONArray jsonArray = new JSONArray(res.getInfo());
                                for (int k = 1; k <= jsonArray.length(); k++) {
                                    JSONObject object = jsonArray.getJSONObject(k - 1);
                                    JSONArray innerArray = object.getJSONArray(year + "_" + k);
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject monday = innerArray.getJSONObject(i);
                                        if (!monday.getString("monday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(1);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(monday.getString("monday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject tuesday = innerArray.getJSONObject(i);
                                        if (!tuesday.getString("tuesday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(2);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(tuesday.getString("tuesday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject wednesday = innerArray.getJSONObject(i);
                                        if (!wednesday.getString("wednesday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(3);
                                            course.setZhouci(k);
                                            course.setJieci((i * 2) + 1);
                                            course.setDes(wednesday.getString("wednesday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject thursday = innerArray.getJSONObject(i);
                                        if (!thursday.getString("thursday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(4);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(thursday.getString("thursday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject friday = innerArray.getJSONObject(i);
                                        if (!friday.getString("friday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(5);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(friday.getString("friday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject saturday = innerArray.getJSONObject(i);
                                        if (!saturday.getString("saturday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(6);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(saturday.getString("saturday"));
                                            course.save();
                                        }
                                    }
                                    for (int i = 0; i < 5; i++) {
                                        JSONObject sunday = innerArray.getJSONObject(i);
                                        if (!sunday.getString("sunday").equals("")) {
                                            DBCourse course = new DBCourse();
                                            course.setDay(7);
                                            course.setJieci((i * 2) + 1);
                                            course.setZhouci(k);
                                            course.setDes(sunday.getString("sunday"));
                                            course.save();
                                        }
                                    }

                                    DBDate dbDate = new DBDate();

                                    dbDate.setZhouci(k);
                                    JSONObject object1 = innerArray.getJSONObject(5);
                                    String month = object1.getString("month");
                                    dbDate.setMonth(month);

                                    JSONArray dateArray = object1.getJSONArray("date");
                                    StringBuilder date = new StringBuilder();
                                    for (int i = 0; i < 7; i++) {
                                        if (i != 6) {
                                            date.append(dateArray.getString(i)).append(",");
                                        } else {
                                            date.append(dateArray.getString(i));
                                        }
                                    }
                                    dbDate.setDate(date.toString());
                                    dbDate.save();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                    showKb(server_week);
                                }
                            });
                        } else {
                            Looper.prepare();
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    //显示课表
    private void showKb(final String week) {
        List<Course> list = new ArrayList<>();

        List<DBCourse> courseList = DataSupport.where("zhouci = ? ", week).find(DBCourse.class);
        List<String> stringList = new ArrayList<>();

        for (DBCourse dbCourse : courseList) {
            stringList.add(dbCourse.getDes());
        }

        List<String> listWithoutDup = new ArrayList<>(new HashSet<>(stringList));

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
                course.setBg_Color(color[listWithoutDup.indexOf(dbCourse.getDes())]);
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

//        Intssent updateIntent = new Intent(ACTION_UPDATE_ALL);
//        mContext.sendBroadcast(updateIntent);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });
    }

}
