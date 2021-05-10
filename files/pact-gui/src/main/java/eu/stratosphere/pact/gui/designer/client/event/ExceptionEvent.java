package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired over the event when global exception needs handling
 */
public class ExceptionEvent extends GwtEvent<ExceptionEventHandler> {
	private Exception exception;
	public static final Type<ExceptionEventHandler> TYPE = new Type<ExceptionEventHandler>();

	public ExceptionEvent(Exception _exception) {
		exception = _exception;
	}

	@Override
	public Type<ExceptionEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ExceptionEventHandler handler) {
		handler.onException(this);
	}

	public Exception getException() {
		return exception;
	}
	
	@Override
    public String toString() {
      return "ExceptionEvent";
    }
}