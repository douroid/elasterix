package org.elasticsoftware.sip;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMessage;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;
import org.elasticsoftware.sip.codec.SipUser;
import org.jboss.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * Standard implementation of <code>SipMessageSender</code>
 * Send incoming <code>SipMessage</code>'s to its belonging recipient.
 * <br>
 * <br>
 * This implementation takes automatically care of handling sockets and
 * connections, and reestablish connections to client's when required.<br>
 *
 * @author Leonard Wolters
 */
@Component
public class SipMessageSenderImpl implements SipMessageSender {
    private static final Logger log = Logger.getLogger(SipMessageSenderImpl.class);
    private static final Logger sipLog = Logger.getLogger("sip");

    /**
     * Channel Factory is required for obtaining channels used for sending messages
     */
    private SipChannelFactory sipChannelFactory;

    @PostConstruct
    private void init() {
    }

	@Override
	public void sendRequest(SipRequest request, SipMessageCallback callback) {
		if(log.isDebugEnabled()) {
			log.debug(String.format("Sending Request\n%s", request));
		}
		
		// content length
		request.setHeader(SipHeader.CONTENT_LENGTH, request.getContentLength(0));

		Channel c = sipChannelFactory.getChannel(new SipUser(request.getUri()));
		if(c == null) {
			log.error(String.format("sendRequest. No channel set/found."));
			return;
		}
		if(c.isConnected() && c.isOpen()) {
			logMessage("SENDING REQUEST", request);
			c.write(request);
		} else {
			log.warn(String.format("sendRequest. Channel not connected or closed"));
		}
	}

	@Override
	public void sendResponse(SipResponse response, SipMessageCallback callback) {
		if(log.isDebugEnabled()) {
			log.debug(String.format("Sending Response\n%s", response));
		}
		
		// content length
		response.setHeader(SipHeader.CONTENT_LENGTH, response.getContentLength(0));
		
		Channel c = sipChannelFactory.getChannel(response.getSipUser(SipHeader.CONTACT));
		if(c == null) {
			log.error(String.format("sendResponse. No channel set/found."));
			return;
		}
		if(c.isConnected() && c.isOpen()) {
			logMessage("SENDING RESPONSE", response);
			c.write(response);
		} else {
			log.warn(String.format("sendResponse. Channel not connected or closed"));
		}
	}

    @Required
    public void setSipChannelFactory(SipChannelFactory sipChannelFactory) {
        this.sipChannelFactory = sipChannelFactory;
    }

    private void logMessage(String prefix, SipMessage message) {
    	if(sipLog.isDebugEnabled()) {
    		sipLog.debug(String.format("%s\n%s\n", prefix, message));
    	}
		if(log.isDebugEnabled()) {
    		log.debug(String.format("%s\n%s\n", prefix, message));
		}
    }
}
