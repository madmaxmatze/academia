package eu.stratosphere.pact.gui.designer.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.client.event.ConnectionPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.ConnectionRemoveEvent;

/**
 * Data Model for Connection
 */
public class Connection implements Serializable {
	private static final long serialVersionUID = 7830554084888264967L;

	/*
	 * pact program wide unique ID of this object
	 */
	private int id;

	/*
	 * this connection goes into which input channel of the to pact
	 */
	private int toChannelNumber = 0;

	/*
	 * from and to location of connection
	 */
	private int fromX = 1;
	private int fromY = 1;
	private int toX = 1;
	private int toY = 1;

	/*
	 * from and to pact of this connection
	 */
	private Pact fromPact = null;
	private Pact toPact = null;

	/*
	 * reference to parent pact program
	 */
	private PactProgram pactProgram = null;

	/**
	 * Empty constructor needed for serialization
	 */
	public Connection() {
	}

	/*
	 * Getter and setter
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setFromX(int fromX) {
		setFromX(fromX, true);
	}

	public void setFromX(int fromX, boolean fireEvent) {
		if (fromX != this.fromX) {
			this.fromX = fromX;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public int getFromX() {
		return fromX;
	}

	public void setFromY(int fromY) {
		setFromY(fromY, true);
	}

	public void setFromY(int fromY, boolean fireEvent) {
		if (fromY != this.fromY) {
			this.fromY = fromY;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public int getFromY() {
		return fromY;
	}

	public void setToX(int toX) {
		setToX(toX, true);
	}

	public void setToX(int toX, boolean fireEvent) {
		if (toX != this.toX) {
			this.toX = toX;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public int getToX() {
		return toX;
	}

	public void setToY(int toY) {
		setToY(toY, true);
	}

	public void setToY(int toY, boolean fireEvent) {
		if (toY != this.toY) {
			this.toY = toY;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public int getToY() {
		return toY;
	}

	public Pact getToPact() {
		return toPact;
	}

	public void setToPact(Pact toPact) {
		setToPact(toPact, true);
	}

	public void setToPact(Pact toPact, boolean fireEvent) {
		if (toPact != this.toPact) {
			this.toPact = toPact;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public Pact getFromPact() {
		return fromPact;
	}

	public void setFromPact(Pact fromPact) {
		setFromPact(fromPact, true);
	}

	public void setFromPact(Pact fromPact, boolean fireEvent) {
		if (fromPact != this.fromPact) {
			this.fromPact = fromPact;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public void setToChannelNumber(int toChannelNumber) {
		setToChannelNumber(toChannelNumber, true);
	}

	public void setToChannelNumber(int toChannelNumber, boolean fireEvent) {
		if (toChannelNumber != this.toChannelNumber) {
			this.toChannelNumber = toChannelNumber;
			if (fireEvent) {
				fireEvent(new ConnectionPropertyChangeEvent(this));
			}
		}
	}

	public int getToChannelNumber() {
		return toChannelNumber;
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}

	public void setPactProgram(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	public void remove() {
		setFromPact(null);
		setToPact(null);

		pactProgram.getConnections().remove(this.getId());
		fireEvent(new ConnectionRemoveEvent(this));
	}

	public void fireEvent(GwtEvent<?> event) {
		if (pactProgram != null) {
			if (pactProgram.getPactProgramManager() != null) {
				if (pactProgram.getPactProgramManager().getAppInjector() != null) {
					Log.debug("Fire event from Connection-Model: " + event.toString());
					pactProgram.getPactProgramManager().getAppInjector().getEventBus().fireEvent(event);
				}
			}
		}
	}

	public boolean isValid() {
		boolean isValid = true;

		if (fromPact == null || toPact == null) {
			isValid = false;
		}

		return isValid;
	}

	public ArrayList<Error> getValidationErrors() {
		ArrayList<Error> errors = new ArrayList<Error>();

		if (!isValid()) {
			errors.add(new Error("Connection-" + getId() + ": " + "Not both ends of the connection are connected."));
		}
		return errors;
	}

	public String toString() {
		String out = "Connection (id:" + id + ", fromPactId:" + (fromPact == null ? "null" : fromPact.getId())
				+ ", toPactId:" + (toPact == null ? "null" : toPact.getId());
		return out;
	}
}
