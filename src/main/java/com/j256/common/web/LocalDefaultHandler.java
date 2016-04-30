package com.j256.common.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;

/**
 * This will take any uncommitted requests and set the status code to SC_OK (200) while ensuring that no content is
 * written out to the response.
 */
public class LocalDefaultHandler extends DefaultHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (baseRequest.isHandled() || response.isCommitted()) {
			return;
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		writer.print("No handler for ");
		writer.print(request.getRequestURL());
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString)) {
			writer.print('?');
			writer.print(queryString);
		}
		writer.println("");
		writer.close();
	}
}
