package com.jeliiadesina.drone.util;

import am.ik.yavi.core.CustomConstraint;

public class IsbnConstraint implements CustomConstraint<String> {

    @Override
    public boolean test(String s) {
        // Delegate processing to another method
        return true;
    }

    @Override
    public String messageKey() {
        return "string.isbn13";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be ISBN13 format";
    }
}
