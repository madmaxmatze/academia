package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Connection;

/**
 * Event fired over the event bus when connection gets need to be focused in all relevant widgets
 */
public class ConnectionFocusEvent extends GwtEvent<ConnectionFocusEventHandler> {
	public static final Type<ConnectionFocusEventHandler> TYPE = new Type<ConnectionFocusEventHandler>();

	private Connection connection;

	public ConnectionFocusEvent(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public Type<ConnectionFocusEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ConnectionFocusEventHandler handler) {
		handler.onConnectionFocus(this);
	}

	public Connection getConnection() {
		return connection;
	}
	
	@Override
    public String toString() {
      return "ConnectionFocusEvent";
    }
}