package net.eliosoft.multisubs.server;
/*
 * This file is part of Elios.
 *
 * Copyright 2010 Jeremie GASTON-RAOUL & Alexandre COLLIGNON
 *
 * Elios is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Elios is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Elios. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eliosoft.multisubs.CSVTable;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

/**
 * The Manager of the Http Server.
 * 
 * @author Jeremie GASTON-RAOUL
 */
public class HttpServerManager {
	private static HttpServerManager instance;

	/**
	 * default value for http port.
	 */
	public static final int DEFAULT_HTTP_PORT = 8080;

	private Server server = null;
	private int inPort = HttpServerManager.DEFAULT_HTTP_PORT;

	private HttpServerManager() {
	}

	/**
	 * get the singleton instance of the HttpServerManager.
	 * 
	 * @return the instance
	 */
	public static HttpServerManager getInstance() {
		if (HttpServerManager.instance == null) {
			HttpServerManager.instance = new HttpServerManager();
		}
		return HttpServerManager.instance;
	}

	/**
	 * Starts the Http Server.
	 * 
	 * @throws IOException
	 *             if the server is unable to start
	 */
	public void startHttp() throws IOException {
		this.initHttpServer();
		try {
			this.server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Stops the Http Server.
	 */
	public void stopHttp() {
		if (this.server != null) {
			try {
				this.server.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.server = null;
		}
	}

	/**
	 * set the in port of the server.
	 * 
	 * @param inPort
	 *            the value of the port
	 */
	public void setInPort(final int inPort) {
		this.inPort = inPort;
	}

	private void initHttpServer() throws IOException {
		
		// Create a basic Jetty server object that will listen on port 8080.  Note that if you set this to port 0
        // then a randomly available port will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        this.server = new Server(8080);

        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/");
        // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
        // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
        ResourceHandler resourceHandler = new ResourceHandler();
        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.htm" });
        resourceHandler.setResourceBase(this.getClass().getResource("/net/eliosoft/multisubs/server/files").getPath());
        context0.setHandler(resourceHandler);
 
        // Rinse and repeat the previous item, only specifying a different resource base.
        ContextHandler context1 = new ContextHandler();
        context1.setContextPath("/data/params");   
        Handler paramsHandler = new DefaultHandler() {
						
			@Override
			public void handle(String target,Request baseRequest,HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
				if (request.getMethod().equalsIgnoreCase("GET")) {
			    	HttpURI u = baseRequest.getUri();
			    	String query = u.getQuery();
			    	String responseOk;
			    	responseOk = CSVTable.getWebText()[Integer.parseInt(query)];

			    	response.setStatus(HttpServletResponse.SC_OK);
				    response.getWriter().write(responseOk.toCharArray());
				    response.getWriter().close();
				} else {
				    String badMethod = "405 : Method not allowed !!!";
				    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				    response.getWriter().write(badMethod.toCharArray());
				    response.getWriter().close();
				}
			}
			
		};

        context1.setHandler(paramsHandler);
 
        // Create a ContextHandlerCollection and set the context handlers to it. This will let jetty process urls
        // against the declared contexts in order to match up content.
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]
        { context0, context1 });
 
        server.setHandler(contexts);
        server.setThreadPool(new ExecutorThreadPool(Executors.newFixedThreadPool(20)));
        // Add the ResourceHandler to the server.
//        HandlerList handlers = new HandlerList();
//        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
//        server.setHandler(handlers);
 
	}

}
