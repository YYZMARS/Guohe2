package com.lyy.guohe.constant;

/**
 * 该部分记录APP所需要创建的SharePreference中的键的信息
 */
public class SpConstant {

    //记录是否已经登录
    public static final String IS_LOGIN = "isLogIn";

    //记录base64码的照片
    public static final String IMAGE_BASE_64 = "image_base_64";

    //记录姓名
    public static final String STU_NAME = "stu_name";

    //记录所在学院
    public static final String STU_ACADEMY = "stu_academy";

    //记录所在专业
    public static final String STU_MAJOR = "stu_major";

    //记录学号
    public static final String STU_ID = "stu_id";

    //记录密码
    public static final String STU_PASS = "stu_pass";

    //记录课表的背景图片的base64
    public static final String BG_COURSE_64 = "bg_course_64";

    //记录服务器上的当前周次
    public static final String SERVER_WEEK = "server_week";

    //记录是否导入课程表,true表示已经导入过课表，false表示未导入课表，默认为false
    public static final String IS_OPEN_KB = "is_open_kb";

    //记录校历信息
    public static final String XIAO_LI = "xiao_li";

    //记录该学生的所有学年
    public static final String ALL_YEAR = "all_year";

    //记录体育课密码
    public static final String PE_PASS = "pe_pass";

    //判断是否已经有校历信息，默认false表示没有导入校历信息
    public static final String IS_HAVE_XIAOLI = "is_have_xiaoli";

    //云端推送是否已读的flag
    public static final String IS_MESS_READ = "is_mess_read";

    //云端推送信息的id
    public static final String MESS_ID = "mess_id";

    //云端推送的url
    public static final String MESS_URL = "mess_url";

    //云端推送的内容content
    public static final String MESS_CONTENT = "mess_content";
}
