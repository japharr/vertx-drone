package com.jeliiadesina.drone.exception;

public class AlreadyExistException extends RuntimeException {
    private int code = 400;

    public AlreadyExistException() {
        super();
    }

    public AlreadyExistException(String message) {
        super(message);
    }

    public AlreadyExistException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
