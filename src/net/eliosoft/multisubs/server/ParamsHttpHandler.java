package net.eliosoft.multisubs.server;


import java.io.IOException;
import java.net.URI;

import net.eliosoft.multisubs.CSVTable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * This handler process data requests. These request send commandLine value by
 * POST method
 * 
 * @author Jeremie GASTON-RAOUL
 * 
 */
public class ParamsHttpHandler implements HttpHandler {

    private static final int MAX_BUFFER_SIZE = 1024 * 512;

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
    if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
    	URI u = httpExchange.getRequestURI();
    	String query = u.getQuery();
    	String responseOk;
    	responseOk = CSVTable.getWebText()[Integer.parseInt(query)];

	    httpExchange.sendResponseHeaders(200, responseOk.getBytes().length);
	    httpExchange.getResponseBody().write(responseOk.getBytes());
	    httpExchange.getResponseBody().close();
	} else {
	    String badMethod = "405 : Method not allowed !!!";
	    httpExchange.sendResponseHeaders(405, badMethod.length());
	    httpExchange.getResponseBody().write(badMethod.getBytes());
	    httpExchange.getResponseBody().close();
	}
    }

}
