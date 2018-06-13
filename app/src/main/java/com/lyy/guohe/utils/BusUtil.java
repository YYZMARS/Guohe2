package com.lyy.guohe.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BusUtil {

    //判断当前时刻有没有校车
    public static String hasSchoolBus() {
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

}
