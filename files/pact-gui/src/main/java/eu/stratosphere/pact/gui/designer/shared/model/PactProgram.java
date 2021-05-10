package eu.stratosphere.pact.gui.designer.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.GwtTransient;

import eu.stratosphere.pact.gui.designer.client.event.ConnectionAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactValidationException.ValidationErrorType;

/**
 * Whole PactProgram model containing jarFiles, connections and pacts. Offers
 * all management functions to add and remove pacts and connections
 * 
 * @author MathiasNitzsche@gmail.com
 * 
 */
public class PactProgram implements Serializable {
	private static final long serialVersionUID = -2826064505541133085L;

	/*
	 * All pacts in this pact program
	 */
	private HashMap<Integer, Pact> pacts = new HashMap<Integer, Pact>();

	/*
	 * All connections in this pact program
	 */
	private HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();

	/*
	 * All jarFiles in this pact program
	 */
	private HashMap<Integer, JarFile> jarFiles = new HashMap<Integer, JarFile>();

	/*
	 * Program id - only relevant for one session. When loading pact program, a
	 * new id is assigned
	 */
	private int id;

	/*
	 * Name of Pact program. Important for package and jar name.
	 */
	private String name = "no name";

	@GwtTransient
	private PactProgramManager pactProgramManager = null;

	/**
	 * Empty constructor needed for serialization
	 */
	public PactProgram() {
	}

	/*
	 * getter and setter
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return "eu.stratosphere.pact.example." + getName().toLowerCase();
	}

	public HashMap<Integer, Pact> getPacts() {
		return pacts;
	}

	public void setPacts(HashMap<Integer, Pact> pacts) {
		this.pacts = pacts;
	}

	public HashMap<Integer, Pact> getPactsByType(PactType type) {
		HashMap<Integer, Pact> pactsOfType = new HashMap<Integer, Pact>();
		for (Pact pact : pacts.values()) {
			if (pact.getType() == type) {
				pactsOfType.put(pact.getId(), pact);
			}
		}
		return pactsOfType;
	}

	public Pact getPactById(int id) {
		return (pacts.containsKey(id) ? pacts.get(id) : null);
	}

	public Pact createNewPact(PactType pactType) {
		Pact newPact = new Pact();
		newPact.setPactProgram(this);
		newPact.setId(getNextObjectId());
		newPact.setType(pactType);
		String newPactName = newPact.getType().getLabel() + "Name" + newPact.getId();

		// validate Name (Name is always valid as this point, but might be taken
		// already)
		PactValidationException nameError = valdiatePactName(newPactName);
		if (nameError != null) {
			newPactName = newPact.getType().getLabel() + "Name" + (new Date()).getTime();
		}

		newPact.setName(newPactName);
		pacts.put(newPact.getId(), newPact);
		Log.debug("createNewPact done");
		fireEvent(new PactAddEvent(newPact));
		return newPact;
	}

	public void fireEvent(GwtEvent<?> event) {
		if (getPactProgramManager() != null) {
			if (getPactProgramManager().getAppInjector() != null) {
				Log.debug("Fire event from PactProgram-Model: " + event.toString());
				getPactProgramManager().getAppInjector().getEventBus().fireEvent(event);
			} else {
				Log.error("getPactProgramManager().getAppInjector() == null");
			}
		} else {
			Log.error("getPactProgramManager() == null");
		}
	}

	public HashMap<Integer, Connection> getConnections() {
		return connections;
	}

	public void setConnections(HashMap<Integer, Connection> connections) {
		this.connections = connections;
	}

	public Connection getConnectionById(int id) {
		return (connections.containsKey(id) ? connections.get(id) : null);
	}

	public Connection createNewConnection() {
		Connection newConnection = new Connection();
		newConnection.setPactProgram(this);
		newConnection.setId(getNextObjectId());
		connections.put(newConnection.getId(), newConnection);
		fireEvent(new ConnectionAddEvent(newConnection));
		return newConnection;
	}

	public HashMap<Integer, JarFile> getJarFiles() {
		return jarFiles;
	}

	public void setJarFiles(HashMap<Integer, JarFile> jarFiles) {
		this.jarFiles = jarFiles;
	}

	public JarFile createNewJarFile() {
		JarFile jarFile = new JarFile();
		jarFile.setPactProgram(this);
		jarFile.setId(getNextObjectId());
		jarFiles.put(jarFile.getId(), jarFile);
		return jarFile;
	}

	/*
	 * pacts, connections and jarFiles share a sinle unique id generated
	 */
	public int getNextObjectId() {
		int nextId = 1;
		nextId = (connections.size() > 0 ? Math.max(Collections.max(connections.keySet()) + 1, nextId) : nextId);
		nextId = (pacts.size() > 0 ? Math.max(Collections.max(pacts.keySet()) + 1, nextId) : nextId);
		nextId = (jarFiles.size() > 0 ? Math.max(Collections.max(jarFiles.keySet()) + 1, nextId) : nextId);
		return nextId;
	}

	/*
	 * Validate pact name, when a single pact should be created or renamed.
	 * 
	 * @param newName
	 * 
	 * @param forWhichPact
	 * 
	 * @return
	 */
	public PactValidationException valdiatePactName(String newName) {
		return valdiatePactName(newName, null);
	}

	public PactValidationException valdiatePactName(String newName, Pact forWhichPact) {
		if (newName == null) {
			return new PactValidationException(ValidationErrorType.NOT_DEFINED);
		} else if (!newName.matches("^[A-Z]{1}.*")) {
			return new PactValidationException(ValidationErrorType.WRONG_FIRST_CHAR);
		} else if (!newName.matches("^[a-zA-Z0-9\\_]+$")) {
			return new PactValidationException(ValidationErrorType.WRONG_CHARS);
		}

		for (Pact pact : pacts.values()) {
			if (pact != forWhichPact && newName.equalsIgnoreCase(pact.getName())) {
				return new PactValidationException(ValidationErrorType.NAME_EXISTS);
			}
		}

		return null;
	}

	/*
	 * this function revalidates the model. Adding backreferences to all child
	 * objecs. Removing impossible connections...
	 */
	public void refresh() {
		for (Pact pact : pacts.values()) {
			pact.setPactProgram(this);
		}

		for (Connection connection : connections.values()) {
			connection.setPactProgram(this);

			// asure that each pact only has is allowed maximum count of input
			// connections; eg with less inputs -
			Pact toPact = connection.getToPact();
			if (toPact != null && connection.getToChannelNumber() >= toPact.getType().getNumberOfInputs()) {
				connection.setToPact(null);
			}
		}
	}

	/*
	 * source and sink pacts need to be numbered - for compilation
	 */
	public int getSourceOrSinkNumber(Pact pact) {
		// TODO: do this better !
		ArrayList<Integer> keys = new ArrayList<Integer>(getPactsByType(pact.getType()).keySet());
		Collections.sort(keys);
		return keys.indexOf(pact.getId());
	}

	/*
	 * Validate whole pact program - done before compilation
	 */
	public ArrayList<Error> getValidationErrors() {
		ArrayList<Error> errors = new ArrayList<Error>();

		for (Pact pact : pacts.values()) {
			errors.addAll(pact.getValidationErrors());
		}
		for (Connection connection : connections.values()) {
			errors.addAll(connection.getValidationErrors());
		}
		if (getPactsByType(PactType.SOURCE).size() == 0) {
			errors.add(new Error("No data source existing"));
		}
		if (getPactsByType(PactType.SINK).size() == 0) {
			errors.add(new Error("No data sink existing"));
		}

		return errors;
	}

	/*
	 * public ArrayList<Pact> getInputPactsFor(Pact pact) { return null; }
	 * 
	 * public Pact getOutputPactFor(Pact pact) { return null; }
	 */
	public String toString() {
		String out = "PACT-Program(";

		out += "id: " + id;
		out += ", name: " + name;
		out += ", nextObjectId:" + getNextObjectId();
		out += ")\n";

		out += pacts.size() + " pact(s)\n";
		for (Pact pact : pacts.values()) {
			out += " - " + pact + "\n";
		}

		out += connections.size() + " connection(s)\n";
		for (Connection connection : connections.values()) {
			out += " - " + connection + "\n";
		}
		
		out += jarFiles.size() + " jarFiles(s)\n";
		for (JarFile jarFile : jarFiles.values()) {
			out += " - " + jarFile + "\n";
		}

		return out;
	}

	public void setPactProgramManager(PactProgramManager pactProgramManager) {
		this.pactProgramManager = pactProgramManager;
	}

	public PactProgramManager getPactProgramManager() {
		return pactProgramManager;
	}

	public void removeJarFile(JarFile jarFile) {
		if (jarFiles.containsValue(jarFile)) {
			jarFiles.remove(jarFile.getId());
			fireEvent(new PactProgramPropertyChangeEvent(this));
		}
	}
}