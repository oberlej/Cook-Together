package com.cooktogether.model;

/**
 * Created by jeremiaoberle on 12/6/16.
 */

public class Day implements Comparable<Day>{

//    private DayEnum dayEnum;
    private String name;
    private boolean lunch = false;
    private boolean dinner = false;

    public Day() {
    }

//    public Day(DayEnum dayEnum, String name, boolean lunch, boolean dinner) {
    public Day(String name, boolean lunch, boolean dinner) {
//        this.dayEnum = dayEnum;
        this.name = name;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    public Day(DayEnum dayEnum) {
        name = dayEnum.getName();
    }

//    public DayEnum getDayEnum() {
//        return dayEnum;
//    }

    public String getName() {
        return name;
    }

    public void setName(DayEnum name) {
        this.name = name.getName();
    }

    public boolean isLunch() {
        return lunch;
    }

    public void setLunch(boolean lunch) {
        this.lunch = lunch;
    }

    public boolean isDinner() {
        return dinner;
    }

    public void setDinner(boolean dinner) {
        this.dinner = dinner;
    }

    private DayEnum getDayEnum(){
        return DayEnum.valueOf(this.name.toUpperCase());
    }

    @Override
    public int compareTo(Day d) {
        int diff = getDayEnum().ordinal() - d.getDayEnum().ordinal();
        if (diff == 0) {
            return 0;
        } else if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Day day = (Day) o;

        return name != null ? name.equals(day.name) : day.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
