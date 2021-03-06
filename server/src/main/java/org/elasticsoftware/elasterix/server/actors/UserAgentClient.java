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

package org.elasticsoftware.elasterix.server.actors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasterix.server.messages.SipRequestMessage;
import org.elasticsoftware.elasterix.server.sip.SipMessageHelper;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipVersion;

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
        if (log.isDebugEnabled()) {
            log.debug(String.format("onReceive. Message[%s]", message));
        }

        State state = getState(null).getAsObject(State.class);
        ActorRef sipService = getSystem().serviceActorFor("sipService");
        if (message instanceof SipRequestMessage) {
            SipRequestMessage m = (SipRequestMessage) message;
            switch (m.getSipMethod()) {
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

    protected void invite(ActorRef sipService, SipRequestMessage message, State state) {
    	// update state
        state.setDeviceState(DeviceState.RINGING);

        // send SIP Invite to UAC
        sipService.tell(SipMessageHelper.createInvite(message.getSipUser(SipHeader.FROM), 
        		message.getSipUser(SipHeader.TO), SipVersion.SIP_2_0, 
        		String.format("%s:%d", state.getIPAddress(), state.getPort())), getSelf());
    }

    /**
     * State belonging to User Agent Client
     */
    public static final class State {
        private final String username;
        private final String ipAddress;
        private final int port;
        private long deviceState = DeviceState.FREE.id;

        @JsonCreator
        public State(@JsonProperty("username") String username,
                     @JsonProperty("ipAddress") String ipAddress,
                     @JsonProperty("port") int port) {
            this.username = username;
            this.ipAddress = ipAddress;
            this.port = port;
        }

        @JsonProperty("username")
        public String getUsername() {
            return username;
        }

        @JsonProperty("ipAddress")
        public String getIPAddress() {
            return ipAddress;
        }

        @JsonProperty("port")
        public int getPort() {
            return port;
        }

        @JsonProperty("deviceState")
        protected long getDeviceStateId() {
            return deviceState;
        }

        @JsonIgnore
        public DeviceState getDeviceState() {
            return DeviceState.lookup(deviceState);
        }

        public void setDeviceState(DeviceState deviceState) {
            this.deviceState = deviceState.getId();
        }
    }

    public static enum DeviceState {
        FREE(1),
        RINGING(2),
        BUSY(3);

        private long id;

        private DeviceState(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public static DeviceState lookup(long id) {
            for (DeviceState ds : values()) {
                if (ds.getId() == id) {
                    return ds;
                }
            }
            return null;
        }
    }
}
