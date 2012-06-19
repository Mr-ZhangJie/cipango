package org.cipango.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

public class DefaultServlet extends SipServlet
{
	@Override
	public void init()
	{
		System.out.println("servlet initialized");
	}
	
	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{
		try
		{
			super.doRequest(request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void doInvite(SipServletRequest request) throws ServletException, IOException
	{
		request.createResponse(200).send();
	}
	
	@Override
	protected void doOptions(SipServletRequest request) throws ServletException, IOException
	{
		SipServletResponse response = request.createResponse(200);
		response.addHeader("Server", getServletConfig().getServletName());
		response.send();
	}
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException, IOException
	{
		request.createResponse(200).send();
	}
}