// ========================================================================
// Copyright 2008-2012 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.server.session;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletResponse;

import org.cipango.server.SipHandler;
import org.cipango.server.SipMessage;
import org.cipango.server.SipRequest;
import org.cipango.server.handler.AbstractSipHandler;

public class SessionHandler extends AbstractSipHandler
{
	private SessionManager _sessionManager;
	private SipHandler _handler;
	
	public SessionHandler()
	{
		_sessionManager = new SessionManager();
	}
	
	public void setHandler(SipHandler handler)
	{
		_handler = handler;
	}
	
	@Override
	protected void doStart() throws Exception
	{
		_sessionManager.start();
		_handler.start();
		
		super.doStart();
	}
	
	public void handle(SipMessage message) throws IOException, ServletException 
	{
		if (message.isRequest())
		{
			SipRequest request = (SipRequest) message;
						
			Session session = null;
			
			if (request.isInitial())
			{
				ApplicationSession appSession = _sessionManager.createApplicationSession();
				session = appSession.createSession(request);
			}
			else
			{
				String appId = request.getParameter("appid");
				if (appId == null)
				{
					String tag = request.getToTag();
					int i = tag.indexOf('-');
					if (i != -1)
						appId = tag.substring(0,  i);
				}
				if (appId == null)
				{
					notFound(request, "No Application Session Identifier");
					return;
				}
				
				ApplicationSession	appSession = _sessionManager.getApplicationSession(appId);
				
				if (appSession == null)
				{
					notFound(request, "No Application Session");
					return;
				}
			}
			
			if (session == null)
			{
				notFound(request, "No SIP Session");
				return;
			}
			session.accessed();
			request.setSession(session);
			
			_handler.handle(request);
		}
	}
	
	protected void notFound(SipRequest request, String reason)
	{
		if (!request.isAck())
			request.createResponse(SipServletResponse.SC_CALL_LEG_DONE, reason);
	}
	protected String getApplicationId(String s)
	{
		return s.substring(s.lastIndexOf('-'));
	}
	
}
