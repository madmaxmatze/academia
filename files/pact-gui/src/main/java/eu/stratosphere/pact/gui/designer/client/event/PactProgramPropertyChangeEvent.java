package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Event fired over the event bus when properties of pact program are updated
 */
public class PactProgramPropertyChangeEvent extends GwtEvent<PactProgramPropertyChangeEventHandler> {
	public static final Type<PactProgramPropertyChangeEventHandler> TYPE = new Type<PactProgramPropertyChangeEventHandler>();

	private PactProgram pactProgram;

	public PactProgramPropertyChangeEvent(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	@Override
	public Type<PactProgramPropertyChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactProgramPropertyChangeEventHandler handler) {
		handler.onPactProgramPropertyChange(this);
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}
	
	@Override
    public String toString() {
      return "PactProgramPropertyChangeEvent";
    }
}