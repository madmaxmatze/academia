package eu.stratosphere.pact.gui.designer.client.async;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * The client side stub for the RPC service.
 * (defines client site interface to access eu.stratosphere.pact.gui.designer.server.AjaxServiceImpl)
 * 
 * \@RemoteServiceRelativePath defines url of AjaxServiceImpl-servlet
 * 
 * @author MathiasNitzsche@gmail.com
 * 
 * http://code.google.com/intl/de-DE/webtoolkit/doc/1.6/DevGuideServerCommunication.html#DevGuideImplementingServices
 */
@RemoteServiceRelativePath("backend/ajax")
public interface AjaxService extends RemoteService {
	String login(String name, String password);
	
	Boolean logout();

	Boolean isLoggedIn(String sessionId);

	Boolean setPactProgramManagerOnServer(String programHash, PactProgramManager pactProgramManager);

	PactProgramCompilerResult compilePactProgram(PactProgram pactProgram);

	PactProgram getUploadedPactProgramFromServer(String programHash);
	
	Boolean deletePactTempFolder(String absolutefolderPath);
}
