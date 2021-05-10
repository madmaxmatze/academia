package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Pact;

/**
 * Event fired over the event bus when pact needs to be focused in all relevant widgets
 */
public class PactFocusEvent extends GwtEvent<PactFocusEventHandler> {
	public static final Type<PactFocusEventHandler> TYPE = new Type<PactFocusEventHandler>();

	private Pact pact;

	public PactFocusEvent(Pact pact) {
		this.pact = pact;
	}

	@Override
	public Type<PactFocusEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(PactFocusEventHandler handler) {
		handler.onPactFocus(this);
	}

	public Pact getPact() {
		return pact;
	}
	
	@Override
    public String toString() {
      return "PactFocusEvent";
    }
}