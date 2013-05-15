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

package org.elasticsoftware.server;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipVersion;
import org.elasticsoftware.sip.codec.impl.SipRequestImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * 
 * send a sip register message to the Sip Server
 * see http://tools.ietf.org/html/rfc3261#section-10.2
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
@Test(enabled=true)
public class SipInviteTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipInviteTest.class);
	
	@Test(enabled = true)
	public void testInviteNonExistingCaller() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<xxx@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
	}
	
	@Test(enabled = true)
	public void testInviteNonExistingCallee() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:xxx@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
	}
	
	@Test(enabled = true)
	public void testInviteNoRegistedUAC() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 410 Gone"));
	}
	
//	@Test(enabled = true)
//	public void testInviteOK() throws Exception {
//		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
//		req.addHeader(SipHeader.CALL_ID, "xxx");
//		req.addHeader(SipHeader.CSEQ, "3 INVITE"); 
//		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
//		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
//		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
//		sipClient.sendMessage(req);
//		// sleep sometime in order for message to be sent back.
//		Thread.sleep(300);
//		String message = sipClient.getMessage();
//		Assert.assertNotNull(message);
//		Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));
//	}
}