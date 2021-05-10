package eu.stratosphere.pact.gui.designer.server.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HelperClass to log event during Compilation
 * log for file, console and client
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class CustomLogger {
	private Logger consoleLog = Logger.getLogger("");
	private String logString = "";
	private File logFile = null;
	private int linecount = 0;

	public CustomLogger() {
	}

	public void log(Level level, String msg) {
		if (level != null && msg != null && !"".equals(msg)) {
			linecount++;
			String logLine = linecount + " " + (level == Level.INFO ? "INFO" : "ERROR") + ": " + msg + "\n";
			logString += logLine;
			consoleLog.log(level, msg);
			if (logFile != null) {
				Utils.createAndAppendFile(logFile.getAbsolutePath(), logLine, true);
			}
		}
	}

	public void info(String text) {
		log(Level.INFO, text);
	}

	public void error(String text) {
		log(Level.SEVERE, text);
	}

	public void setLogFilePath(String path) {
		logFile = Utils.createAndAppendFile(path, logString, false);
		if (!logFile.exists()) {
			this.logFile = null;
		}
	}
	public String getLogFilePath() {
		if (logFile != null) {
			return logFile.getAbsolutePath();
		}
		return null;
	}

	public String getOutput() {
		return logString;
	}

}
