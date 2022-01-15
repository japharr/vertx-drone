package com.jeliiadesina.drone.exception;

public class NotFoundException extends RuntimeException {
    private int code = 404;

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
