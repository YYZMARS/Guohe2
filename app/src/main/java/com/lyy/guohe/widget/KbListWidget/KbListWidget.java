package com.lyy.guohe.widget.KbListWidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.service.KbListService;
import com.lyy.guohe.utils.SpUtils;
import com.lyy.guohe.utils.StuUtils;
import com.tencent.stat.StatService;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of App Widget functionality.
 */
public class KbListWidget extends AppWidgetProvider {

    private static final String TAG = "KbListWidget";

    int i = 0;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context, KbListWidget.class);

        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.kb_list_widget);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, KbListService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);

        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.course_widget_list, intent);

        // 刷新按钮
        final Intent refreshIntent = new Intent(context, KbListWidget.class);
        refreshIntent.setAction(Constant.KB_LIST_UPDATE);
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_refresh_course, refreshPendingIntent);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        //统计refresh按钮被点击的次数
        Properties prop = new Properties();
        prop.setProperty("name", "refresh");
        StatService.trackCustomKVEvent(context, "widget_refresh", prop);
    }

    /**
     * 接收Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (action != null) {
            Log.d(TAG, "onReceive: " + action);
            if (action.equals(Constant.KB_LIST_UPDATE)) {
                //统计refresh的次数
                Properties prop = new Properties();
                prop.setProperty("name", "refresh");
                StatService.trackCustomKVEvent(context, "widget_refresh", prop);

                showKbList(context);
            }
            i++;
        }
    }

    private void showKbList(Context context) {
        // 刷新Widget
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, KbListWidget.class);

        KbListFactory.mList.clear();

        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

        String server_week = SpUtils.getString(context, SpConstant.SERVER_WEEK);
        if (server_week != null) {
            List<DBCourseNew> courseList = LitePal.findAll(DBCourseNew.class);
            for (DBCourseNew dbCourse : courseList) {
                boolean isInThisWeek = StuUtils.isInThisWeek(Integer.parseInt(server_week), dbCourse.getZhouci());
                if (isInThisWeek) {
                    if (dbCourse.getDay() == a[weekday])
                        KbListFactory.mList.add(dbCourse.getJieci() + "@" + dbCourse.getDes());
                }
            }

            ComponentName thisWidget = new ComponentName(context, KbListWidget.class);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.kb_list_widget);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            if (KbListFactory.mList.size() == 0) {
                Log.d(TAG, "showKbList: 今天没课");
                remoteViews.setViewVisibility(R.id.course_widget_list, View.GONE);
                remoteViews.setViewVisibility(R.id.tv_kb_list, View.VISIBLE);
                remoteViews.setTextViewText(R.id.tv_kb_list, "今天居然没有课~" + "\uD83D\uDE01");

                appWidgetManager.partiallyUpdateAppWidget(appWidgetManager.getAppWidgetIds(thisWidget), remoteViews);
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            } else {
                remoteViews.setViewVisibility(R.id.course_widget_list, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.tv_kb_list, View.GONE);
                appWidgetManager.partiallyUpdateAppWidget(appWidgetManager.getAppWidgetIds(thisWidget), remoteViews);
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            }
        }

        // 这句话会调用RemoteViewSerivce中RemoteViewsFactory的onDataSetChanged()方法。
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.course_widget_list);
    }
}

