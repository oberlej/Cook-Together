package com.cooktogether.model;

import java.util.Calendar;

/**
 * Created by hela on 17/01/17.
 */

public class Date {
    private int year;
    private int month;
    private int day;
    private int hrs;
    private int min;

    public Date(java.util.Date date) {
        year = date.getYear();
        month = date.getMonth();
        day = date.getDate();
        hrs = date.getHours();
        min = date.getMinutes();
    }

    public Date(int year, int month, int day, int hrs, int min) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hrs = hrs;
        this.min = min;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHrs() {
        return hrs;
    }

    public int getMin() {
        return min;
    }

    @Override
    public String toString() {
        String d;
        java.util.Date date = new java.util.Date(year, month, day, hrs, min);

        if (this.min < 10)
            d = this.hrs + ":0" + this.min;
        else
            d = this.hrs + ":" + this.min;
        Calendar calendar = Calendar.getInstance();
        if (this.year == calendar.getTime().getYear()) {
            if (this.month == calendar.getTime().getMonth() && this.day == calendar.getTime().getDate()) {
                return d;
            } else {
                String sDate =  date.toLocaleString();
                String toReplace=""+(this.year+1900);
                sDate = sDate.replace(toReplace, " "); // year is stored as actual year -1900
                sDate = sDate.replace(":00 "," "); //00 for the seconds because here we don't store them
                d = sDate;
            }
        } else
            d = date.toLocaleString().replace(":00 "," ");
        return d;
    }
}
