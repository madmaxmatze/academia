package eu.stratosphere.pact.gui.designer.server;

import java.util.logging.Logger;

/**
 * For better testing as much server side functions as possible should be in
 * this class
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class ServerFuntions {
	/**
	 * server side standard java.util logger
	 */
	private static Logger Log = Logger.getLogger("");

	/**
	 * test id user password combination is valid - for easier testability
	 */
	public static boolean isUserPasswordValid(String name, String password) {
		// when no name provided - abort login
		if ("".equals(name) || name == null) {
			Log.info("Name value empty. Login failed. Return null.");
			return false;
		}

		// when credentials ok - login
		if (name.startsWith("a")) {
			Log.info("Login valid.");
			return true;
		}

		return false;
	}

}
