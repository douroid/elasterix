package org.elasterix.sip.codec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.springframework.util.StringUtils;

/**
 * Decodes {@link ChannelBuffer}s into {@link SipRequest}s
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line (e.g. {@code "GET / HTTP/1.0"})
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderLineLength}</td>
 * <td>The maximum length of a single header.  If the length of a single 
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * </table>
 */
public class SipRequestDecoder extends SipMessageDecoder {
	private static final Logger log = Logger.getLogger(SipRequestDecoder.class);
	private static final Pattern URI_PATTERN = 
			Pattern.compile("^sip:[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$", 
					Pattern.CASE_INSENSITIVE);  

	public SipRequestDecoder() {
		super(4096, 8192, 4096);
	}
	public SipRequestDecoder(int maxInitialLineLength, int maxHeaderSize, 
			int maxHeaderLineLength) {
		super(maxInitialLineLength, maxHeaderSize, maxHeaderLineLength);
	}

	@Override
	protected boolean isDecodingRequest() {
		return true;
	}

	@Override
	protected SipMessage createMessage(String[] initialLine) throws Exception {
    	if(log.isDebugEnabled()) log.debug(String.format("createMessage. Creating SIP Message[%s]", 
    			StringUtils.arrayToCommaDelimitedString(initialLine)));
    	
    	SipVersion version = SipVersion.lookup(initialLine[2], false);
    	if(version == null) {
    		log.warn(String.format("createMessage. Invalid Sip Version[%s]", initialLine[2]));
    		return new SipRequestImpl(SipResponseStatus.VERSION_NOT_SUPPORTED);
    	}
    	SipMethod method = SipMethod.lookup(initialLine[0], false);
    	if(method == null) {
    		log.warn(String.format("createMessage. Invalid Sip Method[%s]", initialLine[0]));
    		return new SipRequestImpl(SipResponseStatus.METHOD_NOT_ALLOWED);
    	}
    	String uri = initialLine[1];
    	Matcher matcher = URI_PATTERN.matcher(uri);  
		if(!matcher.matches()) {
			log.warn(String.format("createMessage. Invalid URI[%s]", uri));
			return new SipRequestImpl(SipResponseStatus.BAD_REQUEST);
		}
		return new SipRequestImpl(version, method, uri);
	}
}
