package de.persosim.rcp.cli;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Class that maps commands to requests for the PersoSim components
 * providing the corresponding functionality.
 *
 * @author Giorgio Busatto
 */
public class CommandProcessor {
	public static String OK = "ok";

	public static String ERROR = "error";

	private final ResponseCollector _collector;

	public CommandProcessor() {
		_collector = new ResponseCollector();		
	}

	public String selectReaderType(final String readerType) {
		if ("basic".equals(readerType) || "standard".equals(readerType)) {
			final Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("READER_TYPE", readerType);

			return sendRequest("select-reader", properties);
		}
		else {
			return ERROR;
		}
	}

	public String enterPin(final String pin) {
		boolean allDigits = true;
		for (int i = 0; i < pin.length(); i++) {
			if (!Character.isDigit(pin.charAt(i))) {
				allDigits = false;
				break;
			}
		}

		if (allDigits) {
			final Map<String,Object> properties = new HashMap<String, Object>();
			properties.put("PIN", pin);

			return sendRequest("enter-pin", properties);
		}
		else {
			return ERROR;
		}
	}

	public String selectPersoFromFile(String filePath) {
        final Map<String,Object> properties = new HashMap<String, Object>();
        properties.put("PERSONALIZATION_FILE_PATH", filePath);

		return sendRequest("select-perso", properties);
	}

	/**
	 * Send a request via OSGI and wait for a response.
	 * Synchronize with the thread processing the request.
	 *
	 * @param requestType
	 * @param properties
	 *
	 * @return the response from the component carried out the request on success, or error otherwise
	 */
	public String sendRequest(final String requestType, final Map<String, Object> properties) {
		try {
			final BundleContext ctx = FrameworkUtil.getBundle(CommandProcessor.class).getBundleContext();
			final ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
			final EventAdmin eventAdmin = ctx.getService(ref);

			_collector.clearResponse();
			eventAdmin.sendEvent(new Event("repl_request/" + requestType, properties));
			try {
				_collector.waitForResponse();

				final String response = _collector.getResponse();
				return response == null ? ERROR : response;
			}
			catch (InterruptedException ex) {
				return ERROR;
			}
		}
		catch (NullPointerException ex) {
			return ERROR;
		}
	}
}