package de.persosim.rcp.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;


/**
 * Read-eval-print-loop class.
 *
 * It processes each line as a command and sends a response back.
 */
public class REPL {
	private final CommandProcessor _commandProcessor = new CommandProcessor();

	final BufferedReader _in;

	final PrintStream _out;

	private String [] getTokens(String line) {
		return line.split("[ \t][ \t]*");
	}

	public REPL(final InputStream in, final PrintStream out) {
		_in = new BufferedReader(new InputStreamReader(in));

		_out = out;
	}

	/**
	 * Process a command. Accepted commands are:
	 *
	 *   select-reader basic | standard
	 *   select-perso <absolute-path-to-personalization-file>
	 *   enter-pin <PIN>
	 *
	 * @param line the line containing the command
	 * @return a result string
	 */
	public String processLine(String line) {
		final String [] tokens = getTokens(line);
		if (tokens.length != 2) {
			return CommandProcessor.ERROR;
		}
		else {
			switch (tokens[0]) {
				case "select-reader": return _commandProcessor.selectReaderType(tokens[1]);

				case "enter-pin": return _commandProcessor.enterPin(tokens[1]);

				case "select-perso": return _commandProcessor.selectPersoFromFile(tokens[1]);

				default: return CommandProcessor.ERROR;
			}
		}
	}

	public String runSession() throws IOException {
		String line = _in.readLine();
		while (line != null) {
			final String trimmedLine = line.trim();
			System.out.println("Got command: " + trimmedLine);

			// The special commands "close" and "shutdown" do not cause any processing.
			if ("close".equals(trimmedLine) || "shutdown".equals(trimmedLine)) {
				_out.println(trimmedLine);

				return trimmedLine;
			}
			else {
				final String result = processLine(trimmedLine);
				System.out.println("Got response: " + result);

				_out.println(result);

				line = _in.readLine();
			}
		}

		return "close";
	}
}