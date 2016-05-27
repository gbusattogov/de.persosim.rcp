package de.persosim.rcp.cli;

import java.io.*;
import java.net.*;


/**
 * Command-line tool that can connect with the CLIBackend running in
 * PersoSim.
 *
 * @author Giorgio Busatto
 */
public class CLI {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
            System.err.println("Usage: java de.persosim.rcp.cli.CLI <host name> <port number>");

            System.exit(1);
        }

		final String hostName = args[0];
        final int portNumber = Integer.parseInt(args[1]);
 
        try
        (
            final Socket socket = new Socket(hostName, portNumber);
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        )
        {
        	System.out.print("> ");
        	String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);

                final String response = in.readLine();
                if (response == null) {
                    System.err.println("Connection was closed: exiting");

                    System.exit(1);
                }

                System.out.println(response);
                if ("close".equals(response) || "shutdown".equals(response)) {
                	break;
                }
                else {
                	System.out.print("> ");
                }
            }
        }
        catch (UnknownHostException e) {
            System.err.println("Unknown host " + hostName);

            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName + ":" + portNumber);

            System.exit(1);
        } 
    }
}
