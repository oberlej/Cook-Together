package com.cooktogether.model;

/**
 * Created by hela on 18/01/17.
 */

public enum StatusEnum {
    WAITING("Waiting"), ACCEPTED("Accepted"), REFUSED("Refused");
    private String status;

    StatusEnum(String s) {
        status = s;
    }

    public String getStatus() {
        return status;
    }
}
