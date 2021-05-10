package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Event fired over the event bus when new file list as compilation results need to be displayed
 */
public class TabWidgetCloseEvent extends GwtEvent<TabWidgetCloseEventHandler> {
	public static final Type<TabWidgetCloseEventHandler> TYPE = new Type<TabWidgetCloseEventHandler>();
	private IsWidget tabWidget;
	
	public TabWidgetCloseEvent(IsWidget tabWidget) {
		this.tabWidget = tabWidget;
	}

	@Override
	public Type<TabWidgetCloseEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(TabWidgetCloseEventHandler handler) {
		handler.onClose(this);
	}
	
	public IsWidget getTabWidget() {
		return tabWidget;
	}
	
	@Override
    public String toString() {
      return "TabWidgetCloseEvent";
    }
}