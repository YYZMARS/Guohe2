package com.lyy.guohe.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.lyy.guohe.R;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.utils.StuUtils;
import com.tencent.stat.StatService;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class KbWidget extends AppWidgetProvider {

    private static final String TAG = "KbWidget";

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
            if (intent.getAction() != null) {
                Log.d(TAG, "onReceive: " + intent.getAction());
                switch (intent.getAction()) {
                    case Constant.KB_UPDATE:
                        //小部件更新事件
                        RemoteViews remoteViews = refreshKb(context);
                        Log.d(TAG, "onReceive: "+remoteViews);
                        appWidgetManager.updateAppWidget(componentName, remoteViews);
                        //统计refresh按钮被点击的次数
                        Properties prop = new Properties();
                        prop.setProperty("name", "refresh");
                        StatService.trackCustomKVEvent(context, "widget_refresh", prop);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: ");
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
        Log.d(TAG, "onEnabled: ");
        super.onEnabled(context);
    }

    public RemoteViews refreshKb(Context context) {
        String server_week = SpUtils.getString(context, SpConstant.SERVER_WEEK);

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
        int[] colors = {R.color.material_green_200,
                R.color.material_blue_200,
                R.color.material_pink_200,
                R.color.material_orange_A200,
                R.color.material_brown_200,
                R.color.material_deep_purple_200,
                R.color.material_yellow_100,
                R.color.material_cyan_200};


        //每天的课的集合
        List<Integer> single_index = new ArrayList<Integer>();
        single_index.add(R.id.widget_single_1);
        single_index.add(R.id.widget_single_2);
        single_index.add(R.id.widget_single_3);
        single_index.add(R.id.widget_single_4);
        single_index.add(R.id.widget_single_5);

        boolean isOpenKb = SpUtils.getBoolean(context, SpConstant.IS_OPEN_KB);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.kb_widget);
        remoteViews.setTextViewText(R.id.widget_week, "第" + server_week + "周");

        if (isOpenKb) {
            List<DBCourseNew> courseList = LitePal.findAll(DBCourseNew.class);
            //i表示周几
            for (int i = 1; i <= 7; i++) {
                RemoteViews nestedView = new RemoteViews(context.getPackageName(), R.layout.widget_single_layout);
                nestedView.removeAllViews(single_list.get(i - 1));
                for (DBCourseNew dbCourse : courseList) {
                    boolean isInThisWeek = StuUtils.isInThisWeek(Integer.parseInt(server_week), dbCourse.getZhouci());
                    if (isInThisWeek) {
                        if (dbCourse.getDay() == i) {
                            int jieci = dbCourse.getJieci();
                            String des = "";
                            des = dbCourse.getDes();
                            String courseInfo[] = des.split("@");
                            String courseClassroom = "";
                            String courseName = "";

                            if (courseInfo.length == 5) {
                                courseName = courseInfo[1];
                                courseClassroom = courseInfo[4];
                            }

                            if (courseInfo.length < 5) {
                                courseName = courseInfo[1];
                            }

                            String result = courseName + "@" + courseClassroom;
                            Random random = new Random();

                            switch (jieci) {
                                case 1:
                                    nestedView.setTextViewText(single_index.get(0), result);
                                    nestedView.setInt(single_index.get(0), "setBackgroundResource", colors[random.nextInt(8)]);
                                    break;
                                case 3:
                                    nestedView.setTextViewText(single_index.get(1), result);
                                    nestedView.setInt(single_index.get(1), "setBackgroundResource", colors[random.nextInt(8)]);
                                    break;
                                case 5:
                                    nestedView.setTextViewText(single_index.get(2), result);
                                    nestedView.setInt(single_index.get(2), "setBackgroundResource", colors[random.nextInt(8)]);
                                    break;
                                case 7:
                                    nestedView.setTextViewText(single_index.get(3), result);
                                    nestedView.setInt(single_index.get(3), "setBackgroundResource", colors[random.nextInt(8)]);
                                    break;
                                case 9:
                                    nestedView.setTextViewText(single_index.get(4), result);
                                    nestedView.setInt(single_index.get(4), "setBackgroundResource", colors[random.nextInt(8)]);
                                    break;
                            }
                        }
                    }
                }
                remoteViews.addView(single_list.get(i - 1), nestedView);
            }
        }
        return remoteViews;
    }
}

