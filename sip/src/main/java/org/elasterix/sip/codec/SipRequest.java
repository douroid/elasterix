package org.elasterix.sip.codec;

/**
 * An SIP request.
 *
 * @see SipResponse
 * @author Leonard Wolters
 */
public interface SipRequest extends SipMessage {

    /**
     * Returns the method of this request.
     */
    SipMethod getMethod();

    /**
     * Returns the URI (or path) of this request.
     */
    String getUri();
}