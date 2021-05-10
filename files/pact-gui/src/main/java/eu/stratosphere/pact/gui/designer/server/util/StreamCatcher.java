package eu.stratosphere.pact.gui.designer.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper to catch runtime process output when performing javac and jar commands
 * 
 * http://vyvaks.wordpress.com/2006/05/27/does-runtimeexec-hangs-in-java/
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * with inputStream to String java was hanging sometime:
 * http://www.java2s.com/Tutorial/Java/0180__File/LoadatextfilecontentsasaString.htm
 */
public class StreamCatcher extends Thread {
	private InputStream is;
	private String output = "";

	public StreamCatcher(InputStream is) {
		this.is = is;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				output += line + "\n";
			}
		} catch (IOException e) {
			output += "IOException in StreamGobbler::run(): " + e.getMessage() + "\n";
		}
	}

	public String getOutput() {
		return output;
	}

}
