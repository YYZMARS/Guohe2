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

    private static final String TAG = "KbFragment";

    //获取学生的所有学年信息
    public static void getAllYear(Context context) {
        boolean isHaveXiaoli = SpUtils.getBoolean(context, SpConstant.IS_HAVE_XIAOLI, false);
        if (!isHaveXiaoli) {
            String stu_id = SpUtils.getString(context, SpConstant.STU_ID);
            String stu_pass = SpUtils.getString(context, SpConstant.STU_PASS);
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

    //返回学生的课程信息
    public static void handleCourseInfo(int jieci, int day, String des) {
        String[] dess = des.split("---------------------");
        for (int i = 0; i < dess.length; i++) {
            List<Integer> list = zhouci(dess[i]);
            for (int j = 0; j < list.size(); j++) {
                DBCourseNew dbCourse = new DBCourseNew();
                dbCourse.setJieci(jieci);
                dbCourse.setDes(dess[i]);
                dbCourse.setDay(day);
                dbCourse.setZhouci(list.get(j));
                dbCourse.save();
            }
        }
    }

    //返回周次的集合
    public static List<Integer> zhouci(String s) {
        List<Integer> list = new ArrayList<>();
        //获取所有的周次
        String zhouci = s.split("@")[3];
        /**
         * 原本的周次字符串可能为“1-6,7,8,9-13（周）”
         * 所以需要对字符串进行匹配
         * 第一步：取出字符串中的“(周)”
         * 第二步：按照逗号分割
         * 第三步：判断是否有“-”，如果有则将“-”左右范围内的数字添加进list，如果没有直接添加数字
         */
        String[] zhoucis = zhouci.substring(0, zhouci.length() - 3).split(",");
        for (String s1 : zhoucis) {
            if (s1.contains("-")) {
                String[] ss = s1.split("-");
                int begin = Integer.parseInt(ss[0]);
                int end = Integer.parseInt(ss[1]);
                for (int i = begin; i <= end; i++) {
                    list.add(i);
                }
            } else {
                list.add(Integer.parseInt(s1));
            }
        }
        return list;
    }
}
