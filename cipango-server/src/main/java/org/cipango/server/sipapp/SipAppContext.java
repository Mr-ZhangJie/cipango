// ========================================================================
// Copyright 2012 NEXCOM Systems
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
package org.cipango.server.sipapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.TimerListener;

import org.cipango.server.SipMessage;
import org.cipango.server.handler.AbstractSipHandler;
import org.cipango.server.servlet.SipDispatcher;
import org.cipango.server.servlet.SipServletHandler;
import org.cipango.server.servlet.SipServletHolder;
import org.cipango.server.session.SessionHandler;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class SipAppContext extends AbstractSipHandler 
{
	private static final Logger LOG = Log.getLogger(SipAppContext.class);
	
	public static final int VERSION_10 = 10;
	public static final int VERSION_11 = 11;
    
	public final static String[] EXTENSIONS = { "MESSAGE", "INFO", "SUBSCRIBE", "NOTIFY", "UPDATE", "PUBLISH", "REFER",  "100rel" };
	
	public static final String[] SUPPORTED_RFC = new String[] {
		"2976", // The SIP INFO Method
		"3261", // SIP: Session Initiation Protocol
		"3262", // Reliability of Provisional Responses
		"3265", // (SIP)-Specific Event Notification. 
		"3311", // (SIP) UPDATE Method
		"3327", // SIP) Extension Header Field for Registering Non-Adjacent Contacts (Path header)
		"3428", // SIP Extension for Instant Messaging  
		"3515", // SIP Refer Method
		"3903", // SIP Extension for Event State Publication
		"5658", // Addressing Record-Route Issues in SIP
		"6026"	// Correct Transaction Handling for 2xx Responses to Session Initiation Protocol (SIP) INVITE Requests
	};
	
	public static final String EXTERNAL_INTERFACES = "org.cipango.externalOutboundInterfaces";
	public final static String SIP_DEFAULTS_XML="org/cipango/server/sipapp/sipdefault.xml";
	
	private String _name;
	private String _defaultsDescriptor=SIP_DEFAULTS_XML;
	private final List<String> _overrideDescriptors = new ArrayList<String>();
	
  

	private int _sessionTimeout = -1;
    private int _proxyTimeout = -1;
	


	private SessionHandler _sessionHandler;
	private SipServletHandler _servletHandler;
	private int _specVersion;
	
	private WebAppContext _context;
	private ServletContext _sContext;
	
    private TimerListener[] _timerListeners = new TimerListener[0];
    private SipApplicationSessionListener[] _appSessionListeners = new SipApplicationSessionListener[0];
    private SipErrorListener[] _errorListeners = new SipErrorListener[0];
    private SipApplicationSessionAttributeListener[] _appSessionAttributeListeners = new SipApplicationSessionAttributeListener[0];
    private SipSessionListener[] _sessionListeners = new SipSessionListener[0];
    private SipSessionAttributeListener[] _sessionAttributeListeners = new SipSessionAttributeListener[0];
    private SipServletListener[] _servletListeners = new SipServletListener[0];
    

	
	public SipAppContext()
	{
		_sessionHandler = new SessionHandler();
		_servletHandler = new SipServletHandler();
	}
	
	public void setWebAppContext(WebAppContext context)
	{
		_context = context;
		_sContext = new SContext(_context.getServletContext());
		WebAppContextListener l = new WebAppContextListener();
		context.addLifeCycleListener(l);
		
		// Ensure that lifeCycleStarting is call even if context is starting.
		if (context.isStarting())
			l.lifeCycleStarting(context);
	}
	
	public ServletContext getServletContext()
	{
		return _sContext;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
		
	public SipApplicationSessionListener[] getSipApplicationSessionListeners()
	{
		return _appSessionListeners;
	}

	public TimerListener[] getTimerListeners()
	{
		return _timerListeners;
	}

	public SipErrorListener[] getSipErrorListeners()
	{
		return _errorListeners;
	}

	public SipApplicationSessionAttributeListener[] getSipApplicationSessionAttributeListeners()
	{
		return _appSessionAttributeListeners;
	}

	public SipSessionListener[] getSipSessionListeners()
	{
		return _sessionListeners;
	}

	public SipSessionAttributeListener[] getSessionAttributeListeners()
	{
		return _sessionAttributeListeners;
	}
	
	public SipServletHandler getSipServletHandler()
	{
		return _servletHandler;
	}
	
	public EventListener[] getEventListeners()
    {
        return _context.getEventListeners();
    }

    public void setEventListeners(EventListener[] eventListeners)
    {   	
        _context.setEventListeners(eventListeners);
        
        Object timerListeners = null;
        Object appSessionListeners = null;
        Object errorListeners = null;
        Object appSessionAttributeListeners = null;
        Object sessionListeners = null;
        Object sessionAttributesListeners = null;
        Object servletListeners = null;
        
        for (int i = 0; eventListeners != null && i < eventListeners.length; i++)
        {
            EventListener listener = eventListeners[i];
            if (listener instanceof TimerListener)
                timerListeners = LazyList.add(timerListeners, listener);
            if (listener instanceof SipApplicationSessionListener)
                appSessionListeners = LazyList.add(appSessionListeners, listener);
            if (listener instanceof SipErrorListener)
                errorListeners = LazyList.add(errorListeners, listener);
            if (listener instanceof SipApplicationSessionAttributeListener)
            	appSessionAttributeListeners = LazyList.add(appSessionAttributeListeners, listener);
            if (listener instanceof SipSessionListener)
            	sessionListeners = LazyList.add(sessionListeners, listener);
            if (listener instanceof SipSessionAttributeListener)
            	sessionAttributesListeners = LazyList.add(sessionAttributesListeners, listener);
            if (listener instanceof SipServletListener)
            	servletListeners = LazyList.add(servletListeners, listener);
        }
        _timerListeners = (TimerListener[]) 
            LazyList.toArray(timerListeners, TimerListener.class);
        _appSessionListeners = (SipApplicationSessionListener[]) 
            LazyList.toArray(appSessionListeners, SipApplicationSessionListener.class);
        _errorListeners = (SipErrorListener[])
            LazyList.toArray(errorListeners, SipErrorListener.class);
        _appSessionAttributeListeners = (SipApplicationSessionAttributeListener[])
        	LazyList.toArray(appSessionAttributeListeners, SipApplicationSessionAttributeListener.class);
        _sessionListeners = (SipSessionListener[])
        	LazyList.toArray(sessionListeners, SipSessionListener.class);
        _sessionAttributeListeners = (SipSessionAttributeListener[])
        	LazyList.toArray(sessionAttributesListeners, SipSessionAttributeListener.class);
        _servletListeners = (SipServletListener[])
        	LazyList.toArray(servletListeners, SipServletListener.class);
    }
	
	public void handle(SipMessage message) throws IOException, ServletException 
	{
		_sessionHandler.handle(message);
		// TODO Auto-generated method stub
	}
	
	public int getSessionTimeout()
	{
		return _sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout)
	{
		_sessionTimeout = sessionTimeout;
	}

	public int getProxyTimeout()
	{
		return _proxyTimeout;
	}

	public void setProxyTimeout(int proxyTimeout)
	{
		_proxyTimeout = proxyTimeout;
	}
	
    /**
     * The default descriptor is a sip.xml format file that is applied to the context before the standard WEB-INF/sip.xml
     * @return Returns the defaultsDescriptor.
     */
	public String getDefaultsDescriptor()
	{
		return _defaultsDescriptor;
	}

	public void setDefaultsDescriptor(String defaultsDescriptor)
	{
		_defaultsDescriptor = defaultsDescriptor;
	}

	public List<String> getOverrideDescriptors()
	{
		return _overrideDescriptors;
	}
	
	public WebAppContext getWebAppContext()
	{
		return _context;
	}
	
	public int getSpecVersion()
	{
		return _specVersion;
	}

	public void setSpecVersion(int specVersion)
	{
		_specVersion = specVersion;
	}

	public SipServletHandler getServletHandler()
	{
		return _servletHandler;
	}
	
	public boolean hasSipServlets()
    {
    	SipServletHolder[] holders = getSipServletHandler().getServlets();
    	return holders != null && holders.length != 0;
    }

	class SContext extends ServletContextProxy
	{
		
		public SContext(ServletContext servletContext)
		{
			super(_context.getServletContext());
		}

		@Override
		public RequestDispatcher getNamedDispatcher(String name)
		{
			if (_servletHandler != null)
            {
                SipServletHolder holder =  _servletHandler.getHolder(name);
                if (holder != null)
                	return new SipDispatcher(SipAppContext.this, holder);
            }
            return super.getNamedDispatcher(name);
		}

		@Override
		public String getServerInfo()
		{
			// FIXME what should be returned ?
			return "Cipango-3.0";
			//return super.getServerInfo();
		}
	}
	
	private class WebAppContextListener implements LifeCycle.Listener
	{

		@SuppressWarnings("deprecation")
		@Override
		public void lifeCycleStarting(LifeCycle event)
		{
		    _context.setAttribute(SipServlet.PRACK_SUPPORTED, Boolean.TRUE);
//		    _context.setAttribute(SipServlet.SIP_FACTORY, getSipFactory());
//		    _context.setAttribute(SipServlet.TIMER_SERVICE, getTimerService());
//		    _context.setAttribute(SipServlet.SIP_SESSIONS_UTIL, getSipSessionsUtil());
		    _context.setAttribute(SipServlet.SUPPORTED, Collections.unmodifiableList(Arrays.asList(EXTENSIONS)));
		    _context.setAttribute(SipServlet.SUPPORTED_RFCs, Collections.unmodifiableList(Arrays.asList(SUPPORTED_RFC)));
		}

		@Override
		public void lifeCycleStarted(LifeCycle event)
		{
			try
			{
	//	        if (_sipSecurityHandler!=null)
	//	        {
	//	        	_sipSecurityHandler.setHandler(_servletHandler);	            
	//	            _sipSecurityHandler.start(); // FIXME when should it be started
	//	        }
				_sessionHandler.setHandler(_servletHandler);
				      		
				_servletHandler.start();
				
				if (!_context.isAvailable())
		    	{
	//	    		if (_name == null)
	//					_name = getDefaultName();
	//				Events.fire(Events.DEPLOY_FAIL, 
	//	        			"Unable to deploy application " + getName()
	//	        			+ ": " + _context.getUnavailableException().getMessage());
		    	}
		    	else if (hasSipServlets())
		    	{
	//	    		getServer().applicationStarted(this);
		    	}
			}
			catch (Exception e) 
			{
				LOG.warn("Failed to start SipAppContext " + getName(), e);
				_context.setAvailable(false);
			}
			
		}

		@Override
		public void lifeCycleFailure(LifeCycle event, Throwable cause)
		{
			// TODO Auto-generated method stub
			
		}

		@Override

		public void lifeCycleStopping(LifeCycle event)
		{
			try
			{
				if (hasSipServlets() && _context.isAvailable())
	//				getServer().applicationStopped(this);
				
						
	//			if (_sipSecurityHandler != null)
	//				_sipSecurityHandler.stop();
					
				_servletHandler.stop();
			}
			catch (Exception e) 
			{
				LOG.warn("Failed to stop SipAppContext " + getName(), e);
			}
		}

		@Override
		public void lifeCycleStopped(LifeCycle event)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
}
