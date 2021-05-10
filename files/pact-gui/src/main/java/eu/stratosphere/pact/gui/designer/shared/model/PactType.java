package eu.stratosphere.pact.gui.designer.shared.model;

import java.util.ArrayList;

/*
 * Model to assign input contract (+source, sink, java) to pact
 */
public enum PactType {
	/*
	 * Java class (not a real input contract, only for helper and utility classes)
	 */
	JAVA("Java", 0, 0),

	/*
	 * Data source
	 */
	SOURCE("Source", 0, 1),

	/*
	 * Data sink
	 */
	SINK("Sink", 1, 0),

	PACT_MAP("Map", 1, 1),

	PACT_REDUCE("Reduce", 1, 1),

	PACT_MATCH("Match", 2, 1),

	PACT_CROSS("Cross", 2, 1),

	PACT_COGROUP("CoGroup", 2, 1);

	private String label = "";
	private int numberOfInputs;
	private int numberOfOutputs;

	/*
	 * Construct Enum type to assign properties
	 */
	PactType(String label, int numberOfInputs, int numberOfOutputs) {
		this.label = label;
		this.numberOfInputs = numberOfInputs;
		this.numberOfOutputs = numberOfOutputs;
	}

	public String getLabel() {
		return label;
	}

	/*
	 * return number of input channels for a pact type
	 */
	public int getNumberOfInputs() {
		return numberOfInputs;
	}

	/*
	 * return number of output channels for a pact type
	 */
	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}

	/*
	 * Find out if this type is a real pact input contract and not just source, sink or java
	 */
	public boolean isRealInputContract() {
		return !(this == PactType.JAVA || this == PactType.SINK || this == PactType.SOURCE);
	}

	public boolean isSinkOrSource() {
		return (this == PactType.SINK || this == PactType.SOURCE);
	}

	/*
	 * shorthand to get list of types (to create type dropdown)
	 */
	public static ArrayList<PactType> getInputContractTypes() {
		ArrayList<PactType> inputContractTypes = new ArrayList<PactType>();
		for (final PactType p : PactType.values()) {
			if (p.isRealInputContract()) {
				inputContractTypes.add(p);
			}
		}
		return inputContractTypes;
	}

	public String toString() {
		return getLabel();
	}
}