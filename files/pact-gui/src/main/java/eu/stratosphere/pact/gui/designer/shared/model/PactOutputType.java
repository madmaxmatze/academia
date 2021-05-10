package eu.stratosphere.pact.gui.designer.shared.model;

/*
 * Model to assign output contracts to pacts
 */
public enum PactOutputType {
	SAME_KEY("Same-Key"),

	SUPER_KEY("Super-Key"),

	UNIQUE_KEY("Unique-Key"),

	PARTITIONED_BY_KEY("Partitioned-by-Key");

	private String label = "";

	PactOutputType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}