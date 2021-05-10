package eu.stratosphere.pact.gui.designer.client.component.tab;

import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.IsWidget;

import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramAddEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactProgramSelectEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;

/**
 * Wrapper for all TabPanels representing a pactprogram which are added to the
 * content panel
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class TabContainerManager {
	private HashMap<Integer, TabContainer> tabContainers = new HashMap<Integer, TabContainer>();

	/**
	 * just for testing
	 * 
	 * @return
	 */
	protected HashMap<Integer, TabContainer> getTabContainers() {
		return tabContainers;
	}

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	/**
	 * The model of the pact program this widget represents
	 */
	private TabContainerManagerViewInterface tabContainerManagerView;

	public TabContainerManagerViewInterface getView() {
		return tabContainerManagerView;
	}

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface TabContainerManagerViewInterface extends IsWidget {
		void removeTabContainer(TabContainer tabContainerToRemove);

		void addTabContainer(TabContainer newTabContainer);

		void setActiveTabContainer(TabContainer selectedTabContainer);
	}

	/**
	 * Constructor
	 * 
	 * @param appInjector
	 *            : Dependency Injection object
	 */
	public TabContainerManager(AppInjector appInjector) {
		Log.info(this + ": Constructor");

		this.appInjector = appInjector;
		this.tabContainerManagerView = appInjector.getTabContainerManagerView();

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to stupid view
	 */
	private void bind() {
		Log.info(this + ": bind");

		/**
		 * remove guiPactProgramContainer after PactProgram is removed
		 */
		appInjector.getEventBus().addHandler(PactProgramRemoveEvent.TYPE, new PactProgramRemoveEventHandler() {
			@Override
			public void onRemovePactProgram(PactProgramRemoveEvent event) {
				action_RemoveTabContainerForPactProgram(event.getPactProgram());
			}
		});

		/**
		 * The add event in this widget does the same as select
		 */
		appInjector.getEventBus().addHandler(PactProgramAddEvent.TYPE, new PactProgramAddEventHandler() {
			@Override
			public void onAddPactProgram(PactProgramAddEvent event) {
				action_AddAndSelectTabContainerForPactProgram(event.getPactProgram());
			}
		});

		/**
		 * When new PactProgram is selected (in the pactProgramList on the
		 * left), change content in this panel (the right content)
		 */
		appInjector.getEventBus().addHandler(PactProgramSelectEvent.TYPE, new PactProgramSelectEventHandler() {
			@Override
			public void onSelectPactProgram(PactProgramSelectEvent event) {
				action_AddAndSelectTabContainerForPactProgram(event.getPactProgram());
			}
		});
	}

	/**
	 * when a pact program is closed (removed from model) this method is called
	 * to remove all tabs connected to this program
	 * 
	 * @param pactProgramToRemove
	 */
	protected void action_RemoveTabContainerForPactProgram(PactProgram pactProgramToRemove) {
		if (pactProgramToRemove != null) {
			TabContainer tabContainerToRemove = tabContainers.get(pactProgramToRemove.getId());

			if (tabContainerToRemove == null) {
				Log.warn("guiPactProgramContainerToRemove not existing for pactProgram " + pactProgramToRemove);
			} else {
				tabContainers.remove(pactProgramToRemove.getId());
				tabContainerManagerView.removeTabContainer(tabContainerToRemove);
			}
		}
	}

	/**
	 * when a new pact program is opened (added to model) this method is called
	 * to add new tabs connected to this program
	 * 
	 * @param pactProgramToRemove
	 */
	protected TabContainer action_AddAndSelectTabContainerForPactProgram(PactProgram selectedPactProgram) {
		TabContainer selectedTabContainer = null;

		if (selectedPactProgram != null) {
			selectedTabContainer = tabContainers.get(selectedPactProgram.getId());
			if (selectedTabContainer == null) {
				selectedTabContainer = new TabContainer(appInjector, selectedPactProgram);
				tabContainers.put(selectedPactProgram.getId(), selectedTabContainer);
				tabContainerManagerView.addTabContainer(selectedTabContainer);
			}

			tabContainerManagerView.setActiveTabContainer(selectedTabContainer);
		}

		return selectedTabContainer;
	}

	public String toString() {
		return "PactProgramTabManager";
	}
}