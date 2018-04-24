package com.lyy.guohe.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.R;
import com.lyy.guohe.activity.KbActivity;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.model.DBCourse;
import com.tencent.stat.StatService;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class KbWidget extends AppWidgetProvider {

    private final String WIDGET_UPDATE = "com.lyy.widget.UPDATE_ALL";

    /**
     * 接受广播事件
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null)
            return;
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, KbWidget.class);
            switch (Objects.requireNonNull(intent.getAction())) {
                case WIDGET_UPDATE:
                    //小部件更新事件
                    appWidgetManager.updateAppWidget(componentName, refreshKb(context));
                    //统计refresh按钮被点击的次数
                    Properties prop = new Properties();
                    prop.setProperty("name", "refresh");
                    StatService.trackCustomKVEvent(context, "widget_refresh", prop);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        appWidgetManager.updateAppWidget(appWidgetIds, refreshKb(context));
    }

    /**
     * 删除AppWidget
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * AppWidget首次创建调用
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public RemoteViews refreshKb(Context context) {
        String server_week = SpUtils.getString(context, SpConstant.SERVER_WEEK);
        int week = 0;
        if (server_week != null) {
            week = Integer.parseInt(server_week);
        }

        //每周的天数的集合
        List<Integer> single_list = new ArrayList<Integer>();
        single_list.add(R.id.widget_1);
        single_list.add(R.id.widget_2);
        single_list.add(R.id.widget_3);
        single_list.add(R.id.widget_4);
        single_list.add(R.id.widget_5);
        single_list.add(R.id.widget_6);
        single_list.add(R.id.widget_7);

        //背景颜色集合
        List<Integer> myImageList = new ArrayList<>();
        myImageList.add(R.drawable.course_info_blue);
        myImageList.add(R.drawable.course_info_brown);
        myImageList.add(R.drawable.course_info_cyan);
        myImageList.add(R.drawable.course_info_deep_orange);
        myImageList.add(R.drawable.course_info_deep_purple);
        myImageList.add(R.drawable.course_info_green);
        myImageList.add(R.drawable.course_info_indigo);
        myImageList.add(R.drawable.course_info_light_blue);
        myImageList.add(R.drawable.course_info_light_green);
        myImageList.add(R.drawable.course_info_lime);
        myImageList.add(R.drawable.course_info_orange);
        myImageList.add(R.drawable.course_info_pink);
        myImageList.add(R.drawable.course_info_purple);
        myImageList.add(R.drawable.course_info_teal);

        //每天的课的集合
        List<Integer> single_index = new ArrayList<Integer>();
        single_index.add(R.id.widget_single_1);
        single_index.add(R.id.widget_single_2);
        single_index.add(R.id.widget_single_3);
        single_index.add(R.id.widget_single_4);
        single_index.add(R.id.widget_single_5);

        List<DBCourse> courseList = DataSupport.where("zhouci = ? ", week + "").find(DBCourse.class);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.kb_widget);
        remoteViews.setTextViewText(R.id.widget_week, "第" + week + "周");
        //点击头部跳转到页面内
        Intent skipIntent = new Intent(context, KbActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 200, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_kbWidget, pi);

        for (int i = 1; i <= 7; i++) {
            RemoteViews nestedView = new RemoteViews(context.getPackageName(), R.layout.widget_single_layout);
            for (DBCourse dbCourse : courseList) {
                if (dbCourse.getDay() == i) {
                    int jieci = dbCourse.getJieci();
                    String des = "";
                    des = dbCourse.getDes();
                    String courseInfo[] = des.split("@");
                    String courseClassroom = "";
                    String courseName = "";


                    if (courseInfo.length == 2) {
                        courseName = courseInfo[1];
                    }
                    if (courseInfo.length == 3) {
                        courseName = courseInfo[1];
                    }
                    if (courseInfo.length == 4) {
                        courseName = courseInfo[1];
                        courseClassroom = courseInfo[3];
                    }

                    String result = courseName + "@" + courseClassroom;
                    Random random = new Random();
                    switch (jieci) {
                        case 1:
                            nestedView.setTextViewText(single_index.get(0), result);
                            nestedView.setInt(single_index.get(0), "setBackgroundResource", myImageList.get(random.nextInt(14)));
                            break;
                        case 3:
                            nestedView.setTextViewText(single_index.get(1), result);
                            nestedView.setInt(single_index.get(1), "setBackgroundResource", myImageList.get(random.nextInt(14)));
                            break;
                        case 5:
                            nestedView.setTextViewText(single_index.get(2), result);
                            nestedView.setInt(single_index.get(2), "setBackgroundResource", myImageList.get(random.nextInt(14)));
                            break;
                        case 7:
                            nestedView.setTextViewText(single_index.get(3), result);
                            nestedView.setInt(single_index.get(3), "setBackgroundResource", myImageList.get(random.nextInt(14)));
                            break;
                        case 9:
                            nestedView.setTextViewText(single_index.get(4), result);
                            nestedView.setInt(single_index.get(4), "setBackgroundResource", myImageList.get(random.nextInt(14)));
                            break;
                    }
                }
            }
            remoteViews.addView(single_list.get(i - 1), nestedView);
        }
        return remoteViews;
    }
}

