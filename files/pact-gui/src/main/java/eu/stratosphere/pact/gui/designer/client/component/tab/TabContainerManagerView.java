package eu.stratosphere.pact.gui.designer.client.component.tab;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;

import eu.stratosphere.pact.gui.designer.client.component.tab.TabContainerManager.TabContainerManagerViewInterface;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;

/**
 * Wrapper for all TabPanels (which representing a pactprogram)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class TabContainerManagerView extends CardLayoutContainer implements TabContainerManagerViewInterface {
	/**
	 * Dependency Injection object
	 */
	@SuppressWarnings("unused")
	private AppInjector appInjector;

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 *            : Dependency Injection object
	 */
	@Inject
	public TabContainerManagerView(AppInjector appInjector) {
		super();
		this.appInjector = appInjector;

		Log.info(this + ": Constructor");
		
		CenterLayoutContainer centerContainer = new CenterLayoutContainer();
		ContentPanel contentPanel = new ContentPanel();
		contentPanel.setBodyStyle("padding: 6px");
		contentPanel.setHeadingText("Welcome");
		contentPanel.add(new Label("Please use the menu to load or create a PACT-PROGRAM."));
		contentPanel.setWidth(230);
		centerContainer.add(contentPanel);

		this.add(centerContainer);
	}
	
	public String toString() {
		return "PactProgramTabManagerView";
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void removeTabContainer(TabContainer tabContainerToRemove) {
		this.remove(tabContainerToRemove.getView());
		forceLayout();
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void addTabContainer(TabContainer newTabContainer) {
		add(newTabContainer.getView());
	}

	/**
	 * provide view methods to presenter
	 */
	@Override
	public void setActiveTabContainer(TabContainer selectedTabContainer) {
		setWidget(selectedTabContainer.getView());
	}
}