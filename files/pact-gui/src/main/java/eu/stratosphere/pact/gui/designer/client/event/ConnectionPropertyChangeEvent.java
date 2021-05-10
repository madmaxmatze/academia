package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Connection;

/**
 * Event fired over the event bus when pact properties are updated
 */
public class ConnectionPropertyChangeEvent extends GwtEvent<ConnectionPropertyChangeEventHandler> {
	public static final Type<ConnectionPropertyChangeEventHandler> TYPE = new Type<ConnectionPropertyChangeEventHandler>();

	private Connection connection;
	
	public ConnectionPropertyChangeEvent(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Type<ConnectionPropertyChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ConnectionPropertyChangeEventHandler handler) {
		handler.onConnectionPropertyChange(this);
	}

	public Connection getConnection() {
		return connection;
	}
	
	@Override
    public String toString() {
      return "ConnectionPropertyChangeEvent";
    }
}