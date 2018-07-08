package com.lyy.guohe.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import com.flyco.dialog.widget.ActionSheetDialog;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.CropViewActivity;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourse;
import com.lyy.guohe.model.DBDate;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.DialogUtils;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.view.CourseTableView;
import com.lyy.guohe.view.Fab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
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

import static android.app.Activity.RESULT_OK;

public class KbFragment extends Fragment implements View.OnClickListener {

    private Activity mContext;

    private static final String TAG = "KbActivity";

    private CourseTableView courseTableView;

    private ProgressDialog mProgressDialog;

    private ImageView iv_course_table;

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


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (getActivity() != null) {
            mContext = getActivity();
        }

        all_year_list = new ArrayList<>();
        stu_id = SpUtils.getString(mContext, SpConstant.STU_ID);
        stu_pass = SpUtils.getString(mContext, SpConstant.STU_PASS);
        //记录服务器当前周
        server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
        //初始化界面
        View view = inflater.inflate(R.layout.fragment_kb, container, false);
        initView(view);
        return view;
    }

    //初始化View
    private void initView(View view) {
        tv_course_table_toolbar = (TextView) view.findViewById(R.id.tv_course_table_toolbar);
        //初始化课表的背景
        iv_course_table = view.findViewById(R.id.iv_course_table);
        String bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_kb_default).into(iv_course_table);
        }

        List<DBCourse> dbCourses = LitePal.findAll(DBCourse.class);

        //构造课表界面
        courseTableView = view.findViewById(R.id.ctv);

        courseTableView.setOnCourseItemClickListener((tv, jieci, day, des) -> DialogUtils.showCourseDialog(mContext, des));

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

//        FloatingActionButton mFab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
//        mFab1.setOnClickListener(this);
//        FloatingActionButton mFab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
//        mFab2.setOnClickListener(this);
//        FloatingActionButton mFab3 = (FloatingActionButton) view.findViewById(R.id.fab3);
//        mFab3.setOnClickListener(this);

        Fab fab = view.findViewById(R.id.fab);
        View sheetView = view.findViewById(R.id.fab_sheet);
        View overlay = view.findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.material_white_1000);
        int fabColor = getResources().getColor(R.color.material_white_1000);
        MaterialSheetFab materialSheetFab = new MaterialSheetFab(fab, sheetView, overlay, sheetColor, fabColor);

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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                    }
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
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> showKb(server_week));
                                }
                            }
                        } else {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> Toast.makeText(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show());
                            }
                        }

                    }
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
                    if (data.length() > 3) {
                        Res res = HttpUtil.handleResponse(data);
                        if (res != null) {
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
                                        //表示已经导入过课表
                                        SpUtils.putBoolean(mContext, SpConstant.IS_OPEN_KB, true);
                                        mContext.runOnUiThread(() -> {
                                            if (mProgressDialog.isShowing()) {
                                                mProgressDialog.dismiss();
                                            }
                                            showKb(server_week);
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                                Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        mContext.runOnUiThread(() -> {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show();
                        });
                    }

                } else {
                    mContext.runOnUiThread(() -> {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    //显示课表
    private void showKb(final String week) {
        List<Course> list = new ArrayList<>();

        List<DBCourse> courseList = LitePal.where("zhouci = ? ", week).find(DBCourse.class);
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

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        server_week = SpUtils.getString(mContext, SpConstant.SERVER_WEEK);
        if (server_week != null) {
            showKb(server_week);
        }
        String bg_course_64 = SpUtils.getString(mContext, SpConstant.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        }
        //更新课表小部件
        updateWidget();
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
                        new AlertDialog.Builder(mContext);
                singleChoiceDialog.setTitle("选择学年");
                // 第二个参数是默认选项，此处设置为0
                singleChoiceDialog.setSingleChoiceItems(items, 0,
                        (dialog, which) -> yearChoice = which);
                singleChoiceDialog.setPositiveButton("确定",
                        (radio_dialog, which) -> {
                            if (yearChoice != -1) {
                                mProgressDialog.setMessage("正在切换中,请稍后...");
                                mProgressDialog.show();
                                Log.d(TAG, "onClick: " + items[yearChoice]);
                                LitePal.deleteAll(DBCourse.class);
                                getKbInfo(items[yearChoice]);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.fab1:
//                //更新课表
//                LitePal.deleteAll(DBCourse.class);
//                SpUtils.remove(mContext, SpConstant.SERVER_WEEK);
//                SpUtils.remove(mContext, SpConstant.XIAO_LI);
//                SpUtils.remove(mContext, SpConstant.IS_OPEN_KB);
//                getXiaoLi();
//                break;
//            case R.id.fab2:
//                //更改学期
//                showSchoolYearChoiceDialog();
//                break;
//            case R.id.fab3:
//                //更改周次
//                showKbBgDialog(iv_course_table);
//                break;
        }
    }

    //弹出更换课表背景的对话框
    public void showKbBgDialog(ImageView imageView) {
        final String[] stringItems = {"从相册中选择", "重置为默认"};
        final ActionSheetDialog dialog = new ActionSheetDialog(mContext, stringItems, null);
        dialog.isTitleShow(false).show();
        dialog.setOnOperItemClickL((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, Constant.CHOOSE_PHOTO_FOR_KB); // 打开相册
                    break;
                case 1:
                    Glide.with(mContext).load(R.drawable.bg_kb_default).into(imageView);
                    SpUtils.remove(mContext, SpConstant.BG_COURSE_64);
                    break;
            }
            dialog.dismiss();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.CHOOSE_PHOTO_FOR_KB:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    // 4.4及以上系统使用这个方法处理图片
                    Uri uri = data.getData();
                    Intent cropIntent = new Intent(mContext, CropViewActivity.class);
                    if (uri != null) {
                        cropIntent.putExtra("uri", uri.toString());
                        cropIntent.putExtra("flag", "course");
                        startActivity(cropIntent);
                    } else {
                        mContext.runOnUiThread(() -> {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "出现异常，请稍后重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    //更新小部件
    private void updateWidget() {
        String WIDGET_UPDATE = "com.lyy.widget.UPDATE_ALL";
        Intent intent = new Intent(WIDGET_UPDATE);
        mContext.sendBroadcast(intent);
    }

}
