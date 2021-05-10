package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Event fired over the event bus when pact properties are updated
 */
public class PactPropertyChangeEvent extends GwtEvent<PactPropertyChangeEventHandler> {
	public static final Type<PactPropertyChangeEventHandler> TYPE = new Type<PactPropertyChangeEventHandler>();

	private Pact pact;
	
	public PactPropertyChangeEvent(Pact pact) {
		this.pact = pact;
	}

	@Override
	public Type<PactPropertyChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactPropertyChangeEventHandler handler) {
		handler.onPactPropertyChange(this);
	}

	public Pact getPact() {
		return pact;
	}
	
	@Override
    public String toString() {
      return "PactPropertyChangeEvent";
    }
}