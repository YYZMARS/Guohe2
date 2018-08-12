package com.lyy.guohe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.lyy.guohe.activity.BrowserActivity;

import java.util.HashMap;
import java.util.Map;

public class NavigateUtil {

    /**
     * 功能描述:简单地Activity的跳转(不携带任何数据)
     *
     * @param activity 发起跳转的Activity实例
     * @param cls      目标Activity实例
     */
    public static void navigateTo(Activity activity, Class<? extends Activity> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
    }

    /**
     * 功能描述：带数据的Activity之间的跳转
     *
     * @param activity 发起跳转的Activity实例
     * @param cls      目标Activity实例
     * @param hashMap  要传递的参数列表
     */
    public static void navigateTo(Activity activity, Class<? extends Activity> cls, HashMap<String, ?> hashMap) {
        Intent intent = new Intent(activity, cls);
        for (Object o : hashMap.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) o;
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                intent.putExtra(key, (String) value);
            }
            if (value instanceof Boolean) {
                intent.putExtra(key, (boolean) value);
            }
            if (value instanceof Integer) {
                intent.putExtra(key, (int) value);
            }
            if (value instanceof Float) {
                intent.putExtra(key, (float) value);
            }
            if (value instanceof Double) {
                intent.putExtra(key, (double) value);
            }
        }
        activity.startActivity(intent);
    }

    //跳转至不需要使用VPN的页面
    public static void navigateToUrlWithoutVPN(Context context, String title, String url) {
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("isVpn", false);
        context.startActivity(intent);
    }
}
