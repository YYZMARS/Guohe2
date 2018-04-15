package com.lyy.guohe2.model;

import java.io.Serializable;

public class Course implements Serializable {

    private int spanNum = 2;
    private int bg_Color;               //课程的背景颜色
    private int jieci;                  //节次
    private int day;                    //周几的课
    private String des;                 //课程描述
    private String ClassRoomName;       //教室
    private String ClassTypeName;       //课程号
    private String ClassName;           //课程名
    private String ClassTeacher;        //教师名

    public Course() {
    }

    public Course(int jieci, int day, String des) {
        this.jieci = jieci;
        this.day = day;
        this.des = des;
    }

    public Course(int jieci, String className, String classRoomName) {
        this.jieci = jieci;
        this.ClassName = className;
        this.ClassRoomName = classRoomName;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getClassTeacher() {
        return ClassTeacher;
    }

    public void setClassTeacher(String classTeacher) {
        ClassTeacher = classTeacher;
    }

    public int getBg_Color() {
        return bg_Color;
    }

    public void setBg_Color(int bg_Color) {
        this.bg_Color = bg_Color;
    }

    public int getJieci() {
        return jieci;
    }

    public void setJieci(int jieci) {
        this.jieci = jieci;
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

    public int getSpanNum() {
        return spanNum;
    }

    public void setSpanNum(int spanNum) {
        this.spanNum = spanNum;
    }

    public String getClassRoomName() {
        return ClassRoomName;
    }

    public void setClassRoomName(String classRoomName) {
        ClassRoomName = classRoomName;
    }

    public String getClassTypeName() {
        return ClassTypeName;
    }

    public void setClassTypeName(String classTypeName) {
        ClassTypeName = classTypeName;
    }

    @Override
    public String toString() {
        return "DBCourse [jieci=" + jieci + ", day=" + day + ", des=" + des
                + ", spanNun=" + spanNum + "]";
    }

}
