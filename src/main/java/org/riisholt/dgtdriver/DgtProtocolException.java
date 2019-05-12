package org.riisholt.dgtdriver;

public class DgtProtocolException extends Exception {
    public DgtProtocolException(String msg) { super(msg); }
    public DgtProtocolException(String msg, Throwable e) { super(msg, e); }
}
