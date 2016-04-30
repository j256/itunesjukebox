package com.j256.common.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;

/**
 * Web server head.
 * 
 * @author graywatson
 */
@JmxResource(domainName = "j256.common")
public class WebServer implements InitializingBean, DisposableBean {

	private static Logger logger = LoggerFactory.getLogger(WebServer.class);

	private static final int WEB_SERVER_MIN_THREADS = 10;
	private static final int WEB_SERVER_MAX_THREADS = 100;

	private Server server;
	private SelectChannelConnector connector;
	private SslSelectChannelConnector sslConnector;
	@JmxAttributeField(description = "Port the server is running on")
	private int serverPort;
	private Handler handler;

	@Override
	public void afterPropertiesSet() throws Exception {

		server = new Server();

		// create the NIO connector
		connector = new SelectChannelConnector();
		connector.setPort(serverPort);
		configConnector(connector);
		server.addConnector(connector);

		// create our NIO SSL connector with the ssl stuff stored in the keystore
		// SslContextFactory sslContextFactory = new SslContextFactory("/keystore");
		// sslContextFactory.setKeyStorePassword("yyy");
		// sslConnector = new SslSelectChannelConnector(sslContextFactory);
		// sslConnector.setPort(serverSslPort);
		// configConnector(sslConnector);
		// server.addConnector(sslConnector);

		server.setHandler(new DefaultErrorHandler(handler));
		server.start();
		logger.info("started on port " + serverPort);
	}

	@Override
	public void destroy() throws Exception {
		server.stop();
		connector.close();
		if (sslConnector != null) {
			sslConnector.close();
		}
		logger.info("web server stoppped");
	}

	@Required
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	@Required
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@JmxAttributeMethod(description = "Total number of connections made")
	public int getNumConnections() {
		return getNumConnections(connector) + getNumConnections(sslConnector);
	}

	@JmxAttributeMethod(description = "Number of currently open connections")
	public int getConnectionsOpen() {
		return getConnectionsOpen(connector) + getConnectionsOpen(sslConnector);
	}

	@JmxAttributeMethod(description = "Maximum number of open connections at one time")
	public int getConnectionsOpenMax() {
		return getConnectionsOpenMax(connector) + getConnectionsOpenMax(sslConnector);
	}

	private int getNumConnections(SelectChannelConnector connector) {
		return (connector == null ? 0 : connector.getConnections());
	}

	private int getConnectionsOpen(SelectChannelConnector connector) {
		return (connector == null ? 0 : connector.getConnectionsOpen());
	}

	private int getConnectionsOpenMax(SelectChannelConnector connector) {
		return (connector == null ? 0 : connector.getConnectionsOpenMax());
	}

	private void configConnector(AbstractConnector conector) {
		// turn on collection of statistics by Jetty
		connector.setStatsOn(true);
		// configure the thread pool for accepting connections
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(WEB_SERVER_MIN_THREADS);
		threadPool.setMaxThreads(WEB_SERVER_MAX_THREADS);
		threadPool.setName("web-server");
		connector.setThreadPool(threadPool);
		// set whether or not to reuse the addresses
		connector.setReuseAddress(true);
	}

	private static class DefaultErrorHandler extends HandlerWrapper {

		public DefaultErrorHandler(Handler delegate) {
			setHandler(delegate);
		}

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
				throws IOException {
			try {
				super.handle(target, baseRequest, request, response);
			} catch (Throwable th) {
				if (response.isCommitted()) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} else {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				// don't re-throw
			} finally {
				if (!baseRequest.isHandled() && response.getStatus() == HttpServletResponse.SC_OK) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
	}
}
