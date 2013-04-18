/*
 * Copyright 2013 eBuddy BV
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

package org.elasterix.server.messages;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.sip.codec.SipHeader;

import java.util.List;
import java.util.Map;

/**
 * @author Leonard Wolters
 * @author Joost van de Wijgerd
 */
public final class SipRegister extends SipMessage {
    private final String uri;

    public SipRegister(String uri, Map<String,List<String>> headers) {
        this(uri,headers,null);
    }

    @JsonCreator
    public SipRegister(@JsonProperty("uri") String uri,
                       @JsonProperty("headers") Map<String,List<String>> headers,
                       @JsonProperty("content") byte[] content) {
        super(headers, content);
        this.uri = uri;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public String getUser() {
        return getHeader(SipHeader.TO.getName());
    }
}