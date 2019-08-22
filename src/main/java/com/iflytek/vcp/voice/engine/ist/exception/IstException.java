package com.iflytek.vcp.voice.engine.ist.exception;

import java.io.Serializable;

public class IstException extends Exception implements Serializable {
    private static final long serialVersionUID = 5018770229232677878L;

    private int errorCode = 1000;

    public IstException() {
    }

    public IstException(int errorCode, String arg0) {
        super(arg0);
        this.errorCode = errorCode;
    }

    public IstException(String arg0) {
        super(arg0);
    }

    public IstException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public IstException(int errorCode, String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.errorCode = errorCode;
    }

    public IstException(Throwable arg0) {
        super(arg0);
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
