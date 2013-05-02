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

package org.elasterix.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.test.TestActorSystem;
import org.elasterix.server.actors.User;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipMethod;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipVersion;
import org.elasterix.sip.codec.impl.SipRequestImpl;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class ElasterixSipServerTest {
	private static final Logger log = Logger.getLogger(ElasterixSipServerTest.class);
	protected ActorSystem actorSystem;
	protected SipClient sipClient;
	protected Md5PasswordEncoder md5Encoder = new Md5PasswordEncoder();
	
	protected List<ActorRef> users = new ArrayList<ActorRef>();
	protected List<ActorRef> uacs = new ArrayList<ActorRef>();

	@BeforeSuite
	public void init() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasterix").setLevel(Level.DEBUG);

		actorSystem = TestActorSystem.create(new ElasterixServer());
		sipClient = new SipClient();

		// create a couple of users
		ActorRef ref = actorSystem.actorOf("user/lwolters", User.class, 
				new User.State("leonard@elasticsoftware.org", "lwolters", "test"));
		users.add(ref);
		ref = actorSystem.actorOf("user/jwijgerd", User.class, 
				new User.State("joost@elasticsoftware.org", "jwijgerd", "test"));
		users.add(ref);
		Thread.sleep(300);
	}
	
	@AfterSuite
	protected void destroy() throws Exception {
		for(ActorRef ref : users) {
			actorSystem.stop(ref);
		}
		for(ActorRef ref : uacs) {
			actorSystem.stop(ref);
		}
		if(sipClient != null) {
			sipClient.close();
		}
	}
	
	protected SipRequest createSipRequest() {
		//sipClient.sendMessage("sip-register.txt");

		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.MAX_FORWARDS, "70");
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.CALL_ID, "a84b4c76e66710");
		req.addHeader(SipHeader.CSEQ, "314159 REGISTER");
		req.addHeader(SipHeader.FROM, "Leonard Wolters <sip:leonard@localhost.com>");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:lwolters@localhost:8888>");
		return req;
	}
	
	@Test(enabled = true)
	public void testRegisterNoTo() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		log.info(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
	
	@Test(enabled = false)
	// TODO enable when bug with onUndelivered is fixed
	public void testRegisterNonExistingUser() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:XXXXXX@localhost:8888>");
		req.addHeader(SipHeader.CSEQ, "1 REGISTER");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		log.info(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}

	@Test(enabled = true)
	public void testRegisterUserUnauthorized() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:lwolters@localhost:8888>");
		req.addHeader(SipHeader.CSEQ, "1 REGISTER");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
		Assert.assertTrue(message.indexOf("WWW-Authenticate: Digest algorithm=MD5, realm=") != -1);
	}
	
	@Test(enabled = true)
	public void testRegisterUserUnauthorizedWrongHash() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:lwolters@localhost:8888>");
		req.addHeader(SipHeader.CSEQ, "1 REGISTER");
		// Authorization: Digest username="124",realm="combird",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		req.addHeader(SipHeader.AUTHORIZATION, String.format("Digest username=\"%s\",realm=\"elasterix\""
				+ ",response=\"%s\"", "lwolters", "112233"));
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}
	
	@Test(enabled = true)
	public void testRegisterUserCorrectHashWrongCSeq() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:lwolters@localhost:8888>");
		req.addHeader(SipHeader.CSEQ, "1 REGISTER");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		// get nonce!
		int idx = message.indexOf("nonce=") + 7;
		long nonce = Long.parseLong(message.substring(idx, message.indexOf('\"', idx)));
		req.addHeader(SipHeader.AUTHORIZATION, String.format("Digest username=\"%s\",realm=\"elasterix\""
				+ ",response=\"%s\"", "lwolters", md5Encoder.encodePassword("test", nonce)));
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
	
	@Test(enabled = true)
	public void testRegisterUserCorrectHashNoUAC() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.TO, "\"Leonard Wolters\"<sip:lwolters@localhost:8888>");
		req.addHeader(SipHeader.CSEQ, "1 REGISTER");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		// get nonce!
		int idx = message.indexOf("nonce=") + 7;
		long nonce = Long.parseLong(message.substring(idx, message.indexOf('\"', idx)));
		req.addHeader(SipHeader.AUTHORIZATION, String.format("Digest username=\"%s\",realm=\"elasterix\""
				+ ",response=\"%s\"", "lwolters", md5Encoder.encodePassword("test", nonce)));
		req.setHeader(SipHeader.CSEQ, "2 REGISTER");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
	
	protected boolean startsWith(String input, String startsWith) {
		input = input.trim();
		startsWith = startsWith.trim();
		
		if(input.length() < startsWith.length()) {
			System.err.println(String.format("Length input to short. input[%d] != startsWith[%d]", 
					input.length(), startsWith.length()));
			return false;
		}
		for(int i = 0; i < input.length(); i++) {
			if(input.charAt(i) != startsWith.charAt(i)) {
				System.err.println(String.format("Characters differ. Index[%d]. \n%s\n======\n%s", 
						i, input.subSequence(0, i), startsWith.subSequence(0, i)));
				return false;
			}
		}
		return true;
	}
}

