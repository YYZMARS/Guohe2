package com.lyy.guohe.fragment;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.lyy.guohe.activity.BrowserActivity;
import com.lyy.guohe.activity.ClassRoomActivity;
import com.lyy.guohe.activity.GameActivity;
import com.lyy.guohe.activity.KbActivity;
import com.lyy.guohe.activity.LibraryActivity;
import com.lyy.guohe.activity.ScoreActivity;
import com.lyy.guohe.activity.SportActivity;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.Course;
import com.lyy.guohe.model.DBCourse;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.utils.HttpUtil;
import com.lyy.guohe.utils.ListViewUtil;
import com.lyy.guohe.utils.NavigateUtil;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.R;
import com.lyy.guohe.adapter.CourseAdapter;
import com.lyy.guohe.constant.SpConstant;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
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
        tvKbShow.setText("今天居然没有课~" + "\uD83D\uDE01");

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
        LinearLayout navGame = view.findViewById(R.id.nav_game);
        navGame.setOnClickListener(this);

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
                    for (Course c : courses) {
                        Log.d(TAG, "initTodayKb: " + c.getClassName());
                    }
                    tvKbShow.setVisibility(View.GONE);
                    lvKbToday.setVisibility(View.VISIBLE);
                    CourseAdapter courseAdapter = new CourseAdapter(getActivity(), R.layout.item_course, courses);
                    lvKbToday.setAdapter(courseAdapter);
                    ListViewUtil.setListViewHeightBasedOnChildren(lvKbToday);
                }
            }
        }
    }

    //加载首页Message
    private void initMess() {
        HttpUtil.get(UrlConstant.GET_MSG, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> tvMessage.setText("服务器异常"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                if (response.isSuccessful()) {
                    final Res res = HttpUtil.handleResponse(data);
                    if (res!=null){
                        if (res.getCode() == 200) {
                            try {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                    String s = res.getInfo();
                                    tvMessage.setText(s.substring(2, s.length() - 2));
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> tvMessage.setText("服务器异常"));
                        }
                    }else {
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> tvMessage.setText("服务器异常"));
                    }
                } else {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> tvMessage.setText("服务器异常"));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_kb:
                //跳转至课表
                NavigateUtil.navigateTo(getActivity(), KbActivity.class);
                break;
            case R.id.nav_grade:
                //跳转至成绩查询界面
                NavigateUtil.navigateTo(getActivity(), ScoreActivity.class);
                break;
            case R.id.nav_library:
                //跳转至图书馆藏查询
                NavigateUtil.navigateTo(getActivity(), LibraryActivity.class);
                break;
            case R.id.nav_bus:
                //显示即将到来的校车
                showBusDialog(hasSchoolBus());
                break;
            case R.id.nav_cet:
                Intent intent = new Intent(getActivity(), BrowserActivity.class);
                intent.putExtra("url", UrlConstant.CET);
                intent.putExtra("title", "全国大学英语四六级考试成绩查询");
                intent.putExtra("isVpn", false);
                startActivity(intent);
                break;
            case R.id.nav_classroom:
                //跳转至查询空教室
                NavigateUtil.navigateTo(getActivity(), ClassRoomActivity.class);
                break;
            case R.id.nav_pe:
                //显示可以进入的体育系统
                showPEDialog();
                break;
            case R.id.nav_system:
                //显示可以进入的校园系统
                showSystemDialog();
                break;
            case R.id.nav_game:
                //显示可以玩的小游戏
                showGameDialog();
                break;
        }
    }

    //选择进入哪一个校园系统
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

    //显示校车对话框
    private void showBusDialog(String mess) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getActivity());
        normalDialog.setMessage("即将到来的车次是：\n" + mess);
        normalDialog.setPositiveButton("显示全部车次",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Intent intent = new Intent(getActivity(), BrowserActivity.class);
                        intent.putExtra("title", "校车时刻表");
                        intent.putExtra("url", UrlConstant.SCHOOL_BUS);
                        intent.putExtra("isVpn", false);
                        startActivity(intent);
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }

    //判断当前时刻有没有校车
    private String hasSchoolBus() {
        Calendar calendar = Calendar.getInstance();
        //星期天从0开始
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        Date c = new Date(System.currentTimeMillis());
        String checi = "";
        try {
            String str1 = f.format(c);
            //当前时间,格式为HH：mm
            Date d1 = f.parse(str1);
            if (f.parse("21:55").compareTo(d1) == -1) {
                //没有车了
                checi = "目前没有车了";
            } else if (f.parse("21:05").compareTo(d1) == -1) {
                //16车次
                checi = "车次16\n";
                checi += "21:45 西->21：55 南->东";
            } else if (f.parse("19:40").compareTo(d1) == -1) {
                if (week == 6 || week == 0) {
                    //14
                    //有些车周末周六不开，这种情况跳到下一班车
                    checi = "车次14\n";
                    checi += "19:30 西->19:40 南->东";
                } else {
                    //15
                    checi = "车次15\n";
                    checi += "20:55 西->21：05 南->东";
                }
            } else if (f.parse("18:40").compareTo(d1) == -1) {
                if (week == 0) {
                    //13
                    checi = "车次13\n";
                    checi += "18:30 东->18:40 南->西";
                } else {
                    //14
                    checi = "车次14\n";
                    checi += "19:30 西->19:40 南->东";
                }
            } else if (f.parse("17:50").compareTo(d1) == -1) {
                //13
                checi = "车次13\n";
                checi += "18:30 东->18:40 南->西";
            } else if (f.parse("16:05").compareTo(d1) == -1) {
                //12,11
                checi = "车次11和12\n";
                checi += "17：40 东->17:50 南->西\n";
                checi += "17:40 西->17：50 南->东";
            } else if (f.parse("15:25").compareTo(d1) == -1) {
                if (week == 0) {
                    //9
                    checi = "车次9\n";
                    checi += "15：15 东->15：25 南->西";
                } else {
                    //10
                    checi = "车次10\n";
                    checi += "15：55 西->16：05 南->东";
                }
            } else if (f.parse("13:45").compareTo(d1) == -1) {
                if (week == 0) {
                    //7,8
                    checi = "车次7和8\n";
                    checi += "13:35 东->13：45 南->西\n";
                    checi += "13:35 西->13:45 南->东";
                } else {
                    //9
                    checi = "车次9\n";
                    checi += "15：15 东->15：25 南->西";
                }
            } else if (f.parse("12:00").compareTo(d1) == -1) {
                //7,8
                checi = "车次7和8\n";
                checi += "13:35 东->13：45 南->西\n";
                checi += "13:35 西->13:45 南->东";
            } else if (f.parse("10:05").compareTo(d1) == -1) {
                //5,6
                checi = "车次5和6\n";
                checi += "11:50 东->12:00 南->西\n";
                checi += "11:50 西->12:00 南->东";
            } else if (f.parse("9:30").compareTo(d1) == -1) {
                if (week == 0) {
                    //3
                    checi = "车次3\n";
                    checi += "9:20 东->9:30 南->西\n";
                } else {
                    //4
                    checi = "车次4\n";
                    checi += "9:55 东->10:05 南->西\n";
                }
            } else if (f.parse("7:45").compareTo(d1) == -1) {
                //3
                checi = "车次3\n";
                checi += "9:20 东->9:30 南->西\n";

            } else {
                //2,1
                checi = "车次1和2\n";
                checi += "7:35 东->7:45 南->西\n";
                checi += "7:35 西->7:45 南->东";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return checi;
    }

    //选择进入哪一个体育系统
    private void showPEDialog() {
        final String[] items = {"俱乐部查询", "早操出勤查询", "体育成绩查询"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(getActivity());
        listDialog.setTitle("选择要进入的系统");
        listDialog.setItems(items, (dialog, which) -> {
            // which 下标从0开始

            Intent intent = new Intent(getActivity(), SportActivity.class);
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
//                    intent.putExtra("url", UrlConstant.LAB_URL);
//                    intent.putExtra("type", "3");
//                    startActivity(intent);
                    Toasty.success(getActivity(), "敬请期待", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        listDialog.show();
    }


    //弹出选择小游戏对话框
    private void showGameDialog() {
        final String[] items = {"六角消除", "2048", "六角拼拼", "无尽之旅", "彩虹穿越", "西部枪手", "经典纸牌", "三塔扑克", "Flappy Bird", "飞行小游戏"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(getActivity());
        listDialog.setTitle("请选择你要进入的游戏");
        listDialog.setItems(items, (dialog, which) -> {
            // which 下标从0开始
            // ...To-do
            Intent intent = new Intent(getActivity(), GameActivity.class);
            intent.putExtra("which", which);
            startActivity(intent);

        });
        listDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        initTodayKb();
    }
}