package eu.stratosphere.pact.gui.designer.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Event fired over the event bus when new file list as compilation results need to be displayed
 */
public class TabWidgetSelectEvent extends GwtEvent<TabWidgetSelectEventHandler> {
	public static final Type<TabWidgetSelectEventHandler> TYPE = new Type<TabWidgetSelectEventHandler>();
	private IsWidget tabWidget;
	
	public TabWidgetSelectEvent(IsWidget tabWidget) {
		this.tabWidget = tabWidget;
	}

	@Override
	public Type<TabWidgetSelectEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(TabWidgetSelectEventHandler handler) {
		handler.onSelect(this);
	}
	
	public IsWidget getTabWidget() {
		return tabWidget;
	}
	
	@Override
    public String toString() {
      return "TabWidgetSelectEvent";
    }
}