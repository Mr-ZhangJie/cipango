package org.cipango.sip;

import java.util.HashMap;
import java.util.Map;

import org.cipango.util.StringUtil;
import org.eclipse.jetty.util.StringMap;

public enum SipHeader 
{
	VIA("Via", Type.VIA, true),
	MAX_FORWARDS("Max-Forwards"),
	ROUTE("Route", Type.ADDRESS, true),
	RECORD_ROUTE("Record-Route", Type.ADDRESS, true),
	FROM("From", Type.ADDRESS, true),
	TO("To", Type.ADDRESS, true),
	CALL_ID("Call-ID", Type.STRING, true),
	CSEQ("CSeq", Type.STRING, true),
	CONTACT("Contact"),
	
	ACCEPT("Accept"), 
	ACCEPT_CONTACT("Accept-Contact"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_LANGUAGE("Accept-Language"),
	ACCEPT_RESOURCE_PRIORITY("Accept-Resource-Priority"),
	ALERT_INFO("Alert-Info"),
	ALLOW("Allow"),
	ALLOW_EVENTS("Allow-Events"),	
	AUTHENTICATION_INFO("Authentication-Info"),
	AUTHORIZATION("Authorization"), 
	
	CALL_INFO("Call-Info"),
	
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LANGUAGE("Content-Language"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_TYPE("Content-Type"),
	
	DATE("Date"),
	ERROR_INFO("Error-Info"),
	EVENT("Event"),
	EXPIRES("Expires"),
	
	HISTORY_INFO("History-Info"),
	IDENTITY("Identity"),
	IDENTITY_INFO("Identity-Info"),
	IN_REPLY_TO("In-Reply-To"),
	JOIN("Join"), 
	
	MIME_VERSION("MIME-Version"),
	MIN_EXPIRES("Min-Expires"),
	MIN_SE("Min-SE"),
	ORGANIZATION("Organization"),
	P_ACCESS_NETWORK_INFO("P-Access-Network-Info"),
	P_ASSERTED_IDENTITY("P-Asserted-Identity"),
	P_ASSOCIATED_URI("P-Associated-URI"),
	P_CALLED_PARTY_ID("P-Called-Party-ID"),
	P_CHARGING_FUNCTION_ADDRESSES("P-Charging-Function-Addresses"),
	P_CHARGING_VECTOR("P-Charging-Vector"),
	P_MEDIA_AUTHORIZATION("P-Media-Authorization"), 
	P_PREFERRED_IDENTITY("P-Preferred-Identity"),
	P_USER_DATABASE("P-User-Database"),
	P_VISITED_NETWORK_ID("P-Visited-Network-ID"),
	PATH("Path"), 
	PRIORITY("Priority"),
	PRIVACY("Privacy"), 
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	PROXY_AUTHORIZATION("Proxy-Authorization"),
	PROXY_REQUIRE("Proxy-Require"),
	RACK("RAck", Type.STRING, true),
	REASON("Reason"),
	
	REFER_SUB("Refer-Sub"),
	REFER_TO("Refer-To"),
	REFERRED_BY("Referred-By"),
	REJECT_CONTACT("Reject-Contact"),
	REPLACES("Replaces"),
	REPLY_TO("Reply-To"),
	REQUEST_DISPOSITION("Request-Disposition"),
	REQUIRE("Require"), 
	RESOURCE_PRIORITY("Resource-Priority"),
	RETRY_AFTER("Retry-After"), 
	
	RSEQ("RSeq", Type.STRING, true),
	SECURITY_CLIENT("Secury-Client"), 
	SECURITY_SERVER("Security-Server"),
	SECURITY_VERIFY("Security-Verify"),
	SERVER("Server"),
	SERVICE_ROUTE("Service-Route"), 
	SESSION_EXPIRES("Session-Expires"),
	SIP_ETAG("SIP-ETag"),
	SIP_IF_MATCH("SIP-If-Match"),
	SUBJECT("Subject"),
	SUBSCRIPTION_STATE("Subscription-State"),
	SUPPORTED("Supported"),
	TARGET_DIALOG("Target-Dialog"),
	TIMESTAMP("Timestamp"),
	
	UNSUPPORTED("Unsupported"),
	USER_AGENT("User-Agent"),
	
	WARNING("Warning"),
	WWW_AUTHENTICATE("WWW-Authenticate");
	
	public static final StringMap<SipHeader> CACHE = new StringMap<SipHeader>(true);
	
	static 
	{
		for (SipHeader header : SipHeader.values())
			CACHE.put(header.toString(), header);
	}
	
	private Type _type;
	private String _string;
	private byte[] _bytesColonSpace;
	
	private boolean _system;
	
	SipHeader(String s, Type type, boolean system)
	{
		_string = s;
		_type = type;
		_system = system;
		_bytesColonSpace = StringUtil.getBytes(s+": ", StringUtil.__UTF8);
	}
	
	SipHeader(String s)
	{
		this(s, Type.STRING, false);
	}
	
	public boolean isSystem()
	{
		return _system;
	}
	
	public Type getType()
	{
		return _type;
	}
	
	public byte[] getBytesColonSpace()
	{
		return _bytesColonSpace;
	}
	
	public String asString()
	{
		return _string;
	}
	
	@Override
	public String toString()
	{
		return _string;
	}
	
	public enum Type { STRING, PARAMETERABLE, ADDRESS, VIA }
	
	private final static SipHeader[] __hashed = new SipHeader[4096];
	private final static int __maxHashed;
	
	static
	{
		int max = 0;
		Map<Integer, SipHeader> hashes = new HashMap<Integer, SipHeader>();
		
		for (SipHeader header : SipHeader.values())
		{
			String s = header.asString();
			max = Math.max(max, s.length());
			int h = 0;
			for (char c : s.toCharArray())
				h = 31*h + ((c >= 'a') ? (c - 'a' + 'A') : c);
			int hash = h % __hashed.length;
			if (hash < 0)
				hash = -hash;
			if (hashes.containsKey(hash))
			{
				System.err.println("duplicate hash " + header + " " + hashes.get(hash));
				System.exit(1);
			}
			hashes.put(hash, header);
			__hashed[hash] = header;
		}
		__maxHashed = max;
	}
	
	public static SipHeader lookAheadGet(byte[] bytes, int position, int limit)
	{
		int h=0;
        byte b=0;
        limit=Math.min(position+__maxHashed,limit);
        for (int i=position;i<limit;i++)
        {
            b=bytes[i];
            if (b==':'||b==' ')
                break;
            h= 31*h+ ((b>='a')?(b-'a'+'A'):b);
        }
        if (b!=':'&&b!=' ')
            return null;

        int hash=h%__hashed.length;
        if (hash<0)hash=-hash;
        
        SipHeader header=__hashed[hash];
        
        if (header!=null)
        {
            String s=header.asString();
            for (int i=s.length();i-->0;)
            {
                b=bytes[position+i];
                char c=s.charAt(i);
                if (c!=b && Character.toUpperCase(c)!=(b>='a'?(b-'a'+'A'):b))
                    return null;
            }
        }
        
        return header;
	}
}
