package com.lyy.guohe.model;

public class News {

    //资讯图片地址
    private String imgUrl;

    //资讯图片title
    private String newsTitle;

    //资讯的详细链接
    private String newsLink;

    public News(String imgUrl, String newsTitle, String newsLink) {
        this.imgUrl = imgUrl;
        this.newsTitle = newsTitle;
        this.newsLink = newsLink;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsLink() {
        return newsLink;
    }

    public void setNewsLink(String newsLink) {
        this.newsLink = newsLink;
    }
}
