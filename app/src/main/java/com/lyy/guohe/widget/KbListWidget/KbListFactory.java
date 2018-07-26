package com.lyy.guohe.widget.KbListWidget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.lyy.guohe.R;

import java.util.ArrayList;
import java.util.List;

public class KbListFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "CourseListViewFactory";

    private final Context mContext;
    public static List<String> mList = new ArrayList<String>();

    /*
     * 构造函数
     */
    public KbListFactory(Context context, Intent intent) {

        mContext = context;
    }

    /*
     * MyRemoteViewsFactory调用时执行，这个方法执行时间超过20秒回报错。
     * 如果耗时长的任务应该在onDataSetChanged或者getViewAt中处理
     */
    @Override
    public void onCreate() {
        // 需要显示的数据
        mList.clear();
    }

    /*
     * 当调用notifyAppWidgetViewDataChanged方法时，触发这个方法
     * 例如：ToDoListViewFactory.notifyAppWidgetViewDataChanged();
     */
    @Override
    public void onDataSetChanged() {

    }

    /*
     * 这个方法不用多说了把，这里写清理资源，释放内存的操作
     */
    @Override
    public void onDestroy() {
        mList.clear();
    }

    /*
     * 返回集合数量
     */
    @Override
    public int getCount() {
        return mList.size();
    }

    /*
     * 创建并且填充，在指定索引位置显示的View，这个和BaseAdapter的getView类似
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= mList.size())
            return null;
        String content = mList.get(position);

        // 创建在当前索引位置要显示的View
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.item_kb_list);

        Log.d(TAG, "getViewAt: " + content);
        String courseInfo[] = content.split("@");
        String jieci = "";
        String name = "";
        String classroom = "";
        if (courseInfo.length == 4) {
            jieci = courseInfo[0];
            name = courseInfo[2];
        } else if (courseInfo.length == 5) {
            jieci = courseInfo[0];
            name = courseInfo[2];
            classroom = courseInfo[4];
        }

        // 设置要显示的内容
//        rv.setTextViewText(R.id.tv_widget_course, content);
        rv.setTextViewText(R.id.now_course_jieci_1, jieci + "-" + (Integer.parseInt(jieci) + 1));
        rv.setTextViewText(R.id.widget_course_1_1, name);
        rv.setTextViewText(R.id.widget_course_1_2, classroom);

        // 填充Intent，填充在AppWdigetProvider中创建的PendingIntent
        Intent intent = new Intent();
        // 传入点击行的数据
        intent.putExtra("content", content);
        rv.setOnClickFillInIntent(R.id.ll_widget_course, intent);

        return rv;
    }

    /*
     * 显示一个"加载"View。返回null的时候将使用默认的View
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /*
     * 不同View定义的数量。默认为1（本人一直在使用默认值）
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /*
     * 返回当前索引的。
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * 如果每个项提供的ID是稳定的，即她们不会在运行时改变，就返回true（没用过。。。）
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }
}
