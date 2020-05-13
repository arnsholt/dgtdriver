package org.riisholt.dgtdriver;

/**
 * TODO: Document this class.
 */
// XXX: Consider introducing subclasses of this class for more fine-grained exception handling?
public class DgtProtocolException extends Exception {
    public DgtProtocolException(String msg) { super(msg); }
    public DgtProtocolException(String msg, Throwable e) { super(msg, e); }
}
