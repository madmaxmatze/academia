package eu.stratosphere.pact.gui.designer.server;

import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.stratosphere.pact.gui.designer.client.async.AjaxService;
import eu.stratosphere.pact.gui.designer.server.util.Utils;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;


/**
 * The server side implementation of the RPC service.
 * http://code.google.com/webtoolkit/doc/latest/tutorial/RPC.html
 */
public class AjaxServlet extends RemoteServiceServlet implements AjaxService {
	private static final long serialVersionUID = -5188671048193825195L;

	/**
	 * server side standard java.util logger
	 */
	private static Logger Log = Logger.getLogger("");

	/**
	 * Perform login based on passed name and password Create session with
	 * attribute loggedin=true
	 * 
	 * More about Login:
	 * http://code.google.com/p/google-web-toolkit-incubator/wiki
	 * /LoginSecurityFAQ http://snipt.net/javagner/session-in-gwt/
	 * 
	 * @param name
	 * @param password
	 * @return
	 */
	public String login(String name, String password) {
		Log.info("loginAction");
		
		// if session exists and already logged in
		HttpSession session = this.getThreadLocalRequest().getSession();
		if (session != null) {
			if ("TRUE".equalsIgnoreCase((String) session.getAttribute("loggedin"))) {
				Log.info("Session existing and valid. Return sessionId:" + session.getId());
				return session.getId();
			}
		}

		// test if login is valid
		if (ServerFuntions.isUserPasswordValid(name, password)) {
			Log.info("Login valid.");
			session.setAttribute("loggedin", "TRUE");

			Log.info("return sessionId:" + session.getId());
			return session.getId();
		
		}

		Log.info("No valid login until end of loginAction. Return null.");
		return null;
	}

	/**
	 * Since this is a client side app - with very few interactions with the
	 * server, this method is used to check if still login
	 */
	public Boolean isLoggedIn(String sessionId) {
		Log.info("isLoggedIn");
		HttpSession session = this.getThreadLocalRequest().getSession();

		if (session != null && sessionId != null) {
			return (sessionId.equals(session.getId()) && "TRUE".equalsIgnoreCase((String) session
					.getAttribute("loggedin")));
		}

		return false;
	}

	/**
	 * Perform logout via ajax call
	 */
	@Override
	public Boolean logout() {
		Log.info("Logout");

		HttpSession session = this.getThreadLocalRequest().getSession();
		if (session != null) {
			session.removeAttribute("loggedin");
			session.invalidate();
		}

		Log.info("logged out (on server)");
		return true;
	}

	/**
	 * Upload current pact program manager from client to server. Needed to
	 * later download via servlet request a pactProgram as xml or jar.
	 */
	@Override
	public Boolean setPactProgramManagerOnServer(String programHash, PactProgramManager pactProgramManager) {
		Log.info("setPactProgramManagerOnServer:");

		HttpSession session = this.getThreadLocalRequest().getSession();
		if (session != null) {
			Log.info("session != null");
			Log.info("save mng to session attr: " + "pactProgramManager" + programHash);

			session.setAttribute("pactProgramManager" + programHash, pactProgramManager);
		}

		return true;
	}

	/**
	 * When opening a xml file, it is uploaded to server. Afterwards this method
	 * is used to download the pactProgram via ajax
	 */
	@Override
	public PactProgram getUploadedPactProgramFromServer(String programHash) {
		Log.info("getUploadedPactProgramFromServer");

		HttpSession session = this.getThreadLocalRequest().getSession();
		if (session != null) {
			PactProgram pactProgram = (PactProgram) session.getAttribute("uploadedPactProgram" + programHash);
			if (pactProgram != null) {
				Log.info("return pactProgram: " + pactProgram);
				session.removeAttribute("uploadedPactProgram" + programHash);
				return pactProgram;
			}
		}

		Log.info("No uploaded pact found on server. Return null");
		return null;
	}

	/**
	 * Compile the given pact program to a jar
	 * 
	 * The result is a hashMap of files <FullFilePath, FileContent>
	 */
	@Override
	public PactProgramCompilerResult compilePactProgram(PactProgram pactProgram) {
		HttpSession session = this.getThreadLocalRequest().getSession();
		PactProgramCompiler pactProgramCompiler = new PactProgramCompiler(pactProgram, getServletContext(), session);
		return pactProgramCompiler.getResults();
	}

	
	/**
	 * delete a compilation temp folder via ajax request (when a compilation
	 * result tab is closed)
	 */
	@Override
	public Boolean deletePactTempFolder(String absolutefolderPath) {
		Log.info("deletePactTempFolder: " + absolutefolderPath);
		boolean deleted = false;
		if (absolutefolderPath != null) {
			if (absolutefolderPath.contains("/pact_gui_")) {
				Log.info("call DELETE in file util: " + absolutefolderPath);
				deleted = Utils.delete(absolutefolderPath);
			}
		}

		return deleted;
	}
}
