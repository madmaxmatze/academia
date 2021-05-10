package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Event fired over the event bus when pact is removed from program
 */
public class PactRemoveEvent extends GwtEvent<PactRemoveEventHandler> {
	public static final Type<PactRemoveEventHandler> TYPE = new Type<PactRemoveEventHandler>();

	private Pact pact;
	
	public PactRemoveEvent(Pact pact) {
		this.pact = pact;
	}

	@Override
	public Type<PactRemoveEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactRemoveEventHandler handler) {
		handler.onPactRemove(this);
	}

	public Pact getPact() {
		return pact;
	}
	
	@Override
    public String toString() {
      return "PactRemoveEvent";
    }
}