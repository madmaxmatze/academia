package eu.stratosphere.pact.gui.designer.client.component.programlist;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.sencha.gxt.widget.core.client.info.Info;

import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.JarFile;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * This handler class is passed from presenter to view to handle the result of a
 * jar update process
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class JarUploadHandler {
	private PactProgram pactProgram = null;
	@SuppressWarnings("unused")
	private AppInjector appInjector = null;

	public JarUploadHandler(AppInjector appInjector, PactProgram pactProgram) {
		this.pactProgram = pactProgram;
		this.appInjector = appInjector;
	}

	public void handle(String result) {
		// strip html tags from result
		Log.info("event.getResults() pure:" + result);
		result = result.replaceAll("\\<.+?\\>", "");
		Log.info("event.getResults():" + result);

		// parse result to json object
		String fileHash = null;
		String fileName = null;
		try {
			JSONObject json = (JSONObject) JSONParser.parseStrict(result);
			Log.info("json:" + json);
			JSONValue fileHashJson = json.get("fileHash");
			if (fileHashJson != null) {
				JSONString fileHashJsonString = fileHashJson.isString();
				if (fileHashJsonString != null) {
					fileHash = fileHashJsonString.stringValue();
				}
				Log.info("fileHash: '" + fileHash + "'");
			}
			JSONValue fileNameJson = json.get("fileName");
			if (fileNameJson != null) {
				JSONString fileNameJsonString = fileNameJson.isString();
				if (fileNameJsonString != null) {
					fileName = fileNameJsonString.stringValue();
				}
				Log.info("fileName: '" + fileName + "'");
			}
		} catch (Exception e) {
			Log.info("JOSN Exception:" + e.getMessage());
		}

		if (fileName != null && fileHash != null) {
			JarFile jarFile = pactProgram.createNewJarFile();
			jarFile.setName(fileName, false);
			jarFile.setHash(fileHash, true);
		} else {
			Info.display("No JAR ADDED", "ERROR");
		}
	}
}
