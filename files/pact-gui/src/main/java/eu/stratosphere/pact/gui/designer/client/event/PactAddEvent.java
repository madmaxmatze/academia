package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Event fired over the event bus when new pact was added to pactProgram
 */
public class PactAddEvent extends GwtEvent<PactAddEventHandler> {
	public static final Type<PactAddEventHandler> TYPE = new Type<PactAddEventHandler>();

	private Pact pact;
	
	public PactAddEvent(Pact pact) {
		this.pact = pact;
	}

	@Override
	public Type<PactAddEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactAddEventHandler handler) {
		handler.onPactAdd(this);
	}

	public Pact getPact() {
		return pact;
	}
	
	@Override
    public String toString() {
      return "PactAddEvent";
    }
}