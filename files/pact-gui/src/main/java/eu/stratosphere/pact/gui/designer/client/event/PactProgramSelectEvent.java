package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Event fired over the event bus when program is selected
 */
public class PactProgramSelectEvent extends GwtEvent<PactProgramSelectEventHandler> {
	public static final Type<PactProgramSelectEventHandler> TYPE = new Type<PactProgramSelectEventHandler>();

	private PactProgram pactProgram;
	
	public PactProgramSelectEvent(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	@Override
	public Type<PactProgramSelectEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactProgramSelectEventHandler handler) {
		handler.onSelectPactProgram(this);
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}
	
	@Override
    public String toString() {
      return "PactProgramSelectEvent";
    }
}