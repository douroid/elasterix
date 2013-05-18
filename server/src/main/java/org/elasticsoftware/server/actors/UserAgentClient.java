/*
 * Copyright 2013 Leonard Wolters, Joost van de Wijgerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.server.actors;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.ServerConfig;
import org.elasticsoftware.server.messages.SipRequestMessage;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipUser;
import org.elasticsoftware.sip.codec.SipVersion;
import org.springframework.util.StringUtils;

/**
 * User Agent Client (UAC)<br>
 * Considered a device
 * 
 * @author Leonard Wolters
 */
public final class UserAgentClient extends UntypedActor {
	private static final Logger log = Logger.getLogger(UserAgentClient.class);

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(String.format("onReceive. Message[%s]", message));
		}

		State state = getState(null).getAsObject(State.class);
		ActorRef sipService = getSystem().serviceActorFor("sipService");
		if(message instanceof SipRequestMessage) {
			SipRequestMessage m = (SipRequestMessage) message;
			switch(m.getSipMethod()) {
			case REGISTER:
				register(sipService, m, state);
				return;
			case INVITE:
				invite(sipService, m, state);
				return;
			default:
				log.warn(String.format("onReceive. Unsupported message[%s]", 
						message.getClass().getSimpleName()));
				unhandled(message);
			}
		}
	}

	protected void register(ActorRef sipService, SipRequestMessage message, State state) {

		// set expiration. (seconds)
		Long expires = message.getHeaderAsLong(SipHeader.EXPIRES);
		if(expires != null) {
			state.setExpires(expires);
		}

		// schedule timeout...
		if(expires != null) {
			getSystem().getScheduler().scheduleOnce(getSelf(), 
				message, getSelf(), expires, TimeUnit.SECONDS);
		}
		
		// check for contact header
		String contact = message.getHeader(SipHeader.CONTACT);
		if(!StringUtils.hasLength(contact)) {
			log.warn(String.format("doRegister. No contact header found"));
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.BAD_REQUEST,
					"No CONTACT header found"), getSelf());
		}
		
		message.addHeader(SipHeader.DATE, new Date(state.getExpiration()).toString());
		sipService.tell(message.toSipResponseMessage(SipResponseStatus.OK, null), getSelf());
	}
	
	protected void invite(ActorRef sipService, SipRequestMessage message, State state) {
		// ok, construct a new request message (invite) and sent it to 
		// the sip client of user...
		
		SipVersion version = SipVersion.SIP_2_0;
		
		// uri is complete CONTACT header without trailing '<' and ending '>'
		String uri = message.getHeader(SipHeader.CONTACT);
		if(uri.startsWith("<")) uri = uri.substring(1);
		if(uri.endsWith(">")) uri = uri.substring(0, uri.length() - 1);
		
		// create sip invite request message
		SipRequestMessage sipInvite = new SipRequestMessage(SipMethod.INVITE.name(), uri, 
				version.toString(), null, null, false);
		sipInvite.addHeader(SipHeader.CONTACT, String.format("<sip:%s@%s:%d;transport=%s;rinstance=6f8dc969b62d1466>",
				ServerConfig.getUsername(), ServerConfig.getIPAddress(), ServerConfig.getSipPort(), 
				ServerConfig.getProtocol()));
		sipInvite.addHeader(SipHeader.CSEQ, "");
		sipInvite.addHeader(SipHeader.FROM, String.format("\"%s\"<sip:%s@%s:%d>;tag=6d473a67",
				ServerConfig.getUsername(), ServerConfig.getUsername(), ServerConfig.getIPAddress(), 
				ServerConfig.getSipPort()));
		SipUser user = message.getUser(SipHeader.TO);
		sipInvite.addHeader(SipHeader.TO, String.format("\"%s\"<sip:%s@%s:%d>;tag=6d473a67", 
				user.getDisplayName(), user.getUsername(), state.getIPAddress(), state.getPort()));
		
		// send SIP Request to UAC
		sipService.tell(sipInvite, getSelf());
	}
	
	/**
	 * State belonging to User Agent Client
	 */
	public static final class State {
		private final String uid;
		private long expires = 0;
		private long expiration = 0;
		private String ipAddress;
		private int port;

		@JsonCreator
		public State(@JsonProperty("uid") String uid, 
				@JsonProperty("ipAddress") String ipAddress, 
				@JsonProperty("port") int port) {
			this.uid = uid;
			this.ipAddress = ipAddress;
			this.port = port;
		}

		@JsonProperty("uid")
		public String getUid() {
			return uid;
		}

		@JsonProperty("expires")
		public long getExpires() {
			return expires;
		}
		
		@JsonProperty("expiration")
		public long getExpiration() {
			return expiration;
		}
		
		@JsonProperty("ipAddress")
		public String getIPAddress() {
			return ipAddress;
		}
		
		@JsonProperty("port")
		public int getPort() {
			return port;
		}
		
		protected void setExpires(long expires) {
			this.expires = expires;
			this.expiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires);
		}
	}
}
