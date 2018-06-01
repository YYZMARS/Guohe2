package com.lyy.guohe.model;

/**
 * Created by lyy on 2017/11/22.
 */

public class Sport {

    private String time;

    private String number;

    private String date;

    public Sport(String time, String number, String date) {
        this.time = time;
        this.number = number;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }
}
