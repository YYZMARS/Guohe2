package com.lyy.guohe.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.lyy.guohe.App;
import com.lyy.guohe.constant.Constant;
import com.lyy.guohe.constant.SpConstant;
import com.lyy.guohe.constant.UrlConstant;
import com.lyy.guohe.model.DBCourseNew;
import com.lyy.guohe.model.Res;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StuUtils {

    //获取学生的所有学年信息
    public static void getAllYear(Context context) {
        boolean isHaveXiaoli = SpUtils.getBoolean(context, SpConstant.IS_HAVE_XIAOLI, false);
        if (!isHaveXiaoli) {
            String stu_id = SpUtils.getString(context, SpConstant.STU_ID);
            String stu_pass = SpUtils.getString(context, SpConstant.STU_PASS);
            if (stu_id != null && stu_pass != null) {
                RequestBody requestBody = new FormBody.Builder()
                        .add(Constant.STU_ID, stu_id)
                        .add(Constant.STU_PASS, stu_pass)
                        .build();
                HttpUtil.post(UrlConstant.XIAO_LI, requestBody, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String data = response.body().string();
                            Res res = HttpUtil.handleResponse(data);
                            if (res != null) {
                                if (res.getCode() == 200) {
                                    try {
                                        JSONObject object = new JSONObject(res.getInfo());
                                        JSONArray array = object.getJSONArray("all_year");
                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 0; i < array.length(); i++) {
                                            sb.append(array.get(i)).append("@");
                                        }
                                        String weekNum = object.getString("weekNum");
                                        if (Integer.parseInt(weekNum) > 20)
                                            weekNum = "1";
                                        SpUtils.putString(context, SpConstant.ALL_YEAR, sb.toString());
                                        SpUtils.putBoolean(context, SpConstant.IS_HAVE_XIAOLI, true);
                                        SpUtils.putString(context, SpConstant.SERVER_WEEK, weekNum);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    //返回学生的课程信息
    public static void handleCourseInfo(int jieci, int day, String des) {
        String[] dess = des.split("---------------------");
        if (dess.length > 1) {
            for (String des1 : dess) {
                DBCourseNew dbCourse = new DBCourseNew();
                dbCourse.setJieci(jieci);
                dbCourse.setDes(des1);
                dbCourse.setDay(day);
                dbCourse.setZhouci(zhouci(des1));
                dbCourse.setRepeat(true);
                dbCourse.save();
            }
        } else {
            DBCourseNew dbCourse = new DBCourseNew();
            dbCourse.setJieci(jieci);
            dbCourse.setDes(des);
            dbCourse.setDay(day);
            dbCourse.setZhouci(zhouci(des));
            dbCourse.setRepeat(false);
            dbCourse.save();
        }
    }

    //返回周次
    public static String zhouci(String s) {
        return s.split("@")[3];
    }

    //判断当前周是否在该课的周次内
    public static boolean isInThisWeek(int week, String s) {
        /**
         * 原本的周次字符串可能为“1-6,7,8,9-13（周）”
         * 所以需要对字符串进行匹配
         * 第一步：取出字符串中的“(周)”
         * 第二步：按照逗号分割
         * 第三步：判断是否有“-”，如果有则将“-”左右范围内的数字添加进list，如果没有直接添加数字
         */
        String[] zhoucis = s.substring(0, s.length() - 3).split(",");
        for (String s1 : zhoucis) {
            if (s1.contains("-")) {
                String[] ss = s1.split("-");
                int begin = Integer.parseInt(ss[0]);
                int end = Integer.parseInt(ss[1]);
                if (week >= begin && week <= end)
                    return true;
            } else {
                if (week == Integer.parseInt(s1))
                    return true;
            }
        }
        return false;

    }
}
