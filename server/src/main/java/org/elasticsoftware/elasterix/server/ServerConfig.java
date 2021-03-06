package org.elasticsoftware.elasterix.server;

import org.springframework.beans.factory.annotation.Value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Static Server Configuration
 *
 * @author Leonard Wolters
 */
public class ServerConfig {
    /**
     * Example:  Mon, 13 May 2013 18:16:52 GMT
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss Z");

    @Value("${sip.port}")
    private static int port = 5060;

    public static String getRealm() {
        return "elastic-software";
    }

    public static String getServerName() {
        return "server1";
    }

    public static String getSupported() {
        return "replaces, timer";
    }

    public static String getAllow() {
        return "INVITE, ACK, CANCEL, OPTIONS, BYE, "
                + "REFER, SUBSCRIBE, NOTIFY, INFO, PUBLISH";
    }

    public static String getDateNow() {
        return DATE_FORMAT.format(new Date());
    }

    public static String getUsername() {
        return "Unknown";
    }

    public static String getNewCallId() {
        // 1a79c789402742c5725be8055aa36cb6@217.195.124.187:5060
        return String.format("%s@%s", UUID.randomUUID().toString(), getServerName());
    }

    public static String getIPAddress() {
        //return "127.0.0.1";
        return "sip.outerteams.com";
    }

    public static int getSipPort() {
        return port;
    }

    public static int getMaxForwards() {
        return 70;
    }

    public static String getProtocol() {
        return "UDP";
    }

    public static String getDigestAlgorithm() {
        //return "SHA-256";
        return "MD5";
    }
    
    public static String getUserAgent() {
    	return "Elasterix 0.1";
    }
}
