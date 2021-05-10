package eu.stratosphere.pact.gui.designer.client.event;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Fire Event over the event to get ping (to log) from all registered listeners
 * Used to check Widget and presenter Garbage collection. Was sometimes not working because of pending references
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class ComponentPingEvent extends GwtEvent<ComponentPingEventHandler> {
	public static final Type<ComponentPingEventHandler> TYPE = new Type<ComponentPingEventHandler>();

	public ComponentPingEvent() {
		Log.debug("Ping all Components");
	}

	@Override
	public Type<ComponentPingEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ComponentPingEventHandler handler) {
		handler.onPing(this);
	}
	
	@Override
    public String toString() {
      return "ComponentPingEvent";
    }
}