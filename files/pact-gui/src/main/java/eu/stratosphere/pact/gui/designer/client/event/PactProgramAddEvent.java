package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Event fired over the event bus when new program was added to program manager
 */
public class PactProgramAddEvent extends GwtEvent<PactProgramAddEventHandler> {
	public static final Type<PactProgramAddEventHandler> TYPE = new Type<PactProgramAddEventHandler>();

	private PactProgram pactProgram;
	
	public PactProgramAddEvent(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}
	
	@Override
	public Type<PactProgramAddEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactProgramAddEventHandler handler) {
		handler.onAddPactProgram(this);
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}
	
	@Override
    public String toString() {
      return "PactProgramAddEvent";
    }
}