package eu.stratosphere.pact.gui.designer.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEvent;

/**
 * Pact class for data model - currently also representing sources and sinks -
 * via PactType-Attribute (later maybe replace with inheritance)
 */
public class Pact implements Serializable {
	private static final long serialVersionUID = -4682346393855188765L;

	/*
	 * pact program wide unique ID of this object
	 */
	private int id = 0;

	/*
	 * reference to parent pact program
	 */
	private PactProgram pactProgram = null;

	/*
	 * Type of this pact - defined in extra enum class Maybe better solved with
	 * class inheritance
	 */
	private PactType type = null;

	/*
	 * list of assigned output types for this pact
	 */
	private ArrayList<PactOutputType> outputContracts = new ArrayList<PactOutputType>();

	/*
	 * defined degree of parallelism
	 */
	private int degreeOfParallelism = 1;

	/*
	 * embedded user function
	 */
	private String javaCode = "/* EMPTY */";

	/*
	 * position when shown in graph
	 */
	private int x;
	private int y;

	/*
	 * Name of pact - should match class name in user function
	 */
	private String name = "";

	/**
	 * Empty constructor needed for serialization
	 */
	public Pact() {
		type = PactType.PACT_MAP;
	}

	/*
	 * Getter and setter
	 */
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		setName(name, true);
	}

	public void setName(String name, boolean fireEvent) {
		if (name != null && !name.equals(this.name)) {
			this.name = name;
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	/*
	 * This name is used for the PACT within the pact plan during compilation
	 */
	public String getCompilationName() {
		return type.getLabel().toLowerCase() + "_" + id + "_" + name;
	}

	public int getDegreeOfParallelism() {
		return degreeOfParallelism;
	}

	public void setDegreeOfParallelism(int degreeOfParallelism) {
		setDegreeOfParallelism(degreeOfParallelism, true);
	}

	public void setDegreeOfParallelism(int degreeOfParallelism, boolean fireEvent) {
		if (this.degreeOfParallelism != degreeOfParallelism) {
			this.degreeOfParallelism = degreeOfParallelism;
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	public void setX(int x) {
		setX(x, true);
	}

	public void setX(int x, boolean fireEvent) {
		if (this.x != x) {
			this.x = x;
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		setY(y, true);
	}

	public void setY(int y, boolean fireEvent) {
		if (this.y != y) {
			this.y = y;
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	public int getY() {
		return y;
	}

	public void setType(PactType type) {
		setType(type, true);
	}

	public void setType(PactType type, boolean fireEvent) {
		if (this.type != type) {
			if (type == null) {
				type = PactType.PACT_MATCH;
			}
			this.type = type;

			// when changed from 2 to 1 input, remove connection
			pactProgram.refresh();
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	public PactType getType() {
		return type;
	}

	public void setJavaCode(String javaCode) {
		setJavaCode(javaCode, true);
	}

	public void setJavaCode(String javaCode, boolean fireEvent) {
		if (javaCode == null) {
			javaCode = "";
		}
		if (!javaCode.equals(this.javaCode)) {
			this.javaCode = javaCode;
			if (fireEvent) {
				fireEvent(new PactPropertyChangeEvent(this));
			}
		}
	}

	public String getJavaCode() {
		return (javaCode == null ? "" : javaCode);
	}

	public String replacePactNameInCode(String code) {
		if (code != null) {
			code = code.replaceFirst("class.*?extends", "class " + getName() + " extends");
		}
		return code;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPactProgram(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}

	public ArrayList<PactOutputType> getOutputContracts() {
		return outputContracts;
	}

	public void setOutputContracts(ArrayList<PactOutputType> outputContracts) {
		this.outputContracts = outputContracts;
	}

	public void removeOutputContract(PactOutputType outputContract) {
		if (this.getOutputContracts().contains(outputContract)) {
			this.getOutputContracts().remove(outputContract);
			fireEvent(new PactPropertyChangeEvent(this));
		}
	}

	public void addOutputContract(PactOutputType outputContract) {
		if (!this.getOutputContracts().contains(outputContract)) {
			this.getOutputContracts().add(outputContract);
			fireEvent(new PactPropertyChangeEvent(this));
		}
	}

	/*
	 * source and sink pacts need to be numbered - for compilation
	 */
	public int getSourceOrSinkNumber() {
		return pactProgram.getSourceOrSinkNumber(this);
	}

	/**
	 * remove this pact from patent pact program and disconnect connections
	 */
	public void remove() {
		for (Connection connection : pactProgram.getConnections().values()) {
			if (connection.getFromPact() == this) {
				connection.setFromPact(null);
			}
			if (connection.getToPact() == this) {
				connection.setToPact(null);
			}
		}
		pactProgram.getPacts().remove(id);

		// tell other components that model changed
		fireEvent(new PactRemoveEvent(this));
	}

	/**
	 * get all connections given data into this pact
	 * 
	 * @return
	 */
	public ArrayList<Connection> getInputConntections() {
		ArrayList<Connection> inputConnections = new ArrayList<Connection>();

		for (Connection connection : pactProgram.getConnections().values()) {
			if (connection.getToPact() == this) {
				inputConnections.add(connection);
			}
		}

		return inputConnections;
	}

	/**
	 * get all connections taking data out of this pact
	 * 
	 * @return
	 */
	public ArrayList<Connection> getOutputConntections() {
		ArrayList<Connection> outputConnections = new ArrayList<Connection>();

		for (Connection connection : pactProgram.getConnections().values()) {
			if (connection.getFromPact() == this) {
				outputConnections.add(connection);
			}
		}

		return outputConnections;
	}


	public ArrayList<Connection> getAllConnections() {
		ArrayList<Connection> connections = new ArrayList<Connection>();
		
		for (Connection connection : pactProgram.getConnections().values()) {
			if (connection.getFromPact() == this || connection.getToPact() == this) {
				connections.add(connection);
			}
		}
		
		// TODO Auto-generated method stub
		return connections;
	}
	
	/**
	 * Get Predecessors of this pact in pact program DAG
	 * 
	 * @return
	 */
	public HashMap<Integer, Pact> getPredecessors() {
		return getPredecessors(100);
	}

	public HashMap<Integer, Pact> getPredecessors(int levelToCheck) {
		HashMap<Integer, Pact> predecessors = new HashMap<Integer, Pact>();

		int oldPredecessorsCount = 0;
		while (levelToCheck-- > 0) {
			for (Connection connection : getInputConntections()) {
				if (connection.getFromPact() != null) {
					if (this.equals(connection.getToPact()) || predecessors.containsValue(connection.getToPact())) {
						predecessors.put(connection.getFromPact().getId(), connection.getFromPact());
					}
				}
			}
			if (predecessors.size() == oldPredecessorsCount) {
				levelToCheck = 0;
			}
			oldPredecessorsCount = predecessors.size();
		}

		return predecessors;
	}

	/**
	 * Get successors of this pact in pact program DAG
	 * 
	 * @return
	 */
	public HashMap<Integer, Pact> getSuccessors() {
		return getSuccessors(100);
	}

	public HashMap<Integer, Pact> getSuccessors(int levelToCheck) {
		HashMap<Integer, Pact> successors = new HashMap<Integer, Pact>();

		int oldSuccessorsCount = 0;
		while (levelToCheck-- > 0) {
			for (Connection connection : getOutputConntections()) {
				if (connection.getToPact() != null) {
					if (this.equals(connection.getFromPact()) || successors.containsValue(connection.getFromPact())) {
						successors.put(connection.getToPact().getId(), connection.getToPact());
						successors.putAll(connection.getToPact().getSuccessors());
					}
				}
			}
			if (successors.size() == oldSuccessorsCount) {
				levelToCheck = 0;
			}
			oldSuccessorsCount = successors.size();
		}

		return successors;
	}

	/*
	 * return a list of error which prevent compiling this pact
	 */
	public ArrayList<Error> getValidationErrors() {
		String errorPrefix = getType().getLabel() + "-Pact (" + getName() + "): ";
		ArrayList<Error> errors = new ArrayList<Error>();

		// no output channel defined
		ArrayList<Connection> outputConnections = getOutputConntections();
		if (outputConnections.size() < this.getType().getNumberOfOutputs()) {
			errors.add(new Error(errorPrefix + "No output connection defined."));
		}

		// check number of connections per input channel
		ArrayList<Connection> inputConnections = getInputConntections();
		for (int channelNr = 0; channelNr < getType().getNumberOfInputs(); channelNr++) {
			int connectionsFoundPerInputChhannel = 0;
			for (Connection inputConnection : inputConnections) {
				if (inputConnection.getToChannelNumber() == channelNr) {
					connectionsFoundPerInputChhannel++;
				}
			}

			// connectionsFoundPerInputChhannel has to be exactly 1 in the
			// moment
			if (connectionsFoundPerInputChhannel == 0) {
				errors.add(new Error(errorPrefix + "Input channel " + channelNr + " has no input connection."));
			} else if (connectionsFoundPerInputChhannel > 1) {
				errors.add(new Error(errorPrefix + "has " + connectionsFoundPerInputChhannel
						+ " connection going into input channel " + channelNr
						+ ". This is not allowed, because so far automatic unions are not implemented."));
			}
		}

		// all pacts, sinks, sources and java need to contain one class named
		// like the pact
		if (getJavaCode().indexOf("class " + getName()) == -1) {
			errors.add(new Error(errorPrefix + "The user function does not contain a class with the name of the pact."));
		}

		// real pacts (not sinks, source or java) need to extend the
		// corresponding stub
		if (getType().isRealInputContract()) {
			if (getJavaCode().indexOf("extends " + getType().getLabel() + "Stub") == -1) {
				errors.add(new Error(errorPrefix + "The pact does not extend " + getType().getLabel()
						+ "Stub according to its input contract."));
			}
		}

		return errors;
	}

	public void fireEvent(GwtEvent<?> event) {
		if (pactProgram != null) {
			if (pactProgram.getPactProgramManager() != null) {
				if (pactProgram.getPactProgramManager().getAppInjector() != null) {
					Log.debug("Fire event from Pact-Model: " + event.toString());
					pactProgram.getPactProgramManager().getAppInjector().getEventBus().fireEvent(event);
				}
			}
		}
	}

	public String toString() {
		String out = "";

		out += "PACT(";
		out += "id:" + id;
		out += ", type:" + type;
		out += ", name:" + name;
		out += ", x:" + x;
		out += ", y:" + y;
		out += ", javaCode.length:" + javaCode.length();
		out += ")";

		return out;
	}
}
