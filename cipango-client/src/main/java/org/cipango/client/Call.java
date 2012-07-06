package org.cipango.client;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;

/**
 * A SIP Call abstraction.
 */
public class Call extends AbstractDialog
{

	@Override
	public SipServletRequest createInitialRequest(URI local, URI remote)
	{
		if (_session != null)
			throw new IllegalStateException("Session already created");
		SipApplicationSession appSession = getFactory().createApplicationSession();
		SipServletRequest request = getFactory().createRequest(appSession, SipMethods.INVITE, local, remote);
		_session = request.getSession();
		return request;
	}
	
	public SipServletRequest createAck()
	{
		for (SipServletResponse response : getSessionHandler().getResponses())
		{
			if (response.getStatus() >= 200 && response.getStatus() < 300 && !response.isCommitted())
			{
				return response.createAck();
			}
		}
		return null;
	}
	
	public SipServletRequest createBye()
	{
		return createRequest(SipMethods.BYE);
	}
	
	public void cancel()
	{
		if (_session == null)
			return;

		// TODO
	}
}
