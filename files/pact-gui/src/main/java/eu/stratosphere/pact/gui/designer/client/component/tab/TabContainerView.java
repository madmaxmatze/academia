package eu.stratosphere.pact.gui.designer.client.component.tab;

import javax.inject.Inject;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent;
import com.sencha.gxt.widget.core.client.event.BeforeCloseEvent.BeforeCloseHandler;

import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainer.TabContainerViewInterface;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetCloseEvent;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetSelectEvent;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;

/**
 * View-Widget representing all tabs (graph and code) of one pact program
 */
public class TabContainerView extends TabPanel implements TabContainerViewInterface {
	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * Tab Config object for first Tab - with PACT Graph
	 */
	TabItemConfig graphTabConfiguration = new TabItemConfig();

	/**
	 * Constructor of widget
	 * 
	 * @param appInjector
	 *            : Dependency Injection object
	 * @param pactProgram
	 *            : The model of the pact program this widget represents
	 */
	@Inject
	public TabContainerView(AppInjector appInjector) {
		// call base class (TabPanel widget) constructor
		super();
		this.appInjector = appInjector;
		Log.info(this + ": Constructor");

		// Configure Tabs
		this.setResizeTabs(false);
		this.setAnimScroll(true);
		this.setTabScroll(true);
		this.setCloseContextMenu(true);

		// remove border around head of Tab Panel
		this.getElement().getFirstChildElement().getStyle().setBorderStyle(BorderStyle.NONE);
		this.getContainer().setBorders(false);
		this.setBodyBorder(false);

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to gui objects
	 */
	private void bind() {
		/*
		 * Switch between tabs
		 */
		this.addSelectionHandler(new SelectionHandler<Widget>() {
			@Override
			public void onSelection(SelectionEvent<Widget> event) {
				appInjector.getEventBus().fireEvent(new TabWidgetSelectEvent((IsWidget) event.getSelectedItem()));  
			}
		});

		/*
		 * send delete temp folder ajax request, when compilation result tab is
		 * closed
		 */
		addBeforeCloseHandler(new BeforeCloseHandler<Widget>() {
			@Override
			public void onBeforeClose(BeforeCloseEvent<Widget> event) {
				appInjector.getEventBus().fireEvent(new TabWidgetCloseEvent((IsWidget) event.getItem()));
			}
		});
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void setActiveTab(TabItem tabItem) {
		setWidget(tabItem.getView());
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void addTab(TabItem newTabItem) {
		Log.debug("AddTabItem" + newTabItem);
		if (newTabItem != null) {
			this.add(newTabItem.getView(), newTabItem.getConfig());
		}
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void removeTab(TabItem tabItemtoRemove) {
		Log.info("Remove Tab");
		this.remove(tabItemtoRemove.getView());
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void updateTabTitle(TabItem tabItem) {
		update(tabItem.getView(), tabItem.getConfig());
	}
	
	public String toString() {
		return "TabContainerView";
	}
}