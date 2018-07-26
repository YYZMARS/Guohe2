package com.lyy.guohe.model;

import org.litepal.crud.LitePalSupport;

public class DBCourseNew extends LitePalSupport {

    private int day;  //星期几的课

    private String des;      //课程的描述

    private int jieci;   //课程的节次

    private int zhouci; //课程的周次

    public int getZhouci() {
        return zhouci;
    }

    public void setZhouci(int zhouci) {
        this.zhouci = zhouci;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getJieci() {
        return jieci;
    }

    public void setJieci(int jieci) {
        this.jieci = jieci;
    }
}
