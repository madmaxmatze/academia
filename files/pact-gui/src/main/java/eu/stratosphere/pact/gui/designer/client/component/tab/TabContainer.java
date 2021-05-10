package eu.stratosphere.pact.gui.designer.client.component.tab;

import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.IsWidget;

import eu.stratosphere.pact.gui.designer.client.event.CompilationResultReceivedEvent;
import eu.stratosphere.pact.gui.designer.client.event.CompilationResultReceivedEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactPropertyChangeEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEvent;
import eu.stratosphere.pact.gui.designer.client.event.PactRemoveEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetCloseEvent;
import eu.stratosphere.pact.gui.designer.client.event.TabWidgetCloseEventHandler;
import eu.stratosphere.pact.gui.designer.client.event.UserFunctionOpenEvent;
import eu.stratosphere.pact.gui.designer.client.event.UserFunctionOpenEventHandler;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * Presenter representing all tabs (graph and code) of one pact program
 */
public class TabContainer {
	private TabContainer self = this;

	/**
	 * The model of the pact program this widget represents
	 */
	private PactProgram pactProgram;

	public PactProgram getPactProgram() {
		return pactProgram;
	}

	/**
	 * Dependency Injection object
	 */
	private AppInjector appInjector;

	HashMap<String, TabItem> tabItems = new HashMap<String, TabItem>();

	/**
	 * The model of the pact program this widget represents
	 */
	private TabContainerViewInterface tabContainerView;

	public TabContainerViewInterface getView() {
		return tabContainerView;
	}

	/**
	 * Interface defines needed methods of all view (eg web / mobil...) which
	 * can be provided to this presenter
	 */
	public interface TabContainerViewInterface extends IsWidget {
		void addTab(TabItem newTabItem);

		void setActiveTab(TabItem newTabItem);

		void removeTab(TabItem tabItemtoRemove);

		void updateTabTitle(TabItem tabItem);
	}

	/**
	 * Constructor of widget
	 * 
	 * @param appInjector
	 *            : Dependency Injection object
	 * @param pactProgram
	 *            : The model of the pact program this widget represents
	 */
	public TabContainer(AppInjector appInjector, PactProgram pactProgram) {
		// call base class (TabPanel widget) constructor
		super();
		this.pactProgram = pactProgram;
		this.appInjector = appInjector;
		Log.info(this + ": Constructor");

		tabContainerView = appInjector.getTabContainerView();

		// add first graph tab - embed GWT modul: GraphWidget
		TabItem tabItem = new TabItem(appInjector, pactProgram);
		tabContainerView.addTab(tabItem);

		bind();
	}

	/**
	 * Bind all event handler, so add functionality to gui objects
	 */
	private void bind() {
		Log.info(this + ": bind");

		/*
		 * when pact name or type changes - tab header need to change as well
		 */
		appInjector.getEventBus().addHandler(PactPropertyChangeEvent.TYPE, new PactPropertyChangeEventHandler() {
			@Override
			public void onPactPropertyChange(PactPropertyChangeEvent event) {
				action_updateTabForPact(event.getPact());
			}
		});

		/*
		 * Add/select new java tab to edit pact user function
		 */
		appInjector.getEventBus().addHandler(UserFunctionOpenEvent.TYPE, new UserFunctionOpenEventHandler() {
			@Override
			public void onUserFunctionOpen(UserFunctionOpenEvent event) {
				Log.debug(self + ": UserFunctionOpenEvent");
				action_CreateAndOrSelectTabForPact(event.getPact());
			}
		});

		/**
		 * When pact was removed from the model, remove code editor tab
		 */
		appInjector.getEventBus().addHandler(PactRemoveEvent.TYPE, new PactRemoveEventHandler() {
			@Override
			public void onPactRemove(PactRemoveEvent event) {
				action_RemoveTabForPact(event.getPact());
			}
		});

		/*
		 * Add new tab to display compiler results
		 */
		appInjector.getEventBus().addHandler(CompilationResultReceivedEvent.TYPE,
				new CompilationResultReceivedEventHandler() {
					@Override
					public void onReceive(CompilationResultReceivedEvent event) {
						if (event.getResult() != null && event.getResult().getPactProgramId() == pactProgram.getId()) {
							action_addCompileResultsTab(event.getResult());
						}
					}
				});

		/**
		 * remove tab from list when tab is closed
		 */
		appInjector.getEventBus().addHandler(TabWidgetCloseEvent.TYPE, new TabWidgetCloseEventHandler() {
			@Override
			public void onClose(TabWidgetCloseEvent event) {
				if (event.getTabWidget() != null) {
					Log.info(this + ": Catch TabWidgetCloseEvent");
					TabItem tabItem = null;
					for (TabItem ti : tabItems.values()) {
						if (ti.getView() == event.getTabWidget()) {
							tabItem = ti;
						}
					}
					if (tabItem != null) {
						tabItems.remove(tabItem.getId());
					}
				}
			}
		});
	}

	/**
	 * add tab for CompilerResult (method is called after
	 * CompilationResultReceivedEvent is catched)
	 * 
	 * @param result
	 */
	public void action_addCompileResultsTab(PactProgramCompilerResult result) {
		if (result != null && result.getPactProgramId() == pactProgram.getId()) {
			Log.debug(self + ": add compilation results");

			TabItem newTabItem = new TabItem(appInjector, result);
			tabItems.put(newTabItem.getId(), newTabItem);

			// add new widget to tab container
			tabContainerView.addTab(newTabItem);
			tabContainerView.setActiveTab(newTabItem);
		}
	}

	/**
	 * remove java editor tab for Pact (method is called after
	 * PactRemoveEventHandler is catched)
	 * 
	 * @param result
	 */
	public void action_RemoveTabForPact(Pact pact) {
		if (pact != null && pact.getPactProgram() == pactProgram) {
			TabItem tabItemtoRemove = getTabForPact(pact);
			if (tabItemtoRemove != null) {
				tabItems.remove(pact.getId());
				tabContainerView.removeTab(tabItemtoRemove);
			}
		}
	}

	/**
	 * add/open java editor tab for Pact (method is called after
	 * UserFunctionOpenEventHandler is catched)
	 * 
	 * @param result
	 */
	public TabItem action_CreateAndOrSelectTabForPact(Pact pact) {
		TabItem tabItemForPact = null;

		if (pact != null && pact.getPactProgram() == pactProgram) {

			tabItemForPact = getTabForPact(pact);
			if (tabItemForPact == null) {
				tabItemForPact = action_createTabForPact(pact);
				if (tabItemForPact == null) {
					Log.warn("Tab could not be found or created");
				}
			}

			if (tabItemForPact != null) {
				tabContainerView.setActiveTab(tabItemForPact);
			}
		}

		return tabItemForPact;
	}

	/**
	 * actually create a new java editor tab for a pact
	 * 
	 * @param result
	 */
	private TabItem action_createTabForPact(Pact pact) {
		Log.debug(this + ": action_createTabForPact");
		TabItem newTabItemForPact = null;

		if (pact != null && pact.getPactProgram() == pactProgram) {
			newTabItemForPact = getTabForPact(pact);
			if (newTabItemForPact == null) {
				newTabItemForPact = new TabItem(appInjector, pact);
				tabItems.put(newTabItemForPact.getId(), newTabItemForPact);
				tabContainerView.addTab(newTabItemForPact);
				Log.debug("Add Tab: " + newTabItemForPact);
			}
		}

		return newTabItemForPact;
	}

	/**
	 * update title of java editor tab for Pact (method is called after
	 * PactRemoveEventHandler is catched)
	 * 
	 * @param pact
	 * @return
	 */
	public TabItem action_updateTabForPact(Pact pact) {
		TabItem tabItemForPactToUpdate = null;

		if (pact != null && pact.getPactProgram() == pactProgram) {
			tabItemForPactToUpdate = getTabForPact(pact);
			if (tabItemForPactToUpdate != null) {
				tabContainerView.updateTabTitle(tabItemForPactToUpdate);
			}
		}

		return tabItemForPactToUpdate;
	}

	/**
	 * remove java editor tab for Pact (method is called after
	 * PactPropertyChangeEventHandler is catched)
	 * 
	 * @param result
	 */
	private TabItem getTabForPact(Pact pact) {
		if (pact != null && pact.getPactProgram() == pactProgram) {
			return tabItems.get(String.valueOf(pact.getId()));
		}
		return null;
	}

	public String toString() {
		return "PactProgramTab (id:" + pactProgram.getId() + ", name:" + pactProgram.getName() + "): ";
	}
}