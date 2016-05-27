package de.persosim.rcp.cli;

import java.util.Dictionary;
import java.util.Hashtable;

import org.globaltester.logging.BasicLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.persosim.simulator.Activator;


/**
 * Helper class that collects responses from various components
 * and delivers them to the CLI.
 *
 * Usage:
 *    clearResponse()
 *    waitForResponse()
 *    getResponse()
 *
 * @author Giorgio Busatto
 */
public class ResponseCollector {
	private ServiceRegistration<?> _registration = null;

	private String _response = null;

	public ResponseCollector() {
		register();
	}

	private synchronized void setResponse(final String response) {
		_response = response;

		notify();
	}

	public synchronized void clearResponse() {
		_response = null;
	}

	public synchronized void waitForResponse() throws InterruptedException {
		if (_response == null) {
			wait(10000);
		}
		else {
			// The response is already there, no need to wait.			
		}
	}

	public synchronized String getResponse() {
		return _response;
	}

	private void register() {
	    final Bundle bundle = FrameworkUtil.getBundle(ResponseCollector.class);
	    if (bundle == null) {
			BasicLogger.log(getClass(), "Cannot find bundle");

	    	return;
	    }

	    final BundleContext ctx = Activator.getContext();
	    if (ctx == null) {
			BasicLogger.log(getClass(), "Cannot find context");

	    	return;
	    }

	    final EventHandler handler = new EventHandler() {
	    	public void handleEvent(final Event event) {
	    		final String topic = event.getTopic();
	    		if (!"repl_response/result".equals(topic)) {
	    			return;
	    		}

	    		final Object val = event.getProperty("RESULT");
	    		if (val == null || !(val instanceof String)) {
	    			return;
	    		}

	    		setResponse((String) val);
	    	}
	    };

	    final Dictionary<String, String> properties = new Hashtable<String, String>();
	    properties.put(EventConstants.EVENT_TOPIC, "repl_response/*");
	    _registration = ctx.registerService(EventHandler.class, handler, properties);

		BasicLogger.log(getClass(), "Registered on event: repl_response/*");
	}

	public void finalize() {
	    if (_registration != null) {
			_registration.unregister();
	    }
	}
}