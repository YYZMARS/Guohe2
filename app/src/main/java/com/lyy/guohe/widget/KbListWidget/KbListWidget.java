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
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.activity.KbActivity;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.service.KbListService;
import com.lyy.guohe.utils.SpUtils;
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

    String WIDGET_UPDATE = "com.lyy.widget.UPDATE_ALL";
    String WIDGET_CLICK = "com.lyy.widget.CLICK";

    int i = 0;

    private RemoteViews remoteViews;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context, KbListWidget.class);

        // 创建一个RemoteView
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.kb_list_widget);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, KbListService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);

        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.course_widget_list, intent);

        // 设置当显示的widget_list为空显示的View
        remoteViews.setEmptyView(R.id.course_widget_list, R.layout.none_data);

        // 点击列表触发事件
        Intent clickIntent = new Intent(context, KbListWidget.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(WIDGET_CLICK);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));

        //点击头部跳转到页面内
        Intent skipIntent = new Intent(context, KbActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 200, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_widget_course, pi);

        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setPendingIntentTemplate(R.id.tv_course, pendingIntentTemplate);

        // 刷新按钮
        final Intent refreshIntent = new Intent(context, KbListWidget.class);
        refreshIntent.setAction(WIDGET_UPDATE);
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
            if (action.equals(WIDGET_UPDATE)) {
                // 刷新Widget
                final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                final ComponentName cn = new ComponentName(context, KbListWidget.class);

                KbListFactory.mList.clear();

                Calendar calendar = Calendar.getInstance();
                int weekday = calendar.get(Calendar.DAY_OF_WEEK);

                int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

                String server_week = SpUtils.getString(context, SpConstant.SERVER_WEEK);
                if (server_week != null) {
                    List<DBCourseNew> courseList = LitePal.where("zhouci = ? ", server_week).find(DBCourseNew.class);
                    if (courseList.size() > 0) {
                        for (int i = 0; i < courseList.size(); i++) {
                            if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                                KbListFactory.mList.add(courseList.get(i).getJieci() + "@" + courseList.get(i).getDes());
                            }
                        }
                    }
                }
                // 这句话会调用RemoteViewSerivce中RemoteViewsFactory的onDataSetChanged()方法。
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.course_widget_list);

                //统计refresh的次数
                Properties prop = new Properties();
                prop.setProperty("name", "refresh");
                StatService.trackCustomKVEvent(context, "widget_refresh", prop);

            } else if (action.equals(WIDGET_CLICK)) {
                // 单击Wdiget中ListView的某一项会显示一个Toast提示。
                Toast.makeText(context, intent.getStringExtra("content"),
                        Toast.LENGTH_SHORT).show();
            }
            i++;
        }

    }
}

