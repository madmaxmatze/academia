package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Connection;

/**
 * Event fired over the event bus when new connection was added to pactProgram
 */
public class ConnectionAddEvent extends GwtEvent<ConnectionAddEventHandler> {
	public static final Type<ConnectionAddEventHandler> TYPE = new Type<ConnectionAddEventHandler>();
	private Connection connection;

	public ConnectionAddEvent(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Type<ConnectionAddEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ConnectionAddEventHandler handler) {
		handler.onConnectionAdd(this);
	}

	public Connection getConnection() {
		return connection;
	}
	
	@Override
    public String toString() {
      return "ConnectionAddEvent";
    }
}