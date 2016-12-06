package com.cooktogether.model;

/**
 * Created by jeremiaoberle on 12/6/16.
 */

public enum DayEnum {
    MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY("Thursday"), FRIDAY("Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");

    public String getName() {
        return name;
    }

    private String name;

    DayEnum(String n) {
        name = n;
    }
}