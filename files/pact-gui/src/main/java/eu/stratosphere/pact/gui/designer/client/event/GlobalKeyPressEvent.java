package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired over the event bus, to propagate key press events
 */
public class GlobalKeyPressEvent extends GwtEvent<GlobalKeyPressEventHandler> {
	public static final Type<GlobalKeyPressEventHandler> TYPE = new Type<GlobalKeyPressEventHandler>();

	private KeyDownEvent nativeKeyEvent;
	
	public GlobalKeyPressEvent(KeyDownEvent nativeKeyEvent) {
		this.nativeKeyEvent = nativeKeyEvent;
	}

	@Override
	public Type<GlobalKeyPressEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(GlobalKeyPressEventHandler handler) {
		handler.onGlobalKeyPress(this);
	}

	public KeyDownEvent getNativeKeyEvent() {
		return nativeKeyEvent;
	}
	
	@Override
    public String toString() {
      return "GlobalKeyPressEvent";
    }
}