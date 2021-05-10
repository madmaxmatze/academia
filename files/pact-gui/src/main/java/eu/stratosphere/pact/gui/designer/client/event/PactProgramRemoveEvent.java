package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Event fired over the event bus when program is removed
 */
public class PactProgramRemoveEvent extends GwtEvent<PactProgramRemoveEventHandler> {
	public static final Type<PactProgramRemoveEventHandler> TYPE = new Type<PactProgramRemoveEventHandler>();

	private PactProgram pactProgram;
	
	public PactProgramRemoveEvent(PactProgram pactProgram) {
		this.pactProgram = pactProgram;
	}

	@Override
	public Type<PactProgramRemoveEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactProgramRemoveEventHandler handler) {
		handler.onRemovePactProgram(this);
	}

	public PactProgram getPactProgram() {
		return pactProgram;
	}
	
	@Override
    public String toString() {
      return "PactProgramRemoveEvent";
    }
}