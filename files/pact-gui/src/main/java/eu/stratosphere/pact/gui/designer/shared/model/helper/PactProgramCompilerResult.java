package eu.stratosphere.pact.gui.designer.shared.model.helper;

import java.io.Serializable;
import java.util.HashMap;

public class PactProgramCompilerResult implements Serializable {
	private static final long serialVersionUID = 6460966316809367340L;

	public PactProgramCompilerResult() {

	}

	private int pactProgramId = -1;

	public int getPactProgramId() {
		return pactProgramId;
	}

	public void setPactProgramId(int pactProgramId) {
		this.pactProgramId = pactProgramId;
	}

	/**
	 * path prefix for all files
	 */
	private String serverTempDirPath = null;

	public String getServerTempDirPath() {
		return serverTempDirPath;
	}

	public void setServerTempDirPath(String serverTempDirPath) {
		if (serverTempDirPath != null) {
			serverTempDirPath = serverTempDirPath.replace("\\", "/");
		}

		this.serverTempDirPath = serverTempDirPath;
	}

	public boolean isDownloadPossible() {
		return (serverTempDirPath != null && serverTempDirPath.length() > 0);
	}

	/**
	 * list of files from compilation
	 */
	private HashMap<String, String> files = new HashMap<String, String>();

	public HashMap<String, String> getFiles() {
		return files;
	}

	// public void setFiles(HashMap<String, String> files) {
	// this.files = files;
	// }

	/**
	 * add file to result set
	 */
	public void addFileToResultSet(String path, String content) {
		if (path != null && !"".equals(path)) {
			path = path.replace("\\", "/");
			files.put(path, content);
		}
	}

	/**
	 * this widget will be asked from outside if the compilation was successful
	 * mainly to define icon (red / green)
	 */
	boolean wasSuccessful = false;

	public boolean wasSuccessFul() {
		return wasSuccessful;
	}

	public void wasSuccessFul(boolean wasSuccessful) {
		this.wasSuccessful = wasSuccessful;
	}
}
