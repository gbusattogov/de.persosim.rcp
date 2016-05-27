package de.persosim.rcp.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * CLI back end class. It waits for incoming connections.
 * When a connection is accepted, a read-eval-print-loop (REPL)
 * object is created and given control.
 *
 * The REPL processes each line it receives as a command and
 * sends a response back through the open socket.
 *
 * A REPL session terminates when one of the special commands
 * "close" or "shutdown" is received. The result of the session
 * is then the command itself.
 * A REPL session can also be terminated by an error. In this
 * case, the result "close" is returned.
 *
 * On "close", the CLI back end closes the open socket and
 * waits for a new connection.
 * On "shutdown", the CLI back end shuts down the PersoSim application.
 *
 * @author Giorgio Busatto
 */
public class CLIBackend implements Runnable {
	private static int CLI_PORT = 9090;

	private Socket getConnection() {
		try (final ServerSocket serverSocket = new ServerSocket(CLI_PORT)) {
			return serverSocket.accept();
		}
		catch (IOException e) {
			System.err.println("Exception caught when trying to listen on port " + CLI_PORT + " or listening for a connection");
	        System.err.println(e.getMessage());

	        return null;
	    }
	}

	public void run() {
		try {
			String result = null;
			Socket socket = getConnection();
			while (true) {
				final InputStream in = socket.getInputStream();
				final PrintStream out = new PrintStream(socket.getOutputStream(), true);
				final REPL repl = new REPL(in, out);
				try {
					result = repl.runSession();
				}
				catch (NullPointerException ex) {
					System.err.println("Cannot process request, closing connection");
					out.println("close");
					result = "close";
				}

				if ("shutdown".equals(result)) {
					socket.close();
					System.exit(0);
				}
				else if ("close".equals(result)) {
					socket.close();
					socket = getConnection();
				}
				else {
					// This should never happen.
					throw new RuntimeException("Unknown result");
				}
			}
		}
		catch (IOException ex) {
			System.err.println("IO error: " + ex.getMessage());

			System.exit(1);
		}
	}
}