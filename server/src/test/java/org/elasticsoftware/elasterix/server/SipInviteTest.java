/*
 * Copyright 2013 Joost van de Wijgerd
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

package org.elasticsoftware.elasterix.server;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipRequestImpl;
import org.elasticsoftware.sip.codec.SipVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * <p/>
 * send a sip register message to the Sip Server
 * see http://tools.ietf.org/html/rfc3261#section-10.2
 *
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
@Test(enabled = true)
public class SipInviteTest extends AbstractSipTest {
    private static final Logger log = Logger.getLogger(SipInviteTest.class);
    private int count = 0;
    
    protected SipRequest createSipRequest(String callerId, String calleeId) {
        // INVITE URL contains the 'host' / domain as defined in the UAC 
        // CONTACT contains the 'actual' IP address of the UAC
        // FROM contains the 'host' / domain as defined in the UAC
        // TO contains the 'host' / domain as defined in the UAC
    	SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:" + calleeId + "@" + domain);
        req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
        req.addHeader(SipHeader.CONTACT, "<sip:" + callerId + "@127.0.0.1:8989>");
        req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<" + callerId + "@" + domain + ">");
        req.addHeader(SipHeader.TO, "<sip:" + calleeId + "@" + domain + ">");
        return req;
    }

    @Test(enabled = true)
    public void testInviteNonExistingCaller() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);
        
        // register actors

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
    }

    @Test(enabled = true)
    public void testInviteCallerNotAuthenticated() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);
        
        // register actors
        addUser(callerId);

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
    }

    @Test(enabled = true)
    public void testInviteNonExistingCallee() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);

        // register actors
        addUser(callerId, createSipUser(callerId, "127.0.0.1", 8989));

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
    }

    @Test(enabled = true)
    public void testInviteCalleeWithoutRegisteredUAC() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);

        // register actors
        addUser(callerId, createSipUser(callerId, "127.0.0.1", 8989));
        addUser(calleeId);

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("SIP/2.0 410 Gone"));
    }

    @Test(enabled = true)
    public void testInviteCalleeWithRegisteredUACNotAvailable() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);

        // register caller / callee
        addUser(callerId, createSipUser(callerId, "127.0.0.1", 8989));
        addUser(calleeId, createSipUser(calleeId, "127.0.0.1", 9090));

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        if (message.startsWith("SIP/2.0 100 Trying")) {
            message = sipServer.getMessage();
            Assert.assertNotNull(message);
            Assert.assertTrue(message.startsWith("SIP/2.0 410 Gone"));
        } else if (message.startsWith("SIP/2.0 410 Gone")) {
            message = sipServer.getMessage();
            Assert.assertNotNull(message);
            Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));
        } else {
            Assert.fail(message);
        }
    }

    @Test(enabled = true)
    public void testInviteCalleeWithRegisteredUACAvailable() throws Exception {
        String callerId = "lwolters" + count;
        String calleeId = "jwijgerd" + count++;
        SipRequest req = createSipRequest(callerId, calleeId);

        // register caller / callee
        addUser(callerId, createSipUser(callerId, "127.0.0.1", 8989));
        addUser(calleeId, createSipUser(calleeId, "127.0.0.1", 9090));
        addUserAgentClient(createSipUser(calleeId, "127.0.0.1", 9090));

        // send message and wait
        sipServer.sendMessage(req);
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));

        message = localSipServer.getMessage();
        Assert.assertNotNull(message);
        Assert.assertTrue(message.startsWith("INVITE sip:" + calleeId + "@127.0.0.1:9090"));
    }

    @Test(enabled = true)
    public void testInviteWithRegisterMessage() throws Exception {
        // first let joost register himself ..
        SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
        req.addHeader(SipHeader.CALL_ID, "joost_uac");
        req.addHeader(SipHeader.CONTACT, "<sip:jwijgerd@127.0.0.1:8990;transport=UDP;rinstance=6f8dc969b62d1466>");
        req.addHeader(SipHeader.EXPIRES, "120");
        req.addHeader(SipHeader.FROM, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>;tag=6d473a67");
        req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
        req.addHeader(SipHeader.VIA, "yyy");
        sipServer.sendMessage(req);
        // sleep sometime in order for message to be sent back.
        Thread.sleep(SLEEP);
        sipServer.getMessage(); // call this method in order to remove message..

        // then let leonard invite joost
        req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
        req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
        req.addHeader(SipHeader.CONTACT, "<sip:lwolters@127.0.0.1:8989;transport=UDP;rinstance=6f8dc969b62d1466>");
        req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
        req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
        sipServer.sendMessage(req);
        // sleep sometime in order for message to be sent back.
        Thread.sleep(SLEEP);
        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
//		Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));
        Thread.sleep(SLEEP);
        message = sipServer.getMessage();
//		Assert.assertNotNull(message);
    }
}