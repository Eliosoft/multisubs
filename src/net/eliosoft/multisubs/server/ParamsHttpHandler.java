package net.eliosoft.multisubs.server;


import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.eliosoft.multisubs.CSVTable;
import net.eliosoft.multisubs.WebTextListener;

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
    private ExecutorService executorService = Executors.newCachedThreadPool();
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
    if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
    	final Timer timer = new Timer();
    	final WebTextListener listener = new WebTextListener() {
			
			@Override
			public boolean webTextChanged(final String[] webTexts) {
				timer.cancel();
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						answerToRequest(httpExchange, webTexts);
					}
				});
				return true;
			}
		};
		CSVTable.addWebTextListener(listener);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				CSVTable.removeWebTextListener(listener);
				answerToRequest(httpExchange, CSVTable.getWebText());
			}
		}, 5000);
	} else {
	    String badMethod = "405 : Method not allowed !!!";
	    httpExchange.sendResponseHeaders(405, badMethod.length());
	    httpExchange.getResponseBody().write(badMethod.getBytes());
	    httpExchange.getResponseBody().close();
	}
    }
	/**
	 * @param httpExchange
	 * @param webTexts
	 */
	private void answerToRequest(final HttpExchange httpExchange,
			final String[] webTexts) {
		URI u = httpExchange.getRequestURI();
		String query = u.getQuery();
		String responseOk;
		int textIndex = Integer.parseInt(query);
		responseOk = textIndex < webTexts.length ? webTexts[textIndex] : "";
		
		try {
			httpExchange.sendResponseHeaders(200, responseOk.getBytes().length);
			httpExchange.getResponseBody().write(responseOk.getBytes());
			httpExchange.getResponseBody().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
