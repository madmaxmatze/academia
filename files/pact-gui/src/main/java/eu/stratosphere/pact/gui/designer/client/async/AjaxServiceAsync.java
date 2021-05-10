package eu.stratosphere.pact.gui.designer.client.async;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * The async cliend side counterpart interface of <code>AjaxService</code>.
 * 
 * @author MathiasNitzsche@gmail.com
 */
public interface AjaxServiceAsync {
	void login(String name, String password, AsyncCallback<String> callback);

	void logout(AsyncCallback<Boolean> callback);

	void isLoggedIn(String sessionId, AsyncCallback<Boolean> callback);

	void setPactProgramManagerOnServer(String programHash, PactProgramManager pactProgramManager,
			AsyncCallback<Boolean> callback);

	void compilePactProgram(PactProgram pactProgram, AsyncCallback<PactProgramCompilerResult> asyncCallback);

	void getUploadedPactProgramFromServer(String programHash, AsyncCallback<PactProgram> callback);

	void deletePactTempFolder(String absolutefolderPath, AsyncCallback<Boolean> callback);
}
