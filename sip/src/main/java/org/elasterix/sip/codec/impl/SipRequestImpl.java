package org.elasterix.sip.codec.impl;

import org.elasterix.sip.codec.SipMethod;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;
import org.jboss.netty.util.internal.StringUtil;

/**
 * The default {@link SipRequest} implementation.
 * 
 * @author Leonard Wolters
 */
public class SipRequestImpl extends SipMessageImpl implements SipRequest {
    private SipMethod method;
    private String uri;

    /**
     * Creates a new instance.
     *
     * @param version     the SIP version of the request
     * @param method      the SIP method of the request
     * @param uri         the URI or path of the request
     */
    public SipRequestImpl(SipVersion version, SipMethod method, String uri) {
        super(version);
        setMethod(method);
        setUri(uri);
    }
    
    /**
     * Creates a new instance. <br>
     * Often this constructor is used to indicate syntactically incorrect
     * sip requests
     * 
     * @param responseStatus
     */
    public SipRequestImpl(SipResponseStatus responseStatus) {
    	super(responseStatus);
    }

    @Override
    public SipMethod getMethod() {
        return method;
    }

    private void setMethod(SipMethod method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    private void setUri(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%s %s %s", getMethod().name(), getUri(), getProtocolVersion().name()));
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
}
