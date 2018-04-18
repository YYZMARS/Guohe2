package com.lyy.guohe2.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.flyco.dialog.widget.MaterialDialog;
import com.githang.statusbar.StatusBarCompat;
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
import com.tencent.stat.StatService;

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

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KbActivity extends AppCompatActivity {

//    private final String ACTION_UPDATE_ALL = "com.lyy.widget.UPDATE_ALL";

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

    private TextView tv_course_table_toolbar;

    private List<String> all_year_list;

    private String stu_id;
    private String stu_pass;
    private String current_year;

    private String server_week;     //服务器当前周

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kb);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(33, 150, 243));
        mContext = this;

        all_year_list = new ArrayList<>();

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.course_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        tv_course_table_toolbar = (TextView) findViewById(R.id.tv_course_table_toolbar);

        //初始化课表的背景
        iv_course_table = (ImageView) findViewById(R.id.iv_course_table);
        bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_course_table);
        }

        //构造课表界面

        courseTableView = (CourseTableView) findViewById(R.id.ctv);

        courseTableView.setOnCourseItemClickListener(new CourseTableView.OnCourseItemClickListener() {
            @Override
            public void onCourseItemClick(TextView tv, int jieci, int day, String des) {
//                String string = tv.getText().toString();
                Log.d(TAG, "onCourseItemClick: " + des);
                showCourseDialog(des);
            }
        });

        mProgressDialog = new ProgressDialog(KbActivity.this);
        mProgressDialog.setMessage("课表导入中,请稍后……");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
        stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);

        server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
        if (server_week != null) {
            tv_course_table_toolbar.setText("第" + server_week + "周");

            Calendar calendar = Calendar.getInstance();
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            //判断今天是不是周一
            if (weekday == 2) {
                //判断是否第一次导入课表，默认false，没有导入课表
                boolean first_open_course = SpUtils.getBoolean(mContext, SpConstant.FIRST_OPEN_COURSE);
                if (first_open_course) {
                    getXiaoLi();
                    SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
                } else {
                    showKb(server_week);
                }
            } else {
                //判断是否第一次导入课表，默认false，没有导入课表
                boolean first_open_course = SpUtils.getBoolean(mContext, SpConstant.FIRST_OPEN_COURSE);
                if (first_open_course) {
                    getXiaoLi();
                    SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
                } else {
                    showKb(server_week);
                }
            }

            List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);
            if (dbCourses.size() == 0) {
                getXiaoLi();
                SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
            }

            LinearLayout ll_week = (LinearLayout) findViewById(R.id.ll_week);
            ll_week.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showWeekChoiceDialog(0);
                }
            });

        } else {
            getXiaoLi();
            SpUtils.putBoolean(mContext, SpConstant.FIRST_OPEN_COURSE, false);
        }
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
        runOnUiThread(new Runnable() {
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
                    runOnUiThread(new Runnable() {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_course_table_toolbar.setText("第" + server_week + "周");
                                }
                            });
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
                        runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
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
                            runOnUiThread(new Runnable() {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    runOnUiThread(new Runnable() {
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_week:
                showWeekChoiceDialog(0);
                break;
            case R.id.action_update_course:
                DataSupport.deleteAll(DBCourse.class);
                SpUtils.remove(mContext, SpConstant.SERVER_WEEK);
                SpUtils.remove(mContext, SpConstant.XIAO_LI);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getXiaoLi();
                    }
                });
                break;
            case R.id.action_change_year:
                showSchoolYearChoiceDialog();
                break;
            case R.id.action_change_background:
                final String[] stringItems = {"从相册中选择", "重置为默认"};
                final ActionSheetDialog dialog = new ActionSheetDialog(KbActivity.this, stringItems, null);
                dialog.isTitleShow(false).show();

                dialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_course_table);
                                SpUtils.remove(mContext, SpConstant.BG_COURSE_64);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_change_current_week:
                showWeekChoiceDialog(1);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        Uri uri = data.getData();
                        Intent cropIntent = new Intent(KbActivity.this, CropViewActivity.class);
                        assert uri != null;
                        cropIntent.putExtra("uri", uri.toString());
                        cropIntent.putExtra("flag", "course");
                        startActivity(cropIntent);
                    } else {
                        Toasty.error(KbActivity.this, "安卓版本过低", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bg_course_64 = SpUtils.getString(this, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        }
        //更新课表小部件
        updateWidget();
        StatService.onResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    //显示单选dialog
    int yearChoice;

    private void showSchoolYearChoiceDialog() {
        String xiaoli = SpUtils.getString(mContext, SpConstant.XIAO_LI);
        if (xiaoli != null) {
            try {
                JSONObject object = new JSONObject(xiaoli);
                JSONArray jsonArray = object.getJSONArray("all_year");
                //获取当前周数
                server_week = object.getString("weekNum");
                for (int i = 0; i < jsonArray.length(); i++) {
                    all_year_list.add(jsonArray.get(i).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tv_course_table_toolbar.setText("第" + server_week + "周");
            List<String> listWithoutDup = new ArrayList<>(new HashSet<>(all_year_list));

            if (listWithoutDup.size() != 0) {
                final String[] items = new String[listWithoutDup.size()];
                for (int i = 0; i < listWithoutDup.size(); i++) {
                    items[i] = listWithoutDup.get(i);
                }
                yearChoice = 0;
                AlertDialog.Builder singleChoiceDialog =
                        new AlertDialog.Builder(KbActivity.this);
                singleChoiceDialog.setTitle("选择学年");
                // 第二个参数是默认选项，此处设置为0
                singleChoiceDialog.setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                yearChoice = which;
                            }
                        });
                singleChoiceDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface radio_dialog, int which) {
                                if (yearChoice != -1) {
                                    mProgressDialog.setMessage("正在切换中,请稍后...");
                                    mProgressDialog.show();
                                    Log.d(TAG, "onClick: " + items[yearChoice]);
                                    DataSupport.deleteAll(DBCourse.class);
                                    getKbInfo(items[yearChoice]);
                                }
                            }
                        });
                singleChoiceDialog.show();
            } else {
                Toasty.error(mContext, "暂未获取到你的所有学年，请退出后重试", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toasty.error(mContext, "暂未获取你的学年，请点击更新课表后重试", Toast.LENGTH_SHORT).show();
        }

    }

    int weekChoice;

    private void showWeekChoiceDialog(final int code) {
        //code为0表示切换周次，code为1表示更换当前周
        final String[] items = {"第一周", "第二周", "第三周", "第四周", "第五周", "第六周", "第七周", "第八周", "第九周", "第十周", "第十一周", "第十二周", "第十三周", "第十四周", "第十五周", "第十六周", "第十七周", "第十八周", "第十九周", "第二十周"};
        weekChoice = 0;
        if (server_week != null) {
            weekChoice = Integer.parseInt(server_week);
        }
        AlertDialog.Builder singleChoiceDialog = new AlertDialog.Builder(KbActivity.this);
        singleChoiceDialog.setTitle("请选择周次");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, weekChoice - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weekChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (weekChoice != -1) {
                            if (code == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Integer.parseInt(server_week) != (weekChoice + 1)) {
                                            tv_course_table_toolbar.setText("第" + (weekChoice + 1) + "周(非本周)");
                                        } else {
                                            tv_course_table_toolbar.setText("第" + (weekChoice + 1) + "周");
                                        }
                                        showKb((weekChoice + 1) + "");
                                    }
                                });
                            } else if (code == 1) {
                                SpUtils.putString(mContext, SpConstant.SERVER_WEEK, (weekChoice + 1) + "");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_course_table_toolbar.setText("第" + (weekChoice + 1) + "周");
                                    }
                                });
                                showKb((weekChoice + 1) + "");
                            }

                        }
                    }
                });
        AlertDialog dialog = singleChoiceDialog.create();
        dialog.show();

        // 将对话框的大小按屏幕大小的百分比设置
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth() * 0.8); //设置宽度
        lp.height = (int) (display.getHeight() * 0.7);
        dialog.getWindow().setAttributes(lp);
    }

    //更新小部件
    private void updateWidget() {
        String WIDGET_UPDATE = "com.lyy.widget.UPDATE_ALL";
        Intent intent = new Intent(WIDGET_UPDATE);
        sendBroadcast(intent);
    }
}
