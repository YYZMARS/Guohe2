package com.lyy.guohe2.utils;

import android.content.Context;
import android.content.Intent;

public class NavigateUtil {

    //实现Activity之间的跳转
    public static void navigateTo(Context context, Class cls) {
        Intent intent=new Intent(context,cls);
        context.startActivity(intent);
    }
}
