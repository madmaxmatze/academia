package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;

import eu.stratosphere.pact.gui.designer.shared.model.Connection;

/**
 * Event fired over the event bus when new connection was remove to pactProgram
 */
public class ConnectionRemoveEvent extends GwtEvent<ConnectionRemoveEventHandler> {
	public static final Type<ConnectionRemoveEventHandler> TYPE = new Type<ConnectionRemoveEventHandler>();

	private Connection connection;
	
	public ConnectionRemoveEvent(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Type<ConnectionRemoveEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ConnectionRemoveEventHandler handler) {
		handler.onConnectionRemove(this);
	}

	public Connection getConnection() {
		return connection;
	}
	
	@Override
    public String toString() {
      return "ConnectionRemoveEvent";
    }
}