package eu.stratosphere.pact.gui.designer.shared.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.GwtTransient;

import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException.ValidationErrorType;

/**
 * Manager class for multiple concurrently opened pact programs
 */
public class PactProgramManager implements Serializable {
	private static final long serialVersionUID = -257831357656274819L;

	/*
	 * HashMap of all Pact Programs currently opened by a user
	 */
	private HashMap<Integer, PactProgram> pactPrograms;

	/**
	 * Dependency Injection object
	 */
	// http://stackoverflow.com/questions/4295282/how-do-you-exclude-object-property-in-gwt-from-serialization
	@GwtTransient
	private AppInjector appInjector;

	public AppInjector getAppInjector() {
		return appInjector;
	}

	/*
	 * Empty constructor
	 */
	public PactProgramManager() {
		// Assuring that manager is empty. Just in case AppInjector does
		// something strange.
		resetPactPrograms();
	}
	
	@Inject
	public PactProgramManager(AppInjector appInjector) {
		this.appInjector = appInjector;
		// Assuring that manager is empty. Just in case AppInjector does
		// something strange.
		resetPactPrograms();
	}

	/*
	 * add pact program loaded from file. Assign new id
	 */
	public PactProgram addNewPactProgram(PactProgram pactProgram) {
		if (pactProgram != null) {
			if (!pactPrograms.containsValue(pactProgram)) {
				pactProgram.setPactProgramManager(this);
				pactProgram.setId(getNextPactProgramId());
				pactPrograms.put(pactProgram.getId(), pactProgram);
				fireEvent(new PactProgramAddEvent(pactProgram));
			}
		}
		
		return pactProgram;
	}

	public void fireEvent(GwtEvent<?> event) {
		Log.debug("Fire event from PactProgramManager-Model: " + event.toString());
		appInjector.getEventBus().fireEvent(event);
	}
	
	public PactProgram getPactProgramById(int id) {
		return pactPrograms.get(id);
	}

	public HashMap<Integer, PactProgram> getPactPrograms() {
		return pactPrograms;
	}

	public void removePactProgram(int id) {
		removePactProgram(getPactProgramById(id));
	}

	public boolean removePactProgram(PactProgram pactProgram) {
		Log.debug("model - removePactProgram with id: " + pactProgram.getId()
				+ " from pact program manager with progs: " + pactPrograms.keySet().toString());

		if (pactPrograms.containsValue(pactProgram)) {
			// not use remove with pactProgram object - expects ID!!!
			pactPrograms.remove(pactProgram.getId());
		
			Log.debug("pactProgram removed. PactProgramManager now contains " + pactPrograms.size()
					+ " pact program(s): " + pactPrograms.keySet().toString());
			
			fireEvent(new PactProgramRemoveEvent(pactProgram));
			return true;
		}
		return false;
	}

	/*
	 * factory to get new pact program from manager
	 */
	public PactProgram createNewPactProgram(String name) throws PactValidationException {
		// first Letter uppercase !!!
		if (name != null) {
			name = name.substring(0, 1).toUpperCase() + (name.length() > 1 ? name.substring(1) : "");
		}

		PactValidationException nameError = valdiatePactProgramName(name);
		if (nameError == null) {
			PactProgram newPactProgram = new PactProgram();
			newPactProgram.setName(name);
			return addNewPactProgram(newPactProgram);
		} else {
			throw nameError;
		}
	}

	/*
	 * generate unique new id for new pact program
	 */
	private int getNextPactProgramId() {
		return (pactPrograms.size() > 0 ? Collections.max(pactPrograms.keySet()) + 1 : 1);
	}

	public void resetPactPrograms() {
		pactPrograms = new HashMap<Integer, PactProgram>();
	}
	
	/*
	 * during development it's nice to always have a pact program created
	 */
	public PactProgram addTestPactProgram() {
		Log.info(this + ": addTestPactProgram");

		// Create an initial pact program for test purposes
		PactProgram pactProgram = null;
		try {
			pactProgram = createNewPactProgram("MyPact");
		} catch (PactValidationException e) {

		}

		if (pactProgram != null) {
			Pact source = pactProgram.createNewPact(PactType.SOURCE);
			source.setName("TextInput", false);
			source.setJavaCode(source.replacePactNameInCode(appInjector.i18n().pactSource()), true);

			Pact mapPact = pactProgram.createNewPact(PactType.PACT_MAP);
			mapPact.setName("WordSeperation", false);
			mapPact.setJavaCode(mapPact.replacePactNameInCode(appInjector.i18n().pactMap()), false);
			mapPact.setX(250, true);

			Pact reducePact = pactProgram.createNewPact(PactType.PACT_REDUCE);
			reducePact.setName("SumWordCount", false);
			reducePact.setJavaCode(reducePact.replacePactNameInCode(appInjector.i18n().pactReduce()), false);
			reducePact.setX(500, false);
			reducePact.setY(1500, true);

			Pact sink = pactProgram.createNewPact(PactType.SINK);
			sink.setName("ResultOutput", false);
			sink.setJavaCode(sink.replacePactNameInCode(appInjector.i18n().pactSink()), false);
			sink.setX(1200, true);

			Connection connection = pactProgram.createNewConnection();
			connection.setFromPact(source, false);
			connection.setToPact(mapPact, true);

			Connection connection1 = pactProgram.createNewConnection();
			connection1.setFromPact(mapPact, false);
			connection1.setToPact(reducePact, true);

			Connection connection2 = pactProgram.createNewConnection();
			connection2.setFromPact(reducePact, false);
			connection2.setToPact(sink, true);

			return pactProgram;
		}

		return null;
	}

	/*
	 * Manager validates new or changes pact program name
	 */
	public PactValidationException valdiatePactProgramName(String newName) {
		if (newName == null) {
			return new PactValidationException(ValidationErrorType.NOT_DEFINED);
		} else if (!newName.matches("^[A-Z]{1}.*")) {
			return new PactValidationException(ValidationErrorType.WRONG_FIRST_CHAR);
		} else if (!newName.matches("^[a-zA-Z0-9\\_]+$")) {
			return new PactValidationException(ValidationErrorType.WRONG_CHARS);
		}

		for (PactProgram pactProgram : pactPrograms.values()) {
			if (newName.equalsIgnoreCase(pactProgram.getName())) {
				return new PactValidationException(ValidationErrorType.NAME_EXISTS);
			}
		}

		return null;
	}

	public String toString() {
		String out = "PactProgramManager (programIds: " + pactPrograms.keySet() + ")";
		return out;
	}
}
