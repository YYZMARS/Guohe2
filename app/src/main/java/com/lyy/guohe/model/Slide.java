package com.lyy.guohe.model;

//轮播图对象
public class Slide {

    /**
     * describe : 第八届全国大学生绿植领养
     * img : http://p7gzvzwe4.bkt.clouddn.com/IMG_4157.JPG
     * title : 有种领养，为爱发声
     * url : https://m.weibo.cn/zt/schoolEvent?event=312&luicode=10000011&lfid=2302540002_6151
     */

    private String describe;
    private String img;
    private String title;
    private String url;

    public Slide(String describe, String img, String title, String url) {
        this.describe = describe;
        this.img = img;
        this.title = title;
        this.url = url;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
