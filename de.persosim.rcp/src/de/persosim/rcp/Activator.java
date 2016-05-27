package de.persosim.rcp;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.persosim.rcp.cli.CLIBackend;
import de.persosim.simulator.PersoSim;
import de.persosim.simulator.perso.Personalization;
import de.persosim.simulator.perso.PersonalizationFactory;
import de.persosim.simulator.ui.parts.PersoSimPart;

public class Activator implements BundleActivator {

	private Thread _cli = null;

	@Override
	public void start(BundleContext context) throws Exception {
		de.persosim.simulator.Activator.getDefault().enableService();
		startSimAndConnectToNativeDriver();
		if (_cli == null) {
			_cli = new Thread(new CLIBackend(), "CLI");
		}

		if (!_cli.isAlive()) {
			_cli.start();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		de.persosim.simulator.Activator.getDefault().disableService();

		if (_cli != null && _cli.isAlive()) {
			// Since the CLI uses synchronous I/O on a socket, its thread
			// can be blocked waiting for the next line of input.
			// Use a timeout of 1 second to stop it.
			System.out.print("Waiting for the CLI to stop... ");
			_cli.join(/* Timeout in milliseconds */ 1000);
			_cli = null;
			System.out.println("stopped.");
		}
	}

	/**
	 * This method handles the connection to the simulator. Its primary task is
	 * to ensure the simulator is up and running when a connection is
	 * initialized. This method uses the {@link Simulator} provided by the
	 * {@link de.persosim.simulator.Activator}.
	 */
	private void startSimAndConnectToNativeDriver() {
			de.persosim.simulator.Activator persoSimActivator = de.persosim.simulator.Activator.getDefault();
			PersoSim sim = persoSimActivator.getSim();
			try {
				sim.startSimulator();
				sim.loadPersonalization(getDefaultPersonalization());
			} catch (IOException e) {
				e.printStackTrace();
				MessageDialog.openError(null, "Error", "Failed to automatically load default personalization");
				return;
			}
			de.persosim.simulator.ui.Activator.disconnectFromNativeDriver();
			de.persosim.simulator.ui.Activator.connectToNativeDriver();
	}
	
	/**
	 * This method returns a personalization which can be used as default.
	 * @return a default personalization
	 * @throws IOException
	 */
	private Personalization getDefaultPersonalization() throws IOException {
		Bundle plugin = Platform.getBundle(PersoSimPart.DE_PERSOSIM_SIMULATOR_BUNDLE);
		URL url = plugin.getEntry (PersoSimPart.PERSO_PATH);
		URL resolvedUrl;
		
		resolvedUrl = FileLocator.resolve(url);
		
		File folder = new File(resolvedUrl.getFile());
		String pathString = folder.getAbsolutePath() + File.separator + PersoSimPart.PERSO_FILE;
		
		System.out.println("Loading default personalization from: " + pathString);
		
		Personalization personalization = (Personalization) PersonalizationFactory.unmarshal(pathString);
		
		return personalization;
	}
}
